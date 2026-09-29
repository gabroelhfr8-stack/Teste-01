package com.seleris.selarium.ward.effect.utility;

import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.ward.WardAccessService;
import com.seleris.selarium.ward.WardArea;
import com.seleris.selarium.ward.WardContext;
import com.seleris.selarium.ward.WardFx;
import com.seleris.selarium.ward.WardType;
import com.seleris.selarium.ward.effect.ConfiguredWardEffect;
import com.seleris.selarium.ward.effect.EntityCooldowns;
import com.seleris.selarium.ward.effect.WardEffectUtils;
import net.minecraft.world.entity.animal.Animal;

import java.util.List;
import java.util.Optional;

/** Utility ward: puts nearby animals of the same species into love mode. */
public final class FertilityWardEffect extends ConfiguredWardEffect {
    private final EntityCooldowns cooldowns = new EntityCooldowns();

    public FertilityWardEffect() {
        super(WardType.FERTILITY);
    }

    @Override
    protected void apply(WardContext context, SelariumCommonConfig.MvpWardConfig config) {
        if (!WardAccessService.rulesFor(context.sigil()).affectPassiveMobs()) {
            context.sigil().recordWardDebug(0, 0, "passive mobs disabled by grimoire");
            return;
        }
        int maxAnimals = WardEffectUtils.entityCap(config.maxEntitiesPerCycle().get());
        if (maxAnimals <= 0) {
            return;
        }

        long now = context.level().getGameTime();
        List<Animal> animals = WardArea.of(context, config.range().get()).entities(Animal.class, animal ->
                animal.isAlive() && !animal.isBaby() && animal.canFallInLove() && cooldowns.ready(animal.getUUID(), now));

        int attempts = 0;
        for (Animal animal : animals) {
            if (attempts >= maxAnimals) {
                break;
            }
            if (!cooldowns.ready(animal.getUUID(), now)) {
                continue; // already paired earlier in this cycle
            }
            Optional<Animal> partner = animals.stream()
                    .filter(other -> other != animal
                            && other.getClass() == animal.getClass()
                            && other.canFallInLove()
                            && cooldowns.ready(other.getUUID(), now))
                    .findFirst();
            if (partner.isEmpty() || !canUseFieldMana(context, config.manaCost().get())) {
                continue;
            }

            animal.setInLove(null);
            partner.get().setInLove(null);
            long cooldownUntil = now + config.cooldownPerEntityTicks().get();
            cooldowns.start(animal.getUUID(), cooldownUntil);
            cooldowns.start(partner.get().getUUID(), cooldownUntil);
            WardFx.touch(context.level(), animal, WardType.FERTILITY, 3);
            WardFx.touch(context.level(), partner.get(), WardType.FERTILITY, 3);
            attempts++;
        }
        context.sigil().recordWardDebug(attempts * 2, 0, attempts > 0 ? "love mode applied" : "no valid animal pair");
        cooldowns.prune(now);
    }
}
