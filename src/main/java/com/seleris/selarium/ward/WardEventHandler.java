package com.seleris.selarium.ward;

import com.seleris.selarium.config.SelariumCommonConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class WardEventHandler {
    private static boolean propagatingSoulChain;

    private WardEventHandler() {
    }

    @SubscribeEvent
    public static void onTeleport(EntityTeleportEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }

        SelariumCommonConfig.MvpWardConfig config = SelariumCommonConfig.mvpWard(WardType.DISRUPTION);
        if (!config.enabled().get()) {
            return;
        }

        Entity entity = event.getEntity();
        Vec3 target = event.getTarget();
        ArrayList<ActiveWardIndex.WardInstance> wards = new ArrayList<>();
        wards.addAll(ActiveWardIndex.find(level, entity.position(), WardType.DISRUPTION, config.range().get()));
        wards.addAll(ActiveWardIndex.find(level, target, WardType.DISRUPTION, config.range().get()));
        for (ActiveWardIndex.WardInstance ward : wards) {
            boolean canAffect = entity instanceof LivingEntity living
                    ? WardTargetingService.isInvader(ward.sigil(), living, true, config.affectPlayers().get(), config.affectBosses().get())
                    : config.optionB().get() && entity instanceof Projectile;
            if (canAffect && ward.sigil().hasRecentPaidUpkeep(config.tickInterval().get() + 5)) {
                event.setCanceled(true);
                ward.sigil().recordWardDebug(1, 0, "teleport disrupted");
                WardFx.burst(level, entity.position().add(0.0D, entity.getBbHeight() * 0.5D, 0.0D), WardType.DISRUPTION, 10, 0.4D);
                return;
            }
        }
    }

    @SubscribeEvent
    public static void onMobSpawnPosition(MobSpawnEvent.PositionCheck event) {
        ServerLevel level = event.getLevel().getLevel();
        SelariumCommonConfig.MvpWardConfig config = SelariumCommonConfig.mvpWard(WardType.SANCTUARY);
        if (!config.enabled().get()) {
            return;
        }

        boolean fromSpawner = event.getSpawner() != null || event.getSpawnType() == MobSpawnType.SPAWNER;
        if ((fromSpawner && !config.optionB().get()) || (!fromSpawner && !config.optionA().get())) {
            return;
        }

        Mob mob = event.getEntity();
        Vec3 spawnPos = new Vec3(event.getX(), event.getY(), event.getZ());
        for (ActiveWardIndex.WardInstance ward : ActiveWardIndex.find(level, spawnPos, WardType.SANCTUARY, config.range().get())) {
            if (WardTargetingService.isInvader(ward.sigil(), mob, true, false, config.affectBosses().get())
                    && ward.sigil().hasRecentPaidUpkeep(config.tickInterval().get() + 5)) {
                event.setResult(Event.Result.DENY);
                ward.sigil().recordWardDebug(1, 0, "hostile spawn blocked");
                return;
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }

        SelariumCommonConfig.MvpWardConfig config = SelariumCommonConfig.mvpWard(WardType.BOUNTY);
        if (!config.enabled().get() || event.getDrops().isEmpty()) {
            return;
        }

        LivingEntity entity = event.getEntity();
        for (ActiveWardIndex.WardInstance ward : ActiveWardIndex.find(level, entity.position(), WardType.BOUNTY, config.range().get())) {
            if (!WardTargetingService.isInvader(ward.sigil(), entity, true, false, config.affectBosses().get())
                    || !ward.sigil().hasRecentPaidUpkeep(config.tickInterval().get() + 5)) {
                continue;
            }

            int added = 0;
            if (level.random.nextDouble() <= config.chance().get()) {
                ItemStack oneItem = ItemStack.EMPTY;
                for (ItemEntity drop : event.getDrops()) {
                    if (drop.getItem().isEmpty()) continue;
                    oneItem = drop.getItem().copy();
                    oneItem.setCount(1);
                    break;
                }
                if (!oneItem.isEmpty()) {
                    event.getDrops().add(new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(), oneItem));
                    added = 1;
                    WardFx.burst(level, entity.position().add(0.0D, 0.5D, 0.0D), WardType.BOUNTY, 8, 0.35D);
                }
            }
            ward.sigil().recordWardDebug(added, 0, added > 0 ? "bounty added one item" : "bounty chance missed");
            return;
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }
        handleSoulChain(level, event);
        handleImmortalDamage(level, event);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }
        if (tryPreventDeath(level, event.getEntity())) {
            event.setCanceled(true);
        }
    }

    private static void handleImmortalDamage(ServerLevel level, LivingDamageEvent event) {
        if (event.getAmount() < event.getEntity().getHealth()) {
            return;
        }
        if (tryPreventDeath(level, event.getEntity())) {
            event.setAmount(0.0F);
        }
    }

    private static boolean tryPreventDeath(ServerLevel level, LivingEntity entity) {
        SelariumCommonConfig.MvpWardConfig config = SelariumCommonConfig.mvpWard(WardType.IMMORTAL);
        if (!config.enabled().get()) {
            return false;
        }

        long now = level.getServer().overworld().getGameTime();
        ImmortalCooldownSavedData cooldowns = ImmortalCooldownSavedData.get(level);
        if (!cooldowns.canProtect(entity.getUUID(), now)) {
            return false;
        }

        for (ActiveWardIndex.WardInstance ward : ActiveWardIndex.find(level, entity.position(), WardType.IMMORTAL, config.range().get())) {
            if (canImmortalProtect(ward, config, entity)
                    && ward.sigil().hasRecentPaidUpkeep(config.tickInterval().get() + 5)
                    && WardManaService.consume(level, ward.pos(), ward.sigil(), 200)) {
                entity.setHealth(1.0F);
                cooldowns.protectedUntil(entity.getUUID(), now + Math.max(1200, config.cooldownPerEntityTicks().get()));
                ward.sigil().recordWardDebug(1, 0, "death prevented");
                WardFx.burst(level, entity.position().add(0.0D, entity.getBbHeight() * 0.5D, 0.0D), WardType.IMMORTAL, 24, 0.6D);
                WardFx.pulse(level, entity.blockPosition(), WardType.IMMORTAL, 4);
                level.playSound(null, entity.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.6F, 1.4F);
                return true;
            }
        }
        return false;
    }

    private static boolean canImmortalProtect(ActiveWardIndex.WardInstance ward, SelariumCommonConfig.MvpWardConfig config, LivingEntity entity) {
        return WardAccessService.shouldAffectPositiveWard(ward.sigil(), entity);
    }

    private static void handleSoulChain(ServerLevel level, LivingDamageEvent event) {
        if (propagatingSoulChain || event.getAmount() <= 0.0F) {
            return;
        }

        SelariumCommonConfig.MvpWardConfig config = SelariumCommonConfig.mvpWard(WardType.SOUL_CHAIN);
        if (!config.enabled().get()) {
            return;
        }

        LivingEntity sourceTarget = event.getEntity();
        for (ActiveWardIndex.WardInstance ward : ActiveWardIndex.find(level, sourceTarget.position(), WardType.SOUL_CHAIN, config.range().get())) {
            if (!WardTargetingService.isInvader(ward.sigil(), sourceTarget, true, config.affectPlayers().get(), config.affectBosses().get())
                    || !ward.sigil().hasRecentPaidUpkeep(config.tickInterval().get() + 5)) {
                continue;
            }

            int affected = 0;
            float chainedDamage = Math.max(0.0F, event.getAmount() * config.strength().get().floatValue());
            propagatingSoulChain = true;
            try {
                for (LivingEntity linked : WardTargetingService.findInvaders(level, ward.pos(), ward.sigil(), config.range().get(), true, config.affectPlayers().get(), config.affectBosses().get())) {
                    if (linked == sourceTarget || affected >= config.maxEntitiesPerCycle().get()) {
                        continue;
                    }
                    linked.hurt(event.getSource(), chainedDamage);
                    WardFx.trail(level, sourceTarget.getEyePosition(), linked.getEyePosition(), WardType.SOUL_CHAIN, 8);
                    affected++;
                }
            } finally {
                propagatingSoulChain = false;
            }
            ward.sigil().recordWardDebug(affected, 0, affected > 0 ? "soul-chain propagated" : "no linked targets");
            return;
        }
    }
}
