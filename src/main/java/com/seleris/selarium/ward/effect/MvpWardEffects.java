package com.seleris.selarium.ward.effect;

import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.blockentity.ArcaneGrinderBlockEntity;
import com.seleris.selarium.registry.SelariumBlocks;
import com.seleris.selarium.ward.WardContext;
import com.seleris.selarium.ward.WardAccessService;
import com.seleris.selarium.ward.WardManaService;
import com.seleris.selarium.ward.WardTargetingService;
import com.seleris.selarium.ward.WardType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class MvpWardEffects {
    private MvpWardEffects() {
    }

    public static final class Fertility extends Effect {
        public Fertility() {
            super(WardType.FERTILITY);
        }
    }

    public static final class Citadel extends Effect {
        public Citadel() {
            super(WardType.CITADEL);
        }
    }

    public static final class Disruption extends Effect {
        public Disruption() {
            super(WardType.DISRUPTION);
        }
    }

    public static final class Cloaking extends Effect {
        public Cloaking() {
            super(WardType.CLOAKING);
        }
    }

    public static final class Accelerating extends Effect {
        public Accelerating() {
            super(WardType.ACCELERATING);
        }
    }

    public static final class Efficiency extends Effect {
        public Efficiency() {
            super(WardType.EFFICIENCY);
        }
    }

    public static final class Crushing extends Effect {
        public Crushing() {
            super(WardType.CRUSHING);
        }
    }

    public static final class Inversion extends Effect {
        public Inversion() {
            super(WardType.INVERSION);
        }
    }

    public static final class Aqualung extends Effect {
        public Aqualung() {
            super(WardType.AQUALUNG);
        }
    }

    public static final class Transmutation extends Effect {
        public Transmutation() {
            super(WardType.TRANSMUTATION);
        }
    }

    public static final class Tangible extends Effect {
        public Tangible() {
            super(WardType.TANGIBLE);
        }
    }

    public static final class Sanctuary extends Effect {
        public Sanctuary() {
            super(WardType.SANCTUARY);
        }
    }

    public static final class Bounty extends Effect {
        public Bounty() {
            super(WardType.BOUNTY);
        }
    }

    public static final class Immortal extends Effect {
        public Immortal() {
            super(WardType.IMMORTAL);
        }
    }

    public static final class Drain extends Effect {
        public Drain() {
            super(WardType.DRAIN);
        }
    }

    public static final class SoulChain extends Effect {
        public SoulChain() {
            super(WardType.SOUL_CHAIN);
        }
    }

    public static final class Stasis extends Effect {
        public Stasis() {
            super(WardType.STASIS);
        }
    }

    public static final class Maelstrom extends Effect {
        public Maelstrom() {
            super(WardType.MAELSTROM);
        }
    }

    public static final class Decay extends Effect {
        public Decay() {
            super(WardType.DECAY);
        }
    }

    public static final class Deflection extends Effect {
        public Deflection() {
            super(WardType.DEFLECTION);
        }
    }

    public static final class Silence extends Effect {
        public Silence() {
            super(WardType.SILENCE);
        }
    }

    public static final class Phasing extends Effect {
        public Phasing() {
            super(WardType.PHASING);
        }
    }

    private static class Effect implements IWardEffect {
        private final WardType type;
        private final Map<UUID, Long> cooldowns = new HashMap<>();

        Effect(WardType type) {
            this.type = type;
        }

        private boolean canUseFieldMana(WardContext context, int extraCost) {
            return extraCost <= 0 || WardManaService.consume(context.level(), context.pos(), context.sigil(), extraCost);
        }

        @Override
        public void tick(WardContext context) {
            SelariumCommonConfig.MvpWardConfig config = SelariumCommonConfig.mvpWard(type);
            if (WardEffectUtils.deactivateIfDisabled(context, config.enabled().get())) {
                return;
            }

            switch (type) {
                case FERTILITY -> tickFertility(context, config);
                case CITADEL -> tickTemporaryWall(context, config, SelariumBlocks.TEMPORARY_CITADEL_WALL.get());
                case CLOAKING -> tickCloaking(context, config);
                case ACCELERATING -> tickAccelerating(context, config);
                case EFFICIENCY -> tickEfficiency(context, config);
                case CRUSHING -> tickCrushing(context, config);
                case INVERSION -> tickInversion(context, config);
                case AQUALUNG -> tickAqualung(context, config);
                case TRANSMUTATION -> tickTransmutation(context, config);
                case TANGIBLE -> tickTemporaryWall(context, config, SelariumBlocks.TANGIBLE_BARRIER_BLOCK.get());
                case IMMORTAL -> tickImmortalMaintenance(context, config);
                case DRAIN -> tickDrain(context, config);
                case STASIS -> tickStasis(context, config);
                case MAELSTROM -> tickMaelstrom(context, config);
                case DECAY -> tickDecay(context, config);
                case DEFLECTION -> tickDeflection(context, config);
                case SILENCE -> tickSilence(context, config);
                case PHASING -> tickPhasing(context, config);
                default -> {
                }
            }
        }

        private void tickFertility(WardContext context, SelariumCommonConfig.MvpWardConfig config) {
            if (!WardAccessService.rulesFor(context.sigil()).affectPassiveMobs()) {
                context.sigil().recordWardDebug(0, 0, "passive mobs disabled by grimoire");
                return;
            }
            int maxAnimals = WardEffectUtils.entityCap(config.maxEntitiesPerCycle().get());
            if (maxAnimals <= 0) {
                return;
            }

            long now = context.level().getGameTime();
            List<Animal> animals = context.level().getEntitiesOfClass(Animal.class, new AABB(context.pos()).inflate(config.range().get()), animal ->
                    animal.isAlive() && !animal.isBaby() && animal.canFallInLove() && cooldowns.getOrDefault(animal.getUUID(), 0L) <= now);

            int attempts = 0;
            for (Animal animal : animals) {
                if (attempts >= maxAnimals) {
                    break;
                }
                Optional<Animal> partner = animals.stream()
                        .filter(other -> other != animal
                                && other.getClass() == animal.getClass()
                                && other.canFallInLove()
                                && cooldowns.getOrDefault(other.getUUID(), 0L) <= now)
                        .findFirst();
                if (partner.isEmpty() || !canUseFieldMana(context, config.manaCost().get())) {
                    continue;
                }

                animal.setInLove(null);
                partner.get().setInLove(null);
                long cooldownUntil = now + config.cooldownPerEntityTicks().get();
                cooldowns.put(animal.getUUID(), cooldownUntil);
                cooldowns.put(partner.get().getUUID(), cooldownUntil);
                attempts++;
            }
            context.sigil().recordWardDebug(attempts * 2, 0, attempts > 0 ? "love mode applied" : "no valid animal pair");
            cleanupCooldowns(now);
        }

        private void tickTemporaryWall(WardContext context, SelariumCommonConfig.MvpWardConfig config, Block block) {
            if (context.sigil().getTemporaryWardBlockCount() > 0) {
                context.sigil().recordWardDebug(0, context.sigil().getTemporaryWardBlockCount(), "temporary shell active");
                return;
            }
            if (!canUseFieldMana(context, config.manaCost().get())) {
                context.sigil().recordWardDebug(0, 0, "no mana");
                return;
            }

            int range = config.range().get();
            int height = Math.max(1, config.amplifier().get());
            int maxBlocks = WardEffectUtils.blockCap(config.maxBlocksPerCycle().get());
            boolean fullShell = block == SelariumBlocks.TANGIBLE_BARRIER_BLOCK.get();
            int placed = 0;
            for (int y = fullShell ? 0 : 1; y <= height && placed < maxBlocks; y++) {
                for (int x = -range; x <= range && placed < maxBlocks; x++) {
                    if (fullShell && (y == 0 || y == height)) {
                        for (int z = -range; z <= range && placed < maxBlocks; z++) {
                            placed += tryPlaceTemporary(context, block, context.pos().offset(x, y, z), config.optionA().get()) ? 1 : 0;
                        }
                    } else {
                        placed += tryPlaceTemporary(context, block, context.pos().offset(x, y, -range), config.optionA().get()) ? 1 : 0;
                        placed += tryPlaceTemporary(context, block, context.pos().offset(x, y, range), config.optionA().get()) ? 1 : 0;
                    }
                }
                if (!fullShell || (y > 0 && y < height)) {
                    for (int z = -range + 1; z < range && placed < maxBlocks; z++) {
                        placed += tryPlaceTemporary(context, block, context.pos().offset(-range, y, z), config.optionA().get()) ? 1 : 0;
                        placed += tryPlaceTemporary(context, block, context.pos().offset(range, y, z), config.optionA().get()) ? 1 : 0;
                    }
                }
            }
            context.sigil().recordWardDebug(0, placed, placed > 0 ? "temporary shell created" : "no replaceable positions");
        }

        private boolean tryPlaceTemporary(WardContext context, Block block, BlockPos pos, boolean allowReplacePlants) {
            if (!context.level().getWorldBorder().isWithinBounds(pos) || pos.getY() <= context.level().getMinBuildHeight() || pos.getY() >= context.level().getMaxBuildHeight()) {
                return false;
            }
            BlockState oldState = context.level().getBlockState(pos);
            BlockEntity blockEntity = context.level().getBlockEntity(pos);
            boolean replaceable = context.level().isLoaded(pos) && oldState.isAir();
            if (!replaceable || blockEntity != null || !oldState.getFluidState().isEmpty()) {
                return false;
            }

            context.level().setBlock(pos, block.defaultBlockState(), Block.UPDATE_ALL);
            context.sigil().trackTemporaryWardBlock(pos);
            return true;
        }

        private void tickCloaking(WardContext context, SelariumCommonConfig.MvpWardConfig config) {
            WardEffectUtils.applyOwnerEffect(context, config.range().get(), config.manaCost().get(), MobEffects.INVISIBILITY, config.effectDurationTicks().get(), 0, owner -> {
                int cleared = 0;
                if (config.optionA().get()) {
                    for (Mob mob : context.level().getEntitiesOfClass(Mob.class, new AABB(context.pos()).inflate(config.range().get()), mob -> mob.getTarget() == owner)) {
                        mob.setTarget(null);
                        cleared++;
                    }
                }
                context.sigil().recordWardDebug(cleared, 0, "owner cloaked");
            });
        }

        private void tickAccelerating(WardContext context, SelariumCommonConfig.MvpWardConfig config) {
            int maxBlocks = WardEffectUtils.blockCap(config.maxBlocksPerCycle().get());
            int grownBlocks = 0;
            int range = config.range().get();
            for (BlockPos pos : BlockPos.betweenClosed(context.pos().offset(-range, -1, -range), context.pos().offset(range, range, range))) {
                if (grownBlocks >= maxBlocks) {
                    break;
                }
                BlockState state = context.level().getBlockState(pos);
                if (!(state.getBlock() instanceof BonemealableBlock bonemealable)
                        || !bonemealable.isValidBonemealTarget(context.level(), pos, state, false)
                        || !bonemealable.isBonemealSuccess(context.level(), context.level().random, pos, state)
                        || !canUseFieldMana(context, config.manaCost().get())) {
                    continue;
                }
                bonemealable.performBonemeal(context.level(), context.level().random, pos, state);
                grownBlocks++;
            }

            int grownAnimals = 0;
            if (config.optionB().get() && config.maxEntitiesPerCycle().get() > 0 && WardAccessService.rulesFor(context.sigil()).affectPassiveMobs()) {
                for (AgeableMob baby : context.level().getEntitiesOfClass(AgeableMob.class, new AABB(context.pos()).inflate(range), mob -> mob.isAlive() && mob.isBaby())) {
                    if (grownAnimals >= WardEffectUtils.entityCap(config.maxEntitiesPerCycle().get())) {
                        break;
                    }
                    if (canUseFieldMana(context, config.manaCost().get())) {
                        baby.ageUp(60, true);
                        grownAnimals++;
                    }
                }
            }
            context.sigil().recordWardDebug(grownAnimals, grownBlocks, grownBlocks + grownAnimals > 0 ? "growth boosted" : "no bonemealable target");
        }

        private void tickEfficiency(WardContext context, SelariumCommonConfig.MvpWardConfig config) {
            int maxBlocks = WardEffectUtils.blockCap(config.maxBlocksPerCycle().get());
            if (maxBlocks <= 0 || !config.optionA().get()) {
                context.sigil().recordWardDebug(0, 0, "efficiency disabled");
                return;
            }

            int affected = 0;
            int range = config.range().get();
            for (BlockPos pos : BlockPos.betweenClosed(context.pos().offset(-range, -range, -range), context.pos().offset(range, range, range))) {
                if (affected >= maxBlocks) {
                    break;
                }
                BlockEntity blockEntity = context.level().getBlockEntity(pos);
                boolean eligible = blockEntity instanceof AbstractFurnaceBlockEntity furnace && FurnaceProgressAccess.canBoost(furnace)
                        || blockEntity instanceof ArcaneGrinderBlockEntity grinder && grinder.canBoostProgress();
                if (!eligible || !canUseFieldMana(context, config.manaCost().get())) continue;
                int boost = Math.max(1, config.amplifier().get());
                if (blockEntity instanceof AbstractFurnaceBlockEntity furnace) {
                    if (FurnaceProgressAccess.boost(furnace, boost)) affected++;
                } else if (blockEntity instanceof ArcaneGrinderBlockEntity grinder && grinder.boostProgress(boost)) {
                    affected++;
                }
            }
            context.sigil().recordWardDebug(0, affected, affected > 0 ? "machines boosted" : "no active cooking progress");
        }

        private void tickCrushing(WardContext context, SelariumCommonConfig.MvpWardConfig config) {
            int affected = WardEffectUtils.applyToInvaders(context, config.range().get(), config.affectPlayers().get(), config.affectBosses().get(), config.maxEntitiesPerCycle().get(), config.manaCost().get(), target -> {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, config.effectDurationTicks().get(), config.amplifier().get(), false, true, true));
                if (config.optionA().get()) {
                    target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, config.effectDurationTicks().get(), 0, false, true, true));
                }
                Vec3 motion = target.getDeltaMovement();
                double horizontalScale = Math.max(0.05D, Math.min(0.45D, config.strength().get() <= 0.0D ? 0.2D : 1.0D / (config.strength().get() + 1.0D)));
                double vertical = motion.y > 0.0D ? motion.y * 0.15D : motion.y;
                target.setDeltaMovement(motion.x * horizontalScale, vertical, motion.z * horizontalScale);
                target.hurtMarked = true;
                if (config.strength().get() > 0.0D) {
                    target.hurt(context.level().damageSources().magic(), Math.max(0.5F, config.strength().get().floatValue() * 0.5F));
                }
            });
            context.sigil().recordWardDebug(affected, 0, affected > 0 ? "crushing pressure applied" : "no invaders");
        }

        private void tickInversion(WardContext context, SelariumCommonConfig.MvpWardConfig config) {
            long now = context.level().getGameTime();
            int affected = 0;
            for (LivingEntity target : WardTargetingService.findInvaders(context.level(), context.pos(), context.sigil(), config.range().get(), true, config.affectPlayers().get(), config.affectBosses().get())) {
                if (affected >= WardEffectUtils.entityCap(config.maxEntitiesPerCycle().get())) {
                    break;
                }
                if (cooldowns.getOrDefault(target.getUUID(), 0L) > now || !canUseFieldMana(context, config.manaCost().get())) {
                    continue;
                }
                Vec3 motion = target.getDeltaMovement();
                target.setDeltaMovement(motion.x * 0.25D, Math.max(motion.y + config.strength().get(), config.strength().get()), motion.z * 0.25D);
                target.hurtMarked = true;
                cooldowns.put(target.getUUID(), now + config.cooldownPerEntityTicks().get());
                affected++;
            }
            context.sigil().recordWardDebug(affected, 0, affected > 0 ? "targets inverted upward" : "no invaders or cooldown");
            cleanupCooldowns(now);
        }

        private void tickAqualung(WardContext context, SelariumCommonConfig.MvpWardConfig config) {
            WardEffectUtils.applyOwnerEffect(context, config.range().get(), config.manaCost().get(), MobEffects.WATER_BREATHING, config.effectDurationTicks().get(), 0, owner -> {
                if (config.optionA().get()) {
                    owner.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, config.effectDurationTicks().get(), 0, false, true, true));
                }
                if (config.optionB().get()) {
                    owner.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, config.effectDurationTicks().get(), 0, false, true, true));
                }
                owner.setAirSupply(owner.getMaxAirSupply());
                context.sigil().recordWardDebug(1, 0, "owner breathing restored");
            });
        }

        private void tickTransmutation(WardContext context, SelariumCommonConfig.MvpWardConfig config) {
            int maxBlocks = WardEffectUtils.blockCap(config.maxBlocksPerCycle().get());
            int range = config.range().get();
            int transformedItems = 0;
            if (config.optionA().get() && config.maxEntitiesPerCycle().get() > 0) {
                for (ItemEntity itemEntity : context.level().getEntitiesOfClass(ItemEntity.class, new AABB(context.pos()).inflate(Math.max(1, Math.min(range, 3))), Entity::isAlive)) {
                    if (transformedItems >= WardEffectUtils.entityCap(config.maxEntitiesPerCycle().get())) {
                        break;
                    }
                    ItemStack stack = itemEntity.getItem();
                    Optional<Item> result = transmuteItem(stack.getItem());
                    if (stack.isEmpty() || result.isEmpty() || !canUseFieldMana(context, config.manaCost().get())) {
                        continue;
                    }
                    stack.shrink(1);
                    ItemStack output = new ItemStack(result.get());
                    if (stack.isEmpty()) {
                        itemEntity.discard();
                    }
                    context.level().addFreshEntity(new ItemEntity(context.level(), itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), output));
                    transformedItems++;
                }
            }

            int applied = 0;
            for (BlockPos pos : BlockPos.betweenClosed(context.pos().offset(-range, -range, -range), context.pos().offset(range, range, range))) {
                if (maxBlocks <= 0 || applied >= maxBlocks) {
                    break;
                }
                BlockState state = context.level().getBlockState(pos);
                Optional<BlockState> nextCopper = config.optionB().get()
                        ? WeatheringCopper.getNext(state.getBlock()).map(block -> block.withPropertiesOf(state))
                        : Optional.empty();
                if (nextCopper.isEmpty()) {
                    continue;
                }
                if (!canUseFieldMana(context, config.manaCost().get())) {
                    break;
                }
                context.level().setBlock(pos, nextCopper.get(), Block.UPDATE_ALL);
                applied++;
            }
            context.sigil().recordWardDebug(transformedItems, applied, transformedItems + applied > 0 ? "transmutation applied" : "no valid item/block");
        }

        private void tickDrain(WardContext context, SelariumCommonConfig.MvpWardConfig config) {
            Optional<ServerPlayer> owner = WardTargetingService.findOwnerInRange(context.level(), context.pos(), context.sigil(), Math.max(config.range().get(), 64));
            int[] generatedThisCycle = {0};
            int affected = WardEffectUtils.applyToInvaders(context, config.range().get(), config.affectPlayers().get(), config.affectBosses().get(), config.maxEntitiesPerCycle().get(), config.manaCost().get(), target -> {
                float damage = config.strength().get().floatValue();
                if (target.hurt(context.level().damageSources().magic(), damage)) {
                    int produced = Math.min(20, (int) Math.round(damage * config.chance().get()));
                    produced = Math.min(produced, Math.max(0, 100 - generatedThisCycle[0]));
                    generatedThisCycle[0] += context.sigil().addInternalMana(produced,
                            SelariumCommonConfig.AMBIENT_WARD_MAX_INTERNAL_BUFFER.get());
                    owner.ifPresent(player -> {
                        if (config.optionA().get()) {
                            player.heal(Math.max(0.5F, damage * 0.35F));
                        }
                        if (config.optionB().get()) {
                            player.getFoodData().eat(1, 0.1F);
                        }
                    });
                }
            });
            context.sigil().recordWardDebug(affected, 0, affected > 0 ? "life drained" : "no invaders");
        }

        private void tickImmortalMaintenance(WardContext context, SelariumCommonConfig.MvpWardConfig config) {
            int protectedCount = context.level().getEntitiesOfClass(LivingEntity.class, new AABB(context.pos()).inflate(config.range().get()), entity ->
                    canImmortalProtect(context, config, entity))
                    .size();
            if (protectedCount <= 0) {
                context.sigil().recordWardDebug(0, 0, "no entities in immortal field");
                return;
            }
            if (!canUseFieldMana(context, config.manaCost().get())) {
                context.sigil().recordWardDebug(protectedCount, 0, "no mana for immortal maintenance");
                return;
            }
            context.sigil().recordWardDebug(protectedCount, 0, "immortal field maintained");
        }

        private boolean canImmortalProtect(WardContext context, SelariumCommonConfig.MvpWardConfig config, LivingEntity entity) {
            if (!entity.isAlive() || entity.isSpectator()) {
                return false;
            }
            return WardAccessService.shouldAffectPositiveWard(context.sigil(), entity);
        }

        private void tickStasis(WardContext context, SelariumCommonConfig.MvpWardConfig config) {
            int affected = WardEffectUtils.applyToInvaders(context, config.range().get(), config.affectPlayers().get(), config.affectBosses().get(), config.maxEntitiesPerCycle().get(), config.manaCost().get(), target -> {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, config.effectDurationTicks().get(), config.amplifier().get(), false, true, true));
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, config.effectDurationTicks().get(), 0, false, true, true));
                target.setDeltaMovement(target.getDeltaMovement().scale(config.strength().get()));
                target.hurtMarked = true;
            });

            if (!config.optionA().get()) {
                context.sigil().recordWardDebug(affected, 0, affected > 0 ? "entities stilled" : "no invaders");
                return;
            }
            int projectiles = 0;
            for (Projectile projectile : context.level().getEntitiesOfClass(Projectile.class, new AABB(context.pos()).inflate(config.range().get()), Entity::isAlive)) {
                if (projectiles >= WardEffectUtils.entityCap(config.maxEntitiesPerCycle().get())) {
                    break;
                }
                if (!canUseFieldMana(context, config.manaCost().get())) {
                    break;
                }
                projectile.setDeltaMovement(projectile.getDeltaMovement().scale(config.strength().get()));
                projectile.hurtMarked = true;
                projectiles++;
            }
            context.sigil().recordWardDebug(affected + projectiles, 0, affected + projectiles > 0 ? "stasis applied" : "no targets");
        }

        private void tickMaelstrom(WardContext context, SelariumCommonConfig.MvpWardConfig config) {
            int affected = WardEffectUtils.applyToInvaders(context, config.range().get(), config.affectPlayers().get(), config.affectBosses().get(), config.maxEntitiesPerCycle().get(), config.manaCost().get(), target -> {
                target.hurt(context.level().damageSources().drown(), config.strength().get().floatValue());
                target.setAirSupply(Math.max(-20, target.getAirSupply() - 80));
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, config.effectDurationTicks().get(), config.amplifier().get(), false, true, true));
                Vec3 toCenter = Vec3.atCenterOf(context.pos()).subtract(target.position()).normalize().scale(0.08D);
                target.setDeltaMovement(target.getDeltaMovement().add(toCenter.x, -0.03D, toCenter.z));
                target.hurtMarked = true;
            });
            context.sigil().recordWardDebug(affected, 0, affected > 0 ? "maelstrom pressure applied" : "no invaders");
        }

        private void tickDecay(WardContext context, SelariumCommonConfig.MvpWardConfig config) {
            int affected = WardEffectUtils.applyToInvaders(context, config.range().get(), config.affectPlayers().get(), config.affectBosses().get(), config.maxEntitiesPerCycle().get(), config.manaCost().get(), target -> {
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, config.effectDurationTicks().get(), config.amplifier().get(), false, true, true));
                if (config.optionA().get()) {
                    target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, config.effectDurationTicks().get(), 0, false, true, true));
                }
            });
            context.sigil().recordWardDebug(affected, 0, affected > 0 ? "decay applied" : "no invaders");
        }

        private void tickDeflection(WardContext context, SelariumCommonConfig.MvpWardConfig config) {
            long now = context.level().getGameTime();
            int affected = 0;
            for (Projectile projectile : context.level().getEntitiesOfClass(Projectile.class, new AABB(context.pos()).inflate(config.range().get()), Entity::isAlive)) {
                if (affected >= WardEffectUtils.entityCap(config.maxEntitiesPerCycle().get())) {
                    break;
                }
                if (projectile.getOwner() instanceof LivingEntity owner && WardAccessService.isAlly(context.sigil(), owner)) {
                    continue;
                }
                if (cooldowns.getOrDefault(projectile.getUUID(), 0L) > now) {
                    continue;
                }
                if (!canUseFieldMana(context, config.manaCost().get())) {
                    break;
                }

                Vec3 movement = projectile.getDeltaMovement();
                double speed = Math.max(0.45D, movement.length());
                Entity shooter = projectile.getOwner();
                Vec3 direction = config.optionA().get() && shooter != null
                        ? shooter.position().add(0.0D, shooter.getBbHeight() * 0.5D, 0.0D).subtract(projectile.position()).normalize()
                        : safeReverse(movement);
                projectile.setDeltaMovement(direction.scale(speed));
                projectile.hurtMarked = true;
                cooldowns.put(projectile.getUUID(), now + config.cooldownPerEntityTicks().get());
                affected++;
            }
            context.sigil().recordWardDebug(affected, 0, affected > 0 ? "projectiles deflected" : "no projectile");
            cleanupCooldowns(now);
        }

        private void tickSilence(WardContext context, SelariumCommonConfig.MvpWardConfig config) {
            WardEffectUtils.applyToInvaders(context, config.range().get(), config.affectPlayers().get(), config.affectBosses().get(), config.maxEntitiesPerCycle().get(), config.manaCost().get(), target -> {
                if (config.optionA().get()) {
                    new ArrayList<>(target.getActiveEffects()).stream()
                            .filter(effect -> effect.getEffect().isBeneficial())
                            .forEach(effect -> target.removeEffect(effect.getEffect()));
                }
                if (config.optionB().get()) {
                    target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, config.effectDurationTicks().get(), 0, false, true, true));
                }
                target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, config.effectDurationTicks().get(), 0, false, true, true));
            });
        }

        private void tickPhasing(WardContext context, SelariumCommonConfig.MvpWardConfig config) {
            WardEffectUtils.applyOwnerEffect(context, config.range().get(), config.manaCost().get(), MobEffects.MOVEMENT_SPEED, Math.max(20, config.effectDurationTicks().get()), 0, owner -> {
                owner.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, Math.max(20, config.effectDurationTicks().get()), 0, false, true, true));
                owner.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, Math.max(20, config.effectDurationTicks().get()), 0, false, true, true));
                if (context.level().getGameTime() % 40 == 0) {
                    owner.displayClientMessage(Component.translatable("message.selarium.phasing.available"), true);
                }
                context.sigil().recordWardDebug(1, 0, "phasing support active");
            });
        }

        private Optional<Item> transmuteItem(Item item) {
            if (item == Items.COBBLESTONE) {
                return Optional.of(Items.STONE);
            }
            if (item == Items.STONE) {
                return Optional.of(Items.SMOOTH_STONE);
            }
            if (item == Items.SAND) {
                return Optional.of(Items.GLASS);
            }
            if (item == Items.ROTTEN_FLESH) {
                return Optional.of(Items.BONE_MEAL);
            }
            return Optional.empty();
        }

        private Vec3 safeReverse(Vec3 movement) {
            if (movement.lengthSqr() < 1.0E-4D) {
                return new Vec3(0.0D, 0.0D, 1.0D);
            }
            return movement.reverse().normalize();
        }

        private void cleanupCooldowns(long gameTime) {
            cooldowns.entrySet().removeIf(entry -> entry.getValue() + 20L < gameTime);
        }
    }
}
