package com.seleris.selarium.registry;

import com.seleris.selarium.Selarium;
import com.seleris.selarium.block.ArcaneCrystalClusterBlock;
import com.seleris.selarium.block.ArcaneGrinderBlock;
import com.seleris.selarium.block.ArcaneLeavesBlock;
import com.seleris.selarium.block.ArcanePetalsBlock;
import com.seleris.selarium.block.ArcaneSaplingBlock;
import com.seleris.selarium.block.ArcaneSigilBlock;
import com.seleris.selarium.block.BuddingArcaneCrystalBlock;
import com.seleris.selarium.block.ManaTankBlock;
import com.seleris.selarium.block.InscriptionBenchBlock;
import com.seleris.selarium.block.PhasingBlock;
import com.seleris.selarium.block.TemporaryWardBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.common.util.ForgeSoundType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class SelariumBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Selarium.MOD_ID);
    private static final SoundType ARCANE_CRYSTAL_SOUND = new ForgeSoundType(3.0F, 1.0F,
            SelariumSoundEvents.ARCANE_CRYSTAL_BREAK,
            SelariumSoundEvents.ARCANE_CRYSTAL_PLACE,
            SelariumSoundEvents.ARCANE_CRYSTAL_PLACE,
            SelariumSoundEvents.ARCANE_CRYSTAL_BREAK,
            SelariumSoundEvents.ARCANE_CRYSTAL_BREAK);

    public static final RegistryObject<Block> ARCANE_SIGIL = BLOCKS.register("arcane_sigil",
            () -> new ArcaneSigilBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(0.2F)
                    .sound(SoundType.AMETHYST)
                    .noOcclusion()));

    public static final RegistryObject<Block> MANA_TANK = BLOCKS.register("mana_tank",
            () -> new ManaTankBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .strength(3.0F, 6.0F)
                    .sound(SoundType.COPPER)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> ARCANE_GRINDER = BLOCKS.register("arcane_grinder",
            () -> new ArcaneGrinderBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(3.0F, 6.0F)
                    .sound(SoundType.COPPER)
                    .lightLevel(state -> state.getValue(ArcaneGrinderBlock.LIT) ? 9 : 0)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> INSCRIPTION_BENCH = BLOCKS.register("inscription_bench",
            () -> new InscriptionBenchBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(3.0F, 6.0F)
                    .sound(SoundType.COPPER)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> TEMPORARY_CITADEL_WALL = BLOCKS.register("temporary_citadel_wall",
            () -> new TemporaryWardBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(6.0F, 18.0F)
                    .sound(SoundType.AMETHYST)
                    .noLootTable()));

    public static final RegistryObject<Block> TANGIBLE_BARRIER_BLOCK = BLOCKS.register("tangible_barrier_block",
            () -> new TemporaryWardBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .strength(-1.0F, 3_600_000.0F)
                    .sound(SoundType.AMETHYST)
                    .noLootTable()
                    .noOcclusion()));

    public static final RegistryObject<Block> PHASING_BLOCK = BLOCKS.register("phasing_block",
            () -> new PhasingBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(2.0F, 6.0F)
                    .sound(SoundType.AMETHYST)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> ARCANE_GEODE_STONE = BLOCKS.register("arcane_geode_stone",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(2.0F, 6.0F)
                    .sound(SoundType.AMETHYST)
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> POLISHED_ARCANE_GEODE_STONE = BLOCKS.register("polished_arcane_geode_stone",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .strength(2.0F, 6.0F)
                    .sound(SoundType.AMETHYST)
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> ARCANE_LOG = BLOCKS.register("arcane_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LOG)
                    .mapColor(MapColor.COLOR_BLUE)
                    .strength(2.0F)
                    .sound(SoundType.WOOD)));

    public static final RegistryObject<Block> ARCANE_LEAVES = BLOCKS.register("arcane_leaves",
            () -> new ArcaneLeavesBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES)
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .strength(0.2F)
                    .randomTicks()
                    .noOcclusion()
                    .sound(SoundType.AZALEA_LEAVES)));

    public static final RegistryObject<Block> ARCANE_SAPLING = BLOCKS.register("arcane_sapling",
            () -> new ArcaneSaplingBlock(BlockBehaviour.Properties.copy(Blocks.OAK_SAPLING)
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .randomTicks()
                    .noCollission()
                    .noOcclusion()
                    .sound(SoundType.GRASS)));

    public static final RegistryObject<Block> ARCANE_PETALS = BLOCKS.register("arcane_petals",
            () -> new ArcanePetalsBlock(BlockBehaviour.Properties.copy(Blocks.PINK_PETALS)
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .noCollission()
                    .noOcclusion()
                    .sound(SoundType.PINK_PETALS)));

    public static final RegistryObject<Block> ARCANE_PLANKS = BLOCKS.register("arcane_planks",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)
                    .mapColor(MapColor.COLOR_BLUE)
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD)));

    public static final RegistryObject<Block> ARCANE_CRYSTAL_BLOCK = BLOCKS.register("arcane_crystal_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(1.8F, 6.0F)
                    .sound(ARCANE_CRYSTAL_SOUND)
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> ARCANE_BLOCK = BLOCKS.register("arcane_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(2.0F, 6.0F)
                    .sound(ARCANE_CRYSTAL_SOUND)
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> BUDDING_ARCANE_CRYSTAL = BLOCKS.register("budding_arcane_crystal",
            () -> new BuddingArcaneCrystalBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(2.2F, 6.0F)
                    .sound(ARCANE_CRYSTAL_SOUND)
                    .requiresCorrectToolForDrops()
                    .randomTicks()));

    public static final RegistryObject<Block> SMALL_ARCANE_CRYSTAL_BUD = BLOCKS.register("small_arcane_crystal_bud",
            () -> new ArcaneCrystalClusterBlock(7, 5, ArcaneCrystalClusterBlock.GrowthStage.SMALL,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PURPLE)
                            .strength(1.0F)
                            .sound(ARCANE_CRYSTAL_SOUND)
                            .lightLevel(state -> 2)
                            .noOcclusion()));

    public static final RegistryObject<Block> MEDIUM_ARCANE_CRYSTAL_BUD = BLOCKS.register("medium_arcane_crystal_bud",
            () -> new ArcaneCrystalClusterBlock(9, 4, ArcaneCrystalClusterBlock.GrowthStage.MEDIUM,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PURPLE)
                            .strength(1.0F)
                            .sound(ARCANE_CRYSTAL_SOUND)
                            .lightLevel(state -> 3)
                            .noOcclusion()));

    public static final RegistryObject<Block> LARGE_ARCANE_CRYSTAL_BUD = BLOCKS.register("large_arcane_crystal_bud",
            () -> new ArcaneCrystalClusterBlock(12, 3, ArcaneCrystalClusterBlock.GrowthStage.LARGE,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PURPLE)
                            .strength(1.0F)
                            .sound(ARCANE_CRYSTAL_SOUND)
                                    .noOcclusion()));

    public static final RegistryObject<Block> ARCANE_CRYSTAL_CLUSTER = BLOCKS.register("arcane_crystal_cluster",
            () -> new ArcaneCrystalClusterBlock(14, 2, ArcaneCrystalClusterBlock.GrowthStage.CLUSTER,
                    BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(1.0F)
                    .sound(ARCANE_CRYSTAL_SOUND)
                    .lightLevel(state -> 5)
                    .noOcclusion()));

    private SelariumBlocks() {
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
