package com.seleris.selarium.registry;

import com.seleris.selarium.Selarium;
import com.seleris.selarium.blockentity.ArcaneGrinderBlockEntity;
import com.seleris.selarium.blockentity.ArcaneSigilBlockEntity;
import com.seleris.selarium.blockentity.ManaTankBlockEntity;
import com.seleris.selarium.blockentity.InscriptionBenchBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class SelariumBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Selarium.MOD_ID);

    public static final RegistryObject<BlockEntityType<ArcaneSigilBlockEntity>> ARCANE_SIGIL = BLOCK_ENTITIES.register("arcane_sigil",
            () -> BlockEntityType.Builder.of(ArcaneSigilBlockEntity::new, SelariumBlocks.ARCANE_SIGIL.get()).build(null));

    public static final RegistryObject<BlockEntityType<ManaTankBlockEntity>> MANA_TANK = BLOCK_ENTITIES.register("mana_tank",
            () -> BlockEntityType.Builder.of(ManaTankBlockEntity::new, SelariumBlocks.MANA_TANK.get()).build(null));

    public static final RegistryObject<BlockEntityType<ArcaneGrinderBlockEntity>> ARCANE_GRINDER = BLOCK_ENTITIES.register("arcane_grinder",
            () -> BlockEntityType.Builder.of(ArcaneGrinderBlockEntity::new, SelariumBlocks.ARCANE_GRINDER.get()).build(null));

    public static final RegistryObject<BlockEntityType<InscriptionBenchBlockEntity>> INSCRIPTION_BENCH = BLOCK_ENTITIES.register("inscription_bench",
            () -> BlockEntityType.Builder.of(InscriptionBenchBlockEntity::new, SelariumBlocks.INSCRIPTION_BENCH.get()).build(null));

    private SelariumBlockEntities() {
    }

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
