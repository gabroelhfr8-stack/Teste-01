package com.seleris.selarium.grimoire;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;

public final class SoulEssenceClassifier {
    private SoulEssenceClassifier() {
    }

    public static SoulEssenceCategory classify(Entity entity) {
        if (entity instanceof Player || entity instanceof AbstractVillager || entity instanceof Raider) {
            return SoulEssenceCategory.ANTHROPIC;
        }
        if (entity instanceof Enemy) {
            return SoulEssenceCategory.MONSTROUS;
        }
        if (entity instanceof Animal || entity instanceof TamableAnimal || entity instanceof AmbientCreature || entity instanceof WaterAnimal) {
            return SoulEssenceCategory.BESTIAL;
        }
        if (entity instanceof Mob) {
            return SoulEssenceCategory.BESTIAL;
        }
        return SoulEssenceCategory.UNKNOWN;
    }
}
