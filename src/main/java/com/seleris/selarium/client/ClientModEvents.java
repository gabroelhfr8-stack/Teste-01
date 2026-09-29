package com.seleris.selarium.client;

import com.seleris.selarium.Selarium;
import com.seleris.selarium.client.gui.ArcaneGrinderScreen;
import com.seleris.selarium.client.gui.InscriptionScreen;
import com.seleris.selarium.client.gui.SigilScreen;
import com.seleris.selarium.client.hud.ManaHudOverlay;
import com.seleris.selarium.client.render.ManaTankRenderer;
import com.seleris.selarium.client.sigil.ArcaneSigilRenderer;
import com.seleris.selarium.inscription.ScrollData;
import com.seleris.selarium.registry.SelariumBlockEntities;
import com.seleris.selarium.registry.SelariumBlocks;
import com.seleris.selarium.registry.SelariumItems;
import com.seleris.selarium.registry.SelariumMenus;
import com.seleris.selarium.ward.WardStyles;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Selarium.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void onRegisterGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("mana_hud", ManaHudOverlay::render);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(SelariumMenus.ARCANE_GRINDER.get(), ArcaneGrinderScreen::new);
            MenuScreens.register(SelariumMenus.INSCRIPTION_BENCH.get(), InscriptionScreen::new);
            MenuScreens.register(SelariumMenus.ARCANE_SIGIL.get(), SigilScreen::new);

            // glass, energy and crystal blocks
            ItemBlockRenderTypes.setRenderLayer(SelariumBlocks.MANA_TANK.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(SelariumBlocks.TANGIBLE_BARRIER_BLOCK.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(SelariumBlocks.PHASING_BLOCK.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(SelariumBlocks.ARCANE_GRINDER.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(SelariumBlocks.INSCRIPTION_BENCH.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(SelariumBlocks.SMALL_ARCANE_CRYSTAL_BUD.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(SelariumBlocks.MEDIUM_ARCANE_CRYSTAL_BUD.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(SelariumBlocks.LARGE_ARCANE_CRYSTAL_BUD.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(SelariumBlocks.ARCANE_CRYSTAL_CLUSTER.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(SelariumBlocks.ARCANE_LEAVES.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(SelariumBlocks.ARCANE_SAPLING.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(SelariumBlocks.ARCANE_PETALS.get(), RenderType.cutout());
        });
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(SelariumBlockEntities.ARCANE_SIGIL.get(), ArcaneSigilRenderer::new);
        event.registerBlockEntityRenderer(SelariumBlockEntities.MANA_TANK.get(), ManaTankRenderer::new);
    }

    /** The wax seal of a Ward Scroll takes the colour of the ward it carries. */
    @SubscribeEvent
    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> tintIndex == 0
                        ? 0xFF000000 | WardStyles.primary(ScrollData.ward(stack))
                        : 0xFFFFFFFF,
                SelariumItems.WARD_SCROLL.get());
    }
}
