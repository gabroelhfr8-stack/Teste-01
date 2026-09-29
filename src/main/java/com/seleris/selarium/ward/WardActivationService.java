package com.seleris.selarium.ward;

import com.seleris.selarium.blockentity.ArcaneSigilBlockEntity;
import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.dust.DustPurity;
import com.seleris.selarium.dust.DustType;
import com.seleris.selarium.grimoire.WardingGrimoireData;
import com.seleris.selarium.progression.AttunementProgressSavedData;
import com.seleris.selarium.progression.SelariumAdvancements;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class WardActivationService {
    private WardActivationService() {
    }

    public static ActivationResult activate(ArcaneSigilBlockEntity sigil, Level level, BlockPos pos, Player player, WardDefinition definition) {
        if (sigil.getWardCooldownRemainingTicks() > 0) {
            return ActivationResult.fail("message.selarium.ward.cooldown");
        }

        if (!isWardEnabled(definition.type())) {
            return ActivationResult.fail("message.selarium.ward.disabled");
        }

        DustType dustToConsume = null;
        if (definition.type() == WardType.AMBIENT_MANA && SelariumCommonConfig.AMBIENT_WARD_CONSUMES_DUST_ON_ACTIVATION.get()) {
            dustToConsume = DustType.fromSerializedName(SelariumCommonConfig.AMBIENT_WARD_REQUIRED_DUST_FOR_ACTIVATION.get()).orElse(DustType.FOCUS);
            if (!sigil.hasComponent(dustToConsume, DustPurity.BASIC)) {
                return ActivationResult.fail("message.selarium.ward.missing_activation_dust");
            }
        }

        if (!level.isClientSide && definition.activationCostValue() > 0 && !WardManaService.consume(level, pos, sigil, definition.activationCostValue())) {
            return ActivationResult.fail("message.selarium.ward.not_enough_mana");
        }

        if (dustToConsume != null) {
            sigil.removeOneComponent(dustToConsume, DustPurity.BASIC);
        }

        sigil.startWard(definition);
        if (level instanceof ServerLevel serverLevel && sigil.getOwner() != null) {
            AttunementProgressSavedData.get(serverLevel).record(sigil.getOwner(), definition.type(), definition.tier());
            boolean unlocked = WardingGrimoireData.unlockWardForOwner(serverLevel, sigil.getOwner(), definition.type());
            if (unlocked && player.getUUID().equals(sigil.getOwner())) {
                player.displayClientMessage(Component.translatable("message.selarium.grimoire.ward_recorded", Component.translatable(definition.type().getTranslationKey())), true);
            }
            if (player.getUUID().equals(sigil.getOwner())) {
                SelariumAdvancements.grant(player, "first_ward");
                if (definition.tier() == WardTier.REFINED) {
                    SelariumAdvancements.grant(player, "refined_ward");
                }
                if (AttunementProgressSavedData.get(serverLevel).distinctCount(sigil.getOwner()) >= WardType.values().length - 1) {
                    SelariumAdvancements.grant(player, "all_wards");
                }
            }
        }
        return ActivationResult.ok();
    }

    public static boolean isWardEnabled(WardType type) {
        return switch (type) {
            case AMBIENT_MANA -> SelariumCommonConfig.AMBIENT_WARD_ENABLED.get();
            case FEATHERWEIGHT -> SelariumCommonConfig.FEATHERWEIGHT_WARD_ENABLED.get();
            case GROUNDING -> SelariumCommonConfig.GROUNDING_WARD_ENABLED.get();
            case MAGNETISM -> SelariumCommonConfig.MAGNETISM_WARD_ENABLED.get();
            case BANISHMENT -> SelariumCommonConfig.BANISHMENT_WARD_ENABLED.get();
            case ECLIPSE -> SelariumCommonConfig.ECLIPSE_WARD_ENABLED.get();
            default -> {
                try {
                    yield SelariumCommonConfig.mvpWard(type).enabled().get();
                } catch (IllegalArgumentException ignored) {
                    yield true;
                }
            }
        };
    }

    public record ActivationResult(boolean success, String messageKey) {
        public static ActivationResult ok() {
            return new ActivationResult(true, "");
        }

        public static ActivationResult fail(String messageKey) {
            return new ActivationResult(false, messageKey);
        }
    }
}
