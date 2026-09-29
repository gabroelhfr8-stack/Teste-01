package com.seleris.selarium.registry;

import com.seleris.selarium.Selarium;
import com.seleris.selarium.dust.DustDefinition;
import com.seleris.selarium.dust.DustPurity;
import com.seleris.selarium.dust.DustType;
import com.seleris.selarium.item.ArcaneDustItem;
import com.seleris.selarium.item.DustItem;
import com.seleris.selarium.item.SelariumCodexItem;
import com.seleris.selarium.item.WardingGrimoireItem;
import com.seleris.selarium.item.AttunementScrollItem;
import com.seleris.selarium.item.WardScrollItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class SelariumItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Selarium.MOD_ID);

    public static final RegistryObject<Item> ARCANE_DUST = registerLegacyDust("arcane_dust", DustType.ARCANE);
    public static final RegistryObject<Item> AEGIS_DUST = registerLegacyDust("aegis_dust", DustType.AEGIS);
    public static final RegistryObject<Item> VITAL_DUST = registerLegacyDust("vital_dust", DustType.VITAL);
    public static final RegistryObject<Item> FOCUS_DUST = registerLegacyDust("focus_dust", DustType.FOCUS);
    public static final RegistryObject<Item> BINDING_DUST = registerLegacyDust("binding_dust", DustType.BINDING);

    public static final RegistryObject<Item> ARCANE_CRYSTAL = ITEMS.register("arcane_crystal", () -> new Item(defaultProperties()));
    public static final RegistryObject<Item> SELARIUM_CODEX = ITEMS.register("selarium_codex", () -> new SelariumCodexItem(defaultProperties().stacksTo(1)));
    public static final RegistryObject<Item> WARDING_GRIMOIRE = ITEMS.register("warding_grimoire", () -> new WardingGrimoireItem(defaultProperties().stacksTo(1)));
    public static final RegistryObject<Item> EMPTY_SCROLL = ITEMS.register("empty_scroll", () -> new Item(defaultProperties()));
    public static final RegistryObject<Item> ATTUNEMENT_SCROLL = ITEMS.register("attunement_scroll", () -> new AttunementScrollItem(defaultProperties().stacksTo(1)));
    public static final RegistryObject<Item> WARD_SCROLL = ITEMS.register("ward_scroll", () -> new WardScrollItem(defaultProperties().stacksTo(1)));

    public static final RegistryObject<Item> BASIC_ARCANE_DUST = registerDust("basic_arcane_dust", DustType.ARCANE, DustPurity.BASIC);
    public static final RegistryObject<Item> BASIC_AEGIS_DUST = registerDust("basic_aegis_dust", DustType.AEGIS, DustPurity.BASIC);
    public static final RegistryObject<Item> REFINED_AEGIS_DUST = registerDust("refined_aegis_dust", DustType.AEGIS, DustPurity.REFINED);
    public static final RegistryObject<Item> BASIC_VITAL_DUST = registerDust("basic_vital_dust", DustType.VITAL, DustPurity.BASIC);
    public static final RegistryObject<Item> REFINED_VITAL_DUST = registerDust("refined_vital_dust", DustType.VITAL, DustPurity.REFINED);
    public static final RegistryObject<Item> BASIC_FOCUS_DUST = registerDust("basic_focus_dust", DustType.FOCUS, DustPurity.BASIC);
    public static final RegistryObject<Item> REFINED_FOCUS_DUST = registerDust("refined_focus_dust", DustType.FOCUS, DustPurity.REFINED);
    public static final RegistryObject<Item> BASIC_BINDING_DUST = registerDust("basic_binding_dust", DustType.BINDING, DustPurity.BASIC);
    public static final RegistryObject<Item> REFINED_BINDING_DUST = registerDust("refined_binding_dust", DustType.BINDING, DustPurity.REFINED);
    public static final RegistryObject<Item> BASIC_ECHO_DUST = registerDust("basic_echo_dust", DustType.ECHO, DustPurity.BASIC);
    public static final RegistryObject<Item> REFINED_ECHO_DUST = registerDust("refined_echo_dust", DustType.ECHO, DustPurity.REFINED);
    public static final RegistryObject<Item> BASIC_DENSITY_DUST = registerDust("basic_density_dust", DustType.DENSITY, DustPurity.BASIC);
    public static final RegistryObject<Item> REFINED_DENSITY_DUST = registerDust("refined_density_dust", DustType.DENSITY, DustPurity.REFINED);
    public static final RegistryObject<Item> BASIC_WARP_DUST = registerDust("basic_warp_dust", DustType.WARP, DustPurity.BASIC);
    public static final RegistryObject<Item> REFINED_WARP_DUST = registerDust("refined_warp_dust", DustType.WARP, DustPurity.REFINED);
    public static final RegistryObject<Item> BASIC_VEIL_DUST = registerDust("basic_veil_dust", DustType.VEIL, DustPurity.BASIC);
    public static final RegistryObject<Item> REFINED_VEIL_DUST = registerDust("refined_veil_dust", DustType.VEIL, DustPurity.REFINED);
    public static final RegistryObject<Item> BASIC_CHRONO_DUST = registerDust("basic_chrono_dust", DustType.CHRONO, DustPurity.BASIC);
    public static final RegistryObject<Item> REFINED_CHRONO_DUST = registerDust("refined_chrono_dust", DustType.CHRONO, DustPurity.REFINED);

    public static final RegistryObject<Item> ARCANE_SIGIL = ITEMS.register("arcane_sigil",
            () -> new BlockItem(SelariumBlocks.ARCANE_SIGIL.get(), defaultProperties()));

    public static final RegistryObject<Item> MANA_TANK = ITEMS.register("mana_tank",
            () -> new BlockItem(SelariumBlocks.MANA_TANK.get(), defaultProperties()));

    public static final RegistryObject<Item> ARCANE_GRINDER = ITEMS.register("arcane_grinder",
            () -> new BlockItem(SelariumBlocks.ARCANE_GRINDER.get(), defaultProperties()));

    public static final RegistryObject<Item> INSCRIPTION_BENCH = ITEMS.register("inscription_bench",
            () -> new BlockItem(SelariumBlocks.INSCRIPTION_BENCH.get(), defaultProperties()));

    public static final RegistryObject<Item> PHASING_BLOCK = ITEMS.register("phasing_block",
            () -> new BlockItem(SelariumBlocks.PHASING_BLOCK.get(), defaultProperties()));

    public static final RegistryObject<Item> ARCANE_GEODE_STONE = ITEMS.register("arcane_geode_stone",
            () -> new BlockItem(SelariumBlocks.ARCANE_GEODE_STONE.get(), defaultProperties()));

    public static final RegistryObject<Item> POLISHED_ARCANE_GEODE_STONE = ITEMS.register("polished_arcane_geode_stone",
            () -> new BlockItem(SelariumBlocks.POLISHED_ARCANE_GEODE_STONE.get(), defaultProperties()));

    public static final RegistryObject<Item> ARCANE_LOG = ITEMS.register("arcane_log",
            () -> new BlockItem(SelariumBlocks.ARCANE_LOG.get(), defaultProperties()));

    public static final RegistryObject<Item> ARCANE_LEAVES = ITEMS.register("arcane_leaves",
            () -> new BlockItem(SelariumBlocks.ARCANE_LEAVES.get(), defaultProperties()));

    public static final RegistryObject<Item> ARCANE_SAPLING = ITEMS.register("arcane_sapling",
            () -> new BlockItem(SelariumBlocks.ARCANE_SAPLING.get(), defaultProperties()));

    public static final RegistryObject<Item> ARCANE_PETALS = ITEMS.register("arcane_petals",
            () -> new BlockItem(SelariumBlocks.ARCANE_PETALS.get(), defaultProperties()));

    public static final RegistryObject<Item> ARCANE_PLANKS = ITEMS.register("arcane_planks",
            () -> new BlockItem(SelariumBlocks.ARCANE_PLANKS.get(), defaultProperties()));

    public static final RegistryObject<Item> ARCANE_CRYSTAL_BLOCK = ITEMS.register("arcane_crystal_block",
            () -> new BlockItem(SelariumBlocks.ARCANE_CRYSTAL_BLOCK.get(), defaultProperties()));

    public static final RegistryObject<Item> ARCANE_BLOCK = ITEMS.register("arcane_block",
            () -> new BlockItem(SelariumBlocks.ARCANE_BLOCK.get(), defaultProperties()));

    public static final RegistryObject<Item> BUDDING_ARCANE_CRYSTAL = ITEMS.register("budding_arcane_crystal",
            () -> new BlockItem(SelariumBlocks.BUDDING_ARCANE_CRYSTAL.get(), defaultProperties()));

    public static final RegistryObject<Item> SMALL_ARCANE_CRYSTAL_BUD = ITEMS.register("small_arcane_crystal_bud",
            () -> new BlockItem(SelariumBlocks.SMALL_ARCANE_CRYSTAL_BUD.get(), defaultProperties()));

    public static final RegistryObject<Item> MEDIUM_ARCANE_CRYSTAL_BUD = ITEMS.register("medium_arcane_crystal_bud",
            () -> new BlockItem(SelariumBlocks.MEDIUM_ARCANE_CRYSTAL_BUD.get(), defaultProperties()));

    public static final RegistryObject<Item> LARGE_ARCANE_CRYSTAL_BUD = ITEMS.register("large_arcane_crystal_bud",
            () -> new BlockItem(SelariumBlocks.LARGE_ARCANE_CRYSTAL_BUD.get(), defaultProperties()));

    public static final RegistryObject<Item> ARCANE_CRYSTAL_CLUSTER = ITEMS.register("arcane_crystal_cluster",
            () -> new BlockItem(SelariumBlocks.ARCANE_CRYSTAL_CLUSTER.get(), defaultProperties()));

    private SelariumItems() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    private static Item.Properties defaultProperties() {
        return new Item.Properties();
    }

    private static RegistryObject<Item> registerLegacyDust(String name, DustType type) {
        return ITEMS.register(name, () -> createDustItem(type, DustPurity.BASIC));
    }

    private static RegistryObject<Item> registerDust(String name, DustType type, DustPurity purity) {
        return ITEMS.register(name, () -> createDustItem(type, purity));
    }

    private static Item createDustItem(DustType type, DustPurity purity) {
        DustDefinition definition = new DustDefinition(type, purity);
        if (type == DustType.ARCANE) {
            return new ArcaneDustItem(definition, defaultProperties());
        }
        return new DustItem(definition, defaultProperties());
    }
}
