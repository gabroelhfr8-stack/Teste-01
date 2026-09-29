package com.seleris.selarium.registry;

import com.seleris.selarium.Selarium;
import com.seleris.selarium.grinder.menu.ArcaneGrinderMenu;
import com.seleris.selarium.inscription.InscriptionMenu;
import com.seleris.selarium.sigil.SigilMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class SelariumMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Selarium.MOD_ID);

    public static final RegistryObject<MenuType<ArcaneGrinderMenu>> ARCANE_GRINDER = MENUS.register("arcane_grinder",
            () -> IForgeMenuType.create((windowId, inv, data) -> new ArcaneGrinderMenu(windowId, inv, data.readBlockPos())));

    public static final RegistryObject<MenuType<InscriptionMenu>> INSCRIPTION_BENCH = MENUS.register("inscription_bench",
            () -> IForgeMenuType.create((windowId, inv, data) -> new InscriptionMenu(windowId, inv, data.readBlockPos())));

    public static final RegistryObject<MenuType<SigilMenu>> ARCANE_SIGIL = MENUS.register("arcane_sigil",
            () -> IForgeMenuType.create((windowId, inv, data) -> new SigilMenu(windowId, inv, data.readBlockPos())));

    private SelariumMenus() {
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
