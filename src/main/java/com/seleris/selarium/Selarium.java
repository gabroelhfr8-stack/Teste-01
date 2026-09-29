package com.seleris.selarium;

import com.seleris.selarium.command.SelariumCommands;
import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.grimoire.WardingGrimoireEventHandler;
import com.seleris.selarium.mana.capability.ManaCapability;
import com.seleris.selarium.mana.capability.ManaEvents;
import com.seleris.selarium.network.SelariumNetwork;
import com.seleris.selarium.registry.SelariumBlockEntities;
import com.seleris.selarium.registry.SelariumBlocks;
import com.seleris.selarium.registry.SelariumCreativeTabs;
import com.seleris.selarium.registry.SelariumFeatures;
import com.seleris.selarium.registry.SelariumItems;
import com.seleris.selarium.registry.SelariumMenus;
import com.seleris.selarium.registry.SelariumRecipeTypes;
import com.seleris.selarium.registry.SelariumSoundEvents;
import com.seleris.selarium.ward.WardEventHandler;
import com.seleris.selarium.ward.WardProjectionEvents;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Selarium.MOD_ID)
public class Selarium {
    public static final String MOD_ID = "selarium";

    public Selarium() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        SelariumSoundEvents.register(modEventBus);
        SelariumBlocks.register(modEventBus);
        SelariumBlockEntities.register(modEventBus);
        SelariumItems.register(modEventBus);
        SelariumCreativeTabs.register(modEventBus);
        SelariumFeatures.register(modEventBus);
        SelariumMenus.register(modEventBus);
        SelariumRecipeTypes.register(modEventBus);
        modEventBus.addListener(ManaCapability::register);
        SelariumNetwork.register();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SelariumCommonConfig.SPEC);

        MinecraftForge.EVENT_BUS.register(ManaEvents.class);
        MinecraftForge.EVENT_BUS.register(WardEventHandler.class);
        MinecraftForge.EVENT_BUS.register(WardProjectionEvents.class);
        MinecraftForge.EVENT_BUS.register(WardingGrimoireEventHandler.class);
        MinecraftForge.EVENT_BUS.addListener(SelariumCommands::onRegisterCommands);
    }
}
