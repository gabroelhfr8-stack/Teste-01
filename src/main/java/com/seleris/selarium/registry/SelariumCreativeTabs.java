package com.seleris.selarium.registry;

import com.seleris.selarium.Selarium;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class SelariumCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Selarium.MOD_ID);

    public static final RegistryObject<CreativeModeTab> SELARIUM = CREATIVE_TABS.register("selarium",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.selarium"))
                    .icon(() -> new ItemStack(SelariumItems.BASIC_ARCANE_DUST.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(SelariumItems.SELARIUM_CODEX.get());
                        output.accept(SelariumItems.WARDING_GRIMOIRE.get());
                        output.accept(SelariumItems.EMPTY_SCROLL.get());
                        output.accept(SelariumItems.INSCRIPTION_BENCH.get());
                        output.accept(SelariumItems.ARCANE_CRYSTAL.get());
                        output.accept(SelariumItems.BASIC_ARCANE_DUST.get());
                        output.accept(SelariumItems.BASIC_AEGIS_DUST.get());
                        output.accept(SelariumItems.REFINED_AEGIS_DUST.get());
                        output.accept(SelariumItems.BASIC_VITAL_DUST.get());
                        output.accept(SelariumItems.REFINED_VITAL_DUST.get());
                        output.accept(SelariumItems.BASIC_FOCUS_DUST.get());
                        output.accept(SelariumItems.REFINED_FOCUS_DUST.get());
                        output.accept(SelariumItems.BASIC_BINDING_DUST.get());
                        output.accept(SelariumItems.REFINED_BINDING_DUST.get());
                        output.accept(SelariumItems.BASIC_ECHO_DUST.get());
                        output.accept(SelariumItems.REFINED_ECHO_DUST.get());
                        output.accept(SelariumItems.BASIC_DENSITY_DUST.get());
                        output.accept(SelariumItems.REFINED_DENSITY_DUST.get());
                        output.accept(SelariumItems.BASIC_WARP_DUST.get());
                        output.accept(SelariumItems.REFINED_WARP_DUST.get());
                        output.accept(SelariumItems.BASIC_VEIL_DUST.get());
                        output.accept(SelariumItems.REFINED_VEIL_DUST.get());
                        output.accept(SelariumItems.BASIC_CHRONO_DUST.get());
                        output.accept(SelariumItems.REFINED_CHRONO_DUST.get());
                        output.accept(SelariumItems.ARCANE_SIGIL.get());
                        output.accept(SelariumItems.MANA_TANK.get());
                        output.accept(SelariumItems.ARCANE_GRINDER.get());
                        output.accept(SelariumItems.PHASING_BLOCK.get());
                        output.accept(SelariumItems.ARCANE_GEODE_STONE.get());
                        output.accept(SelariumItems.POLISHED_ARCANE_GEODE_STONE.get());
                        output.accept(SelariumItems.ARCANE_LOG.get());
                        output.accept(SelariumItems.ARCANE_LEAVES.get());
                        output.accept(SelariumItems.ARCANE_SAPLING.get());
                        output.accept(SelariumItems.ARCANE_PETALS.get());
                        output.accept(SelariumItems.ARCANE_PLANKS.get());
                        output.accept(SelariumItems.ARCANE_CRYSTAL_BLOCK.get());
                        output.accept(SelariumItems.ARCANE_BLOCK.get());
                        output.accept(SelariumItems.BUDDING_ARCANE_CRYSTAL.get());
                        output.accept(SelariumItems.SMALL_ARCANE_CRYSTAL_BUD.get());
                        output.accept(SelariumItems.MEDIUM_ARCANE_CRYSTAL_BUD.get());
                        output.accept(SelariumItems.LARGE_ARCANE_CRYSTAL_BUD.get());
                        output.accept(SelariumItems.ARCANE_CRYSTAL_CLUSTER.get());
                    })
                    .build());

    private SelariumCreativeTabs() {
    }

    public static void register(IEventBus eventBus) {
        CREATIVE_TABS.register(eventBus);
    }
}
