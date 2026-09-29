package com.seleris.selarium.gametest;

import com.seleris.selarium.Selarium;
import com.seleris.selarium.blockentity.ArcaneSigilBlockEntity;
import com.seleris.selarium.blockentity.ManaTankBlockEntity;
import com.seleris.selarium.dust.DustDefinition;
import com.seleris.selarium.dust.DustPurity;
import com.seleris.selarium.dust.DustType;
import com.seleris.selarium.registry.SelariumBlocks;
import com.seleris.selarium.registry.SelariumRecipeTypes;
import com.seleris.selarium.ward.WardActivationService;
import com.seleris.selarium.ward.WardContext;
import com.seleris.selarium.ward.WardDefinition;
import com.seleris.selarium.ward.WardDefinitions;
import com.seleris.selarium.ward.WardManager;
import com.seleris.selarium.ward.WardType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;

/**
 * Headless server tests, run in CI with {@code ./gradlew runGameTestServer}.
 * They exercise real registries, datapack parsing and the ward pipeline.
 */
@GameTestHolder(Selarium.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SelariumGameTests {
    private static final String EMPTY = "empty";
    private static final Set<String> BLOCKS_WITHOUT_LOOT = Set.of("temporary_citadel_wall", "tangible_barrier_block");

    private SelariumGameTests() {
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Selarium.MOD_ID, path);
    }

    private static <T> T requireBlockEntity(GameTestHelper helper, BlockPos pos, Class<T> type) {
        Object blockEntity = helper.getBlockEntity(pos);
        if (!type.isInstance(blockEntity)) {
            helper.fail("expected " + type.getSimpleName() + " at " + pos + " but found " + blockEntity);
        }
        return type.cast(blockEntity);
    }

    @GameTest(template = EMPTY)
    public static void everyWardHasADefinition(GameTestHelper helper) {
        for (WardType type : WardType.values()) {
            if (type != WardType.NONE) {
                helper.assertTrue(WardDefinitions.get(type).isPresent(), "missing definition for " + type);
            }
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void grindingRecipesLoad(GameTestHelper helper) {
        int count = helper.getLevel().getRecipeManager().getAllRecipesFor(SelariumRecipeTypes.ARCANE_GRINDING_TYPE.get()).size();
        helper.assertTrue(count >= 19, "expected at least 19 arcane grinding recipes, found " + count);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void worldgenLoads(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();
        helper.assertTrue(registries.registryOrThrow(Registries.CONFIGURED_FEATURE).containsKey(id("arcane_geode")),
                "configured feature selarium:arcane_geode did not load");
        helper.assertTrue(registries.registryOrThrow(Registries.PLACED_FEATURE).containsKey(id("arcane_geode")),
                "placed feature selarium:arcane_geode did not load");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void blockLootTablesLoad(GameTestHelper helper) {
        for (RegistryObject<Block> block : SelariumBlocks.BLOCKS.getEntries()) {
            String path = block.getId().getPath();
            if (BLOCKS_WITHOUT_LOOT.contains(path)) {
                continue;
            }
            LootTable table = helper.getLevel().getServer().getLootData().getLootTable(id("blocks/" + path));
            helper.assertTrue(table != LootTable.EMPTY, "missing or invalid loot table for block " + path);
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void ambientManaFeedsAdjacentTank(GameTestHelper helper) {
        BlockPos floor = new BlockPos(3, 0, 3);
        BlockPos sigilPos = floor.above();
        BlockPos tankPos = sigilPos.east();
        helper.setBlock(floor, Blocks.STONE);
        helper.setBlock(floor.east(), Blocks.STONE);
        helper.setBlock(sigilPos, SelariumBlocks.ARCANE_SIGIL.get());
        helper.setBlock(tankPos, SelariumBlocks.MANA_TANK.get());

        ArcaneSigilBlockEntity sigil = requireBlockEntity(helper, sigilPos, ArcaneSigilBlockEntity.class);
        ManaTankBlockEntity tank = requireBlockEntity(helper, tankPos, ManaTankBlockEntity.class);
        sigil.addComponent(new DustDefinition(DustType.ARCANE, DustPurity.BASIC), false);
        sigil.addComponent(new DustDefinition(DustType.FOCUS, DustPurity.BASIC), false);
        sigil.addComponent(new DustDefinition(DustType.FOCUS, DustPurity.BASIC), false);
        tank.setStoredMana(100);

        WardDefinition definition = WardDefinitions.resolve(sigil).orElse(null);
        helper.assertTrue(definition != null && definition.type() == WardType.AMBIENT_MANA,
                "Arcane + 2 Focus should resolve to Ambient Mana");

        Player player = helper.makeMockPlayer();
        BlockPos absolute = helper.absolutePos(sigilPos);
        WardActivationService.ActivationResult result =
                WardActivationService.activate(sigil, helper.getLevel(), absolute, player, definition);
        helper.assertTrue(result.success(), "activation should succeed but failed with " + result.messageKey());
        helper.assertTrue(sigil.isActive(), "sigil should be active after activation");
        int afterActivation = tank.getStoredMana();
        helper.assertTrue(afterActivation == 100 - definition.activationCostValue(),
                "activation should draw " + definition.activationCostValue() + " mana from the tank, tank holds " + afterActivation);

        WardManager.tick(new WardContext(helper.getLevel(), absolute, sigil.getBlockState(), sigil));
        helper.assertTrue(tank.getStoredMana() > afterActivation, "one ward cycle should push generated mana into the tank");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void sigilStateSurvivesSaveAndLoad(GameTestHelper helper) {
        BlockPos floor = new BlockPos(2, 0, 2);
        BlockPos sigilPos = floor.above();
        helper.setBlock(floor, Blocks.STONE);
        helper.setBlock(sigilPos, SelariumBlocks.ARCANE_SIGIL.get());
        ArcaneSigilBlockEntity original = requireBlockEntity(helper, sigilPos, ArcaneSigilBlockEntity.class);
        original.addComponent(new DustDefinition(DustType.ARCANE, DustPurity.BASIC), false);
        original.addComponent(new DustDefinition(DustType.AEGIS, DustPurity.REFINED), false);
        original.addInternalMana(42, 1000);

        CompoundTag saved = original.saveWithoutMetadata();
        ArcaneSigilBlockEntity restored = new ArcaneSigilBlockEntity(helper.absolutePos(sigilPos), original.getBlockState());
        restored.load(saved);

        helper.assertTrue(restored.getTotalComponents() == 2, "component count should survive, got " + restored.getTotalComponents());
        helper.assertTrue(restored.hasComponent(DustType.AEGIS, DustPurity.REFINED), "refined aegis should survive");
        helper.assertTrue(restored.getInternalManaBuffer() == 42, "internal mana should survive, got " + restored.getInternalManaBuffer());
        helper.succeed();
    }
}
