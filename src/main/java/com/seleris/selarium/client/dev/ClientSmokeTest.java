package com.seleris.selarium.client.dev;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import com.seleris.selarium.Selarium;
import com.seleris.selarium.block.ArcaneGrinderBlock;
import com.seleris.selarium.blockentity.ArcaneSigilBlockEntity;
import com.seleris.selarium.blockentity.ManaTankBlockEntity;
import com.seleris.selarium.client.SelariumClientHooks;
import com.seleris.selarium.dust.DustDefinition;
import com.seleris.selarium.dust.DustPurity;
import com.seleris.selarium.dust.DustType;
import com.seleris.selarium.grimoire.WardingRuleSet;
import com.seleris.selarium.inscription.ScrollData;
import com.seleris.selarium.registry.SelariumBlocks;
import com.seleris.selarium.registry.SelariumItems;
import com.seleris.selarium.ward.WardDefinition;
import com.seleris.selarium.ward.WardDefinitions;
import com.seleris.selarium.ward.WardProjectionSavedData;
import com.seleris.selarium.ward.WardRequirement;
import com.seleris.selarium.ward.WardType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Drives a real client in CI: creates a flat world, builds a small showcase (active sigils, machines,
 * crystals), flies the camera around and writes screenshots to {@code <gameDir>/smoke}.
 *
 * <p>Inert unless the JVM is started with {@code -Dselarium.smoketest=true} ({@code ./gradlew runClient -Psmoketest}).
 * Any exception while rendering crashes the client, which is exactly what the job is there to catch;
 * problems in the scripted steps are logged as {@code SMOKE_FAIL} and picked up by {@code tools/dump_smoke.py}.
 */
@Mod.EventBusSubscriber(modid = Selarium.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientSmokeTest {
    private static final boolean ENABLED = Boolean.getBoolean("selarium.smoketest");
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final long DEADLINE_MS = 12 * 60_000L;
    private static final double EYE_HEIGHT = 1.62D;
    private static final long NOON = 6000L;
    private static final long MIDNIGHT = 18000L;

    private static final long STARTED_AT = System.currentTimeMillis();
    private static final List<Step> STEPS = steps();

    private static boolean worldRequested;
    private static boolean finished;
    private static volatile boolean failed;
    private static volatile int base;
    private static int step = -1;
    private static int settle;
    private static String pendingShot;

    private ClientSmokeTest() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (!ENABLED || finished || event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        try {
            if (System.currentTimeMillis() - STARTED_AT > DEADLINE_MS) {
                LOGGER.error("SMOKE_FAIL deadline exceeded at step {}", step);
                failed = true;
                finish(mc);
                return;
            }
            if (!worldRequested) {
                if (mc.getOverlay() == null && mc.level == null) {
                    LOGGER.info("SMOKE resources loaded, creating world");
                    requestWorld(mc);
                }
                return;
            }
            if (mc.level == null || mc.player == null || mc.getSingleplayerServer() == null) {
                return;
            }
            if (step < 0 && (mc.screen != null || !mc.levelRenderer.hasRenderedAllChunks())) {
                return;
            }
            if (settle > 0) {
                settle--;
                return;
            }
            advance(mc);
        } catch (Throwable t) {
            LOGGER.error("SMOKE_FAIL step {}", step, t);
            failed = true;
            finish(mc);
        }
    }

    private static void requestWorld(Minecraft mc) {
        worldRequested = true;
        GameRules rules = new GameRules();
        rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false, null);
        rules.getRule(GameRules.RULE_DAYLIGHT).set(false, null);
        rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false, null);
        LevelSettings settings = new LevelSettings("Selarium Smoke", GameType.CREATIVE, false, Difficulty.EASY, true,
                rules, WorldDataConfiguration.DEFAULT);
        mc.createWorldOpenFlows().createFreshLevel("selarium_smoke_" + (System.currentTimeMillis() % 100000L), settings,
                new WorldOptions(20260929L, false, false),
                registries -> registries.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT)
                        .value().createWorldDimensions());
    }

    private static void advance(Minecraft mc) throws Exception {
        if (pendingShot != null) {
            shoot(mc, pendingShot);
            pendingShot = null;
        }
        step++;
        if (step >= STEPS.size()) {
            finish(mc);
            return;
        }
        Step next = STEPS.get(step);
        LOGGER.info("SMOKE step {} {}", step, next.name());
        next.action().run(mc);
        pendingShot = next.shot();
        settle = next.settleTicks();
    }

    private static void finish(Minecraft mc) {
        finished = true;
        LOGGER.info(failed ? "SMOKE_FAILED" : "SMOKE_DONE");
        mc.stop();
    }

    private static void shoot(Minecraft mc, String name) throws IOException {
        Path dir = mc.gameDirectory.toPath().resolve("smoke");
        Files.createDirectories(dir);
        try (NativeImage image = Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
            image.writeToFile(dir.resolve(name + ".png").toFile());
            LOGGER.info("SMOKE shot {} {}x{} particles={}", name, image.getWidth(), image.getHeight(),
                    mc.particleEngine.countParticles());
        }
    }

    // ---- scripted steps -------------------------------------------------------------------------------------

    private static List<Step> steps() {
        List<Step> steps = new ArrayList<>();
        steps.add(new Step("build stage", null, 120, mc -> {
            mc.options.hideGui = true;
            onServer(mc, ClientSmokeTest::buildStage);
        }));
        steps.add(new Step("wards overview (day)", "01_overview_day", 80, camera(0, 22, -46, 0, 1, 2)));
        steps.add(new Step("bulwark close (day)", "02_bulwark_close_day", 60, camera(-16.8, 3.2, 3.2, -20, 0.8, 0)));
        steps.add(new Step("bulwark from above (day)", "03_bulwark_top_day", 60, camera(-20, 6, 0.01, -20, 0, 0)));
        steps.add(new Step("night falls", null, 20, mc -> time(mc, MIDNIGHT)));
        steps.add(new Step("bulwark close (night)", "04_bulwark_close_night", 80, camera(-16.8, 3.2, 3.2, -20, 0.8, 0)));
        steps.add(new Step("bulwark from above (night)", "05_bulwark_top_night", 40, camera(-20, 6, 0.01, -20, 0, 0)));
        steps.add(new Step("banishment (night)", "06_banishment_night", 60, camera(6, 3.5, 7, 0, 1.5, 0)));
        steps.add(new Step("soft shell (night)", "07_soft_shell_night", 60, camera(-13, 3.5, -19, -20, 2, -26)));
        steps.add(new Step("runes shell (night)", "08_runes_shell_night", 60, camera(7, 3.5, -19, 0, 2, -26)));
        steps.add(new Step("sigil close-up (night)", "19_sigil_closeup_night", 40, camera(-18.6, 1.5, 1.7, -20, 0.4, 0)));
        steps.add(new Step("citadel (night)", "09_citadel_night", 60, camera(20, 9, -22, 20, 4, 0)));
        steps.add(new Step("tangible barrier (night)", "20_tangible_night", 60, camera(-26, 3, 28, -26, 2, 22)));
        steps.add(new Step("wards overview (night)", "10_overview_night", 60, camera(0, 22, -46, 0, 1, 2)));
        steps.add(new Step("workshop (night)", "11_workshop_night", 60, camera(-3, 8, 32, -3, 1, 16)));
        steps.add(new Step("inactive sigil (night)", "12_inactive_sigil_night", 40, camera(-3, 1.9, 13.6, -3, 0, 11)));
        steps.add(new Step("scroll projection (night)", "18_projection_night", 60, camera(23, 3.5, 22, 30, 1.5, 14)));
        steps.add(new Step("day returns", null, 20, mc -> time(mc, NOON)));
        steps.add(new Step("workshop (day)", "13_workshop_day", 80, camera(-3, 8, 32, -3, 1, 16)));
        steps.add(new Step("inactive sigil (day)", "14_inactive_sigil_day", 40, camera(-3, 1.9, 13.6, -3, 0, 11)));
        steps.add(new Step("machines close", "15_machines_close_day", 40, camera(-5, 2.6, 22.5, -5, 0.8, 16)));
        steps.add(new Step("grinders close", "21_grinders_close_day", 40, camera(-9, 1.7, 18.6, -9, 0.7, 16)));
        steps.add(new Step("tanks close", "22_tanks_close_day", 40, camera(-3, 1.6, 18.4, -3, 0.7, 16)));
        steps.add(new Step("crystals and materials", "16_materials_day", 40, camera(0, 2.4, 24, 0, 0.6, 20)));
        steps.add(new Step("hud", "17_hud_day", 40, mc -> {
            mc.options.hideGui = false;
            camera(-5, 2.6, 22.5, -5, 0.8, 16).run(mc);
        }));

        // screens
        steps.add(new Step("codex", "20_gui_codex_start", 20, mc -> SelariumClientHooks.openCodex()));
        steps.add(new Step("codex dusts", "21_gui_codex_dusts", 6, mc -> pressRight(mc, 4)));
        steps.add(new Step("codex sigils", "22_gui_codex_sigils", 6, mc -> pressRight(mc, 1)));
        steps.add(new Step("codex wards", "23_gui_codex_wards", 6, mc -> pressRight(mc, 4)));
        steps.add(new Step("codex atlas", "28_gui_codex_atlas", 10, mc -> {
            pressRight(mc, 1);
            int left = (mc.screen.width - 300) / 2 + 112;
            int top = (mc.screen.height - 210) / 2 + 46;
            mc.screen.mouseClicked(left + 3 * 22 + 5, top + 22 + 5, 0);
        }));
        steps.add(new Step("grimoire", "24_gui_grimoire", 20, mc ->
                SelariumClientHooks.openWardingGrimoire(WardingRuleSet.defaults(mc.player.getUUID()))));
        steps.add(new Step("close screen", null, 10, mc -> mc.player.closeContainer()));
        // a container must be fully closed on the server before the next one opens, or the late close packet shuts it
        steps.add(new Step("grinder", "25_gui_grinder", 20, mc -> openMenu(mc, -10, 16)));
        steps.add(new Step("close grinder", null, 10, mc -> mc.player.closeContainer()));
        steps.add(new Step("inscription bench", "26_gui_inscription", 20, mc -> openMenu(mc, 3, 16)));
        steps.add(new Step("close bench", null, 10, mc -> mc.player.closeContainer()));
        steps.add(new Step("sigil", "27_gui_sigil", 20, mc -> openMenu(mc, -20, 0)));
        steps.add(new Step("close sigil", null, 10, mc -> mc.player.closeContainer()));
        steps.add(new Step("item catalog", "30_gui_item_catalog", 10, mc -> mc.setScreen(new CatalogScreen(catalog(mc), false))));
        steps.add(new Step("scroll tooltips", "31_gui_tooltips_scrolls", 10, mc ->
                mc.setScreen(new CatalogScreen(List.of(ScrollData.ward(mc.player, WardType.BANISHMENT), ScrollData.attunement(mc.player, 2)), true))));
        steps.add(new Step("item tooltips", "32_gui_tooltips_items", 10, mc ->
                mc.setScreen(new CatalogScreen(tooltipSamples(), true))));
        steps.add(new Step("close", null, 10, mc -> mc.player.closeContainer()));
        return steps;
    }

    private static Action camera(double x, double dy, double z, double tx, double tdy, double tz) {
        return mc -> onServer(mc, (server, level, player) -> {
            double y = base + dy;
            double dx = tx - x;
            double dyy = base + tdy - y;
            double dz = tz - z;
            float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
            float pitch = (float) -Math.toDegrees(Math.atan2(dyy, Math.sqrt(dx * dx + dz * dz)));
            player.teleportTo(level, x, y - EYE_HEIGHT, z, yaw, pitch);
        });
    }

    /** Every Selarium item, then a few scrolls that carry data (their seals are tinted per ward). */
    private static List<ItemStack> catalog(Minecraft mc) {
        List<ItemStack> stacks = new ArrayList<>();
        ForgeRegistries.ITEMS.getEntries().stream()
                .filter(entry -> entry.getKey().location().getNamespace().equals(Selarium.MOD_ID))
                .sorted(java.util.Comparator.comparing(entry -> entry.getKey().location().getPath()))
                .forEach(entry -> stacks.add(new ItemStack(entry.getValue())));
        for (WardType type : List.of(WardType.BULWARK, WardType.BANISHMENT, WardType.REJUVENATION, WardType.STASIS,
                WardType.SANCTUARY, WardType.WHISPERING)) {
            stacks.add(ScrollData.ward(mc.player, type));
        }
        for (int tier = 1; tier <= 4; tier++) {
            stacks.add(ScrollData.attunement(mc.player, tier));
        }
        return stacks;
    }

    private static List<ItemStack> tooltipSamples() {
        return List.of(
                new ItemStack(ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath(Selarium.MOD_ID, "refined_focus_dust"))),
                new ItemStack(SelariumItems.SELARIUM_CODEX.get()));
    }

    /** Draws item icons large (or their tooltips) on a dark plate, so models and tints can be judged at a glance. */
    private static final class CatalogScreen extends Screen {
        private final List<ItemStack> stacks;
        private final boolean tooltips;

        private CatalogScreen(List<ItemStack> stacks, boolean tooltips) {
            super(Component.literal("Selarium catalog"));
            this.stacks = stacks;
            this.tooltips = tooltips;
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(0, 0, width, height, 0xFF1B1626);
            if (tooltips) {
                for (int i = 0; i < stacks.size(); i++) {
                    graphics.renderTooltip(font, stacks.get(i), 20 + (i % 2) * 210, 70);
                }
                return;
            }
            int columns = 14;
            float scale = 1.5F;
            int cell = 30;
            for (int i = 0; i < stacks.size(); i++) {
                graphics.pose().pushPose();
                graphics.pose().translate(6 + (i % columns) * cell, 6 + (i / columns) * cell, 0.0F);
                graphics.pose().scale(scale, scale, 1.0F);
                graphics.renderItem(stacks.get(i), 0, 0);
                graphics.pose().popPose();
            }
        }
    }

    private static void pressRight(Minecraft mc, int times) {
        for (int i = 0; i < times; i++) {
            mc.screen.keyPressed(GLFW.GLFW_KEY_RIGHT, 0, 0);
        }
    }

    private static void openMenu(Minecraft mc, int x, int z) {
        onServer(mc, (server, level, player) -> {
            BlockPos pos = new BlockPos(x, base, z);
            if (!(level.getBlockEntity(pos) instanceof MenuProvider provider)) {
                throw new IllegalStateException("no menu provider at " + pos);
            }
            NetworkHooks.openScreen(player, provider, pos);
        });
    }

    private static void time(Minecraft mc, long dayTime) {
        onServer(mc, (server, level, player) -> level.setDayTime(dayTime));
    }

    private static void buildStage(MinecraftServer server, ServerLevel level, ServerPlayer player) {
        GameRules rules = server.getGameRules();
        rules.getRule(GameRules.RULE_DAYLIGHT).set(false, server);
        rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false, server);
        rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false, server);
        level.setDayTime(NOON);
        // hover: the client cancels creative flight while standing on the ground, so remove gravity instead
        player.setNoGravity(true);
        player.setDeltaMovement(0.0D, 0.0D, 0.0D);
        player.setInvulnerable(true);
        for (Entity entity : level.getAllEntities()) {
            if (!(entity instanceof Player)) {
                entity.discard();
            }
        }

        base = level.getHeight(Heightmap.Types.WORLD_SURFACE, 0, 0);
        BlockState floor = Blocks.DEEPSLATE_TILES.defaultBlockState();
        for (int x = -34; x <= 34; x++) {
            for (int z = -40; z <= 34; z++) {
                level.setBlock(new BlockPos(x, base - 1, z), floor, 2);
            }
        }

        // three active wards in a row
        placeSigil(level, new BlockPos(-20, base, 0), player, WardType.BULWARK);
        placeSigil(level, new BlockPos(0, base, 0), player, WardType.BANISHMENT);
        placeSigil(level, new BlockPos(20, base, 0), player, WardType.CITADEL);
        // the other two shell styles, one row behind
        placeSigil(level, new BlockPos(-20, base, -26), player, WardType.REJUVENATION);
        placeSigil(level, new BlockPos(0, base, -26), player, WardType.WHISPERING);
        placeInactiveSigil(level, new BlockPos(-3, base, 11), player);
        placeSigil(level, new BlockPos(-26, base, 22), player, WardType.TANGIBLE);
        // a fixed ward field cast from a scroll: no block entity, drawn by the level-stage renderer
        var cast = WardProjectionSavedData.get(level).cast(player, WardDefinitions.get(WardType.STASIS).orElseThrow(),
                new BlockPos(30, base, 14), false);
        LOGGER.info("SMOKE projection cast: {}", cast);
        Creeper creeper = EntityType.CREEPER.create(level);
        if (creeper != null) {
            creeper.moveTo(3.5D, base, 1.5D, 200.0F, 0.0F);
            creeper.setNoAi(true);
            creeper.setPersistenceRequired();
            level.addFreshEntity(creeper);
        }

        // machines: two grinders (idle and lit), three tanks (empty, half, full), the bench, glass-like blocks
        BlockState grinder = SelariumBlocks.ARCANE_GRINDER.get().defaultBlockState().setValue(ArcaneGrinderBlock.FACING, Direction.SOUTH);
        put(level, -10, 16, grinder);
        put(level, -8, 16, grinder.setValue(ArcaneGrinderBlock.LIT, true));
        double[] fill = {0.0D, 0.5D, 1.0D};
        for (int i = 0; i < fill.length; i++) {
            BlockPos pos = put(level, -5 + i * 2, 16, SelariumBlocks.MANA_TANK.get().defaultBlockState());
            if (level.getBlockEntity(pos) instanceof ManaTankBlockEntity tank) {
                tank.setStoredMana((int) (tank.getManaCapacity() * fill[i]));
            }
        }
        put(level, 3, 16, SelariumBlocks.INSCRIPTION_BENCH.get().defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH));
        put(level, 5, 16, SelariumBlocks.PHASING_BLOCK.get().defaultBlockState());
        put(level, 6, 16, SelariumBlocks.TANGIBLE_BARRIER_BLOCK.get().defaultBlockState());
        put(level, 7, 16, SelariumBlocks.TEMPORARY_CITADEL_WALL.get().defaultBlockState());

        // crystals and materials
        Block[] row = {
                SelariumBlocks.SMALL_ARCANE_CRYSTAL_BUD.get(), SelariumBlocks.MEDIUM_ARCANE_CRYSTAL_BUD.get(),
                SelariumBlocks.LARGE_ARCANE_CRYSTAL_BUD.get(), SelariumBlocks.ARCANE_CRYSTAL_CLUSTER.get(),
                SelariumBlocks.ARCANE_CRYSTAL_BLOCK.get(), SelariumBlocks.BUDDING_ARCANE_CRYSTAL.get(),
                SelariumBlocks.ARCANE_GEODE_STONE.get(), SelariumBlocks.POLISHED_ARCANE_GEODE_STONE.get(),
                SelariumBlocks.ARCANE_BLOCK.get(), SelariumBlocks.ARCANE_PLANKS.get(), SelariumBlocks.ARCANE_LOG.get(),
                SelariumBlocks.ARCANE_LEAVES.get(), SelariumBlocks.ARCANE_SAPLING.get(), SelariumBlocks.ARCANE_PETALS.get()};
        for (int i = 0; i < row.length; i++) {
            BlockState state = row[i].defaultBlockState();
            if (state.hasProperty(BlockStateProperties.PERSISTENT)) {
                state = state.setValue(BlockStateProperties.PERSISTENT, true);
            }
            if (row[i] == SelariumBlocks.ARCANE_SAPLING.get() || row[i] == SelariumBlocks.ARCANE_PETALS.get()) {
                level.setBlock(new BlockPos(-10 + i, base - 1, 20), Blocks.GRASS_BLOCK.defaultBlockState(), 2);
            }
            put(level, -10 + i, 20, state);
        }
        LOGGER.info("SMOKE stage built at y={}", base);
    }

    private static BlockPos put(ServerLevel level, int x, int z, BlockState state) {
        BlockPos pos = new BlockPos(x, base, z);
        level.setBlock(pos, state, 3);
        return pos;
    }

    private static void placeSigil(ServerLevel level, BlockPos pos, ServerPlayer owner, WardType type) {
        level.setBlock(pos, SelariumBlocks.ARCANE_SIGIL.get().defaultBlockState(), 3);
        if (!(level.getBlockEntity(pos) instanceof ArcaneSigilBlockEntity sigil)) {
            throw new IllegalStateException("no sigil block entity at " + pos);
        }
        WardDefinition definition = WardDefinitions.get(type).orElseThrow();
        sigil.setOwner(owner.getUUID());
        sigil.setCreatedGameTime(level.getGameTime());
        for (WardRequirement requirement : definition.requirements()) {
            for (int i = 0; i < requirement.count(); i++) {
                sigil.addComponent(new DustDefinition(requirement.type(), requirement.minimumPurity()), false);
            }
        }
        sigil.addInternalMana(4000, 4000);
        sigil.startWard(definition);
    }

    private static void placeInactiveSigil(ServerLevel level, BlockPos pos, ServerPlayer owner) {
        level.setBlock(pos, SelariumBlocks.ARCANE_SIGIL.get().defaultBlockState(), 3);
        if (!(level.getBlockEntity(pos) instanceof ArcaneSigilBlockEntity sigil)) {
            throw new IllegalStateException("no sigil block entity at " + pos);
        }
        sigil.setOwner(owner.getUUID());
        sigil.setCreatedGameTime(level.getGameTime());
        for (DustType type : List.of(DustType.ARCANE, DustType.AEGIS, DustType.FOCUS, DustType.VITAL, DustType.ECHO)) {
            sigil.addComponent(new DustDefinition(type, DustPurity.BASIC), false);
        }
    }

    // ---- plumbing -------------------------------------------------------------------------------------------

    private static void onServer(Minecraft mc, ServerAction action) {
        MinecraftServer server = mc.getSingleplayerServer();
        server.execute(() -> {
            try {
                action.run(server, server.overworld(), server.getPlayerList().getPlayers().get(0));
            } catch (Throwable t) {
                LOGGER.error("SMOKE_FAIL server task", t);
                failed = true;
            }
        });
    }

    private interface ServerAction {
        void run(MinecraftServer server, ServerLevel level, ServerPlayer player) throws Exception;
    }

    private interface Action {
        void run(Minecraft mc) throws Exception;
    }

    private record Step(String name, String shot, int settleTicks, Action action) {
    }
}
