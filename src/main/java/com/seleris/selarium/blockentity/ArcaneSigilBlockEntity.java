package com.seleris.selarium.blockentity;

import com.seleris.selarium.config.SelariumCommonConfig;
import com.seleris.selarium.dust.DustDefinition;
import com.seleris.selarium.dust.DustPurity;
import com.seleris.selarium.dust.DustType;
import com.seleris.selarium.config.SelariumClientConfig;
import com.seleris.selarium.particle.GlowParticleOptions;
import com.seleris.selarium.ward.WardStyles;
import net.minecraft.util.RandomSource;
import com.seleris.selarium.registry.SelariumBlockEntities;
import com.seleris.selarium.registry.SelariumBlocks;
import com.seleris.selarium.ward.ActiveWardIndex;
import com.seleris.selarium.ward.WardDefinition;
import com.seleris.selarium.ward.WardContext;
import com.seleris.selarium.ward.WardDefinitions;
import com.seleris.selarium.ward.WardFieldSource;
import com.seleris.selarium.ward.WardManager;
import com.seleris.selarium.ward.WardType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.MenuProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import com.seleris.selarium.sigil.SigilMenu;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ArcaneSigilBlockEntity extends BlockEntity implements WardFieldSource, MenuProvider {
    private final Map<DustDefinition, Integer> components = new HashMap<>();
    private final Set<BlockPos> temporaryWardBlocks = new HashSet<>();
    private UUID owner;
    private WardType wardType = WardType.NONE;
    private boolean active;
    private int internalManaBuffer;
    private int range = 1;
    private long createdGameTime;
    private int wardDurationRemainingTicks;
    private int wardCooldownRemainingTicks;
    private int manaGeneratedThisActivation;
    private int alertCooldownRemainingTicks;
    private long lastWardTick = -1L;
    private int lastWardEntitiesAffected;
    private int lastWardBlocksAffected;
    private String lastWardDebug = "not run";
    private long lastUpkeepTick = -1L;
    private int lastUpkeepCost;
    private boolean lastUpkeepPaid;
    private String lastUpkeepDebug = "not paid yet";
    // client-only bookkeeping used to detect visual transitions (never saved)
    private boolean clientSeen;
    private boolean clientWasActive;
    private int clientMarkCount;

    public ArcaneSigilBlockEntity(BlockPos pos, BlockState state) {
        super(SelariumBlockEntities.ARCANE_SIGIL.get(), pos, state);
    }

    @Override public Component getDisplayName() { return Component.translatable("block.selarium.arcane_sigil"); }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new SigilMenu(id, inventory, worldPosition);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ArcaneSigilBlockEntity sigil) {
        if (level instanceof ServerLevel serverLevel && sigil.shouldTickWardState()) {
            if (!sigil.isActive() && !sigil.temporaryWardBlocks.isEmpty()) sigil.cleanupTemporaryWardBlocks();
            WardManager.tick(new WardContext(serverLevel, pos, state, sigil));
        }
    }

    /**
     * While active the sigil draws its ward field, so the renderer must be invoked even when the block
     * itself is off-screen; the box therefore grows to the field's reach.
     */
    @Override
    public AABB getRenderBoundingBox() {
        if (active) {
            return new AABB(worldPosition).inflate(Math.max(1, range) + 1.0D);
        }
        return new AABB(worldPosition).inflate(0.5D, 1.5D, 0.5D);
    }

    /** Client-side ticker: spawns the ambient and transition particles of the ritual circle. */
    public static void clientTick(Level level, BlockPos pos, BlockState state, ArcaneSigilBlockEntity sigil) {
        sigil.tickClientEffects(level, pos);
    }

    private void tickClientEffects(Level level, BlockPos pos) {
        if (!SelariumClientConfig.vfxEnabled() || !SelariumClientConfig.AMBIENT_PARTICLES.get()) {
            clientSeen = false;
            return;
        }
        int marks = Math.max(0, getTotalComponents() - getComponentCount(DustType.ARCANE, DustPurity.BASIC));
        if (!clientSeen) {
            clientSeen = true;
            clientWasActive = active;
            clientMarkCount = marks;
            return;
        }

        RandomSource random = level.random;
        double cx = pos.getX() + 0.5D;
        double cy = pos.getY() + 0.1D;
        double cz = pos.getZ() + 0.5D;
        WardStyles.Style style = WardStyles.of(wardType);
        int primary = wardType == WardType.NONE ? 0xC9C2EE : style.primary();
        int secondary = wardType == WardType.NONE ? 0xEDE9FF : style.secondary();

        if (marks > clientMarkCount) {
            for (int i = 0; i < 8; i++) {
                double angle = random.nextDouble() * Math.PI * 2.0D;
                double radius = 0.15D + random.nextDouble() * 0.3D;
                level.addParticle(GlowParticleOptions.spark(0xE6DBFF, 0.8F), cx + Math.cos(angle) * radius, cy + 0.05D,
                        cz + Math.sin(angle) * radius, 0.0D, 0.02D, 0.0D);
            }
        }
        if (active && !clientWasActive) {
            level.addParticle(GlowParticleOptions.ring(secondary, Math.max(1, range)), cx, pos.getY() + 0.06D, cz, 0.0D, 0.0D, 0.0D);
            level.addParticle(GlowParticleOptions.ring(primary, Math.max(1, range) * 0.55F), cx, pos.getY() + 0.08D, cz, 0.0D, 0.0D, 0.0D);
            for (int i = 0; i < 16; i++) {
                double angle = random.nextDouble() * Math.PI * 2.0D;
                double radius = 0.2D + random.nextDouble() * 0.28D;
                level.addParticle(GlowParticleOptions.rune(secondary, 1.0F), cx + Math.cos(angle) * radius, cy + 0.1D,
                        cz + Math.sin(angle) * radius, 0.0D, 0.03D + random.nextDouble() * 0.03D, 0.0D);
                level.addParticle(GlowParticleOptions.wisp(primary, 1.0F), cx + Math.cos(angle) * radius, cy + 0.05D,
                        cz + Math.sin(angle) * radius, -Math.cos(angle) * 0.01D, 0.04D, -Math.sin(angle) * 0.01D);
            }
        }
        if (!active && clientWasActive) {
            for (int i = 0; i < 10; i++) {
                double angle = random.nextDouble() * Math.PI * 2.0D;
                double radius = 0.1D + random.nextDouble() * 0.35D;
                level.addParticle(GlowParticleOptions.wisp(primary, 0.8F), cx + Math.cos(angle) * radius, cy + 0.05D,
                        cz + Math.sin(angle) * radius, 0.0D, 0.015D, 0.0D);
            }
        }
        if (active && level.getGameTime() % 5L == 0L) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double radius = 0.34D + random.nextDouble() * 0.12D;
            level.addParticle(GlowParticleOptions.wisp(primary, 0.85F), cx + Math.cos(angle) * radius, cy,
                    cz + Math.sin(angle) * radius, 0.0D, 0.018D + random.nextDouble() * 0.012D, 0.0D);
            if (random.nextInt(3) == 0) {
                level.addParticle(GlowParticleOptions.spark(secondary, 0.7F), cx + Math.cos(angle) * radius, cy + 0.15D,
                        cz + Math.sin(angle) * radius, 0.0D, 0.01D, 0.0D);
            }
        }
        clientWasActive = active;
        clientMarkCount = marks;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        setChangedAndSync();
    }

    public WardType getWardType() {
        return wardType;
    }

    public void setWardType(WardType wardType) {
        this.wardType = wardType == null ? WardType.NONE : wardType;
        if (this.wardType == WardType.NONE) {
            this.active = false;
            this.wardDurationRemainingTicks = 0;
            cleanupTemporaryWardBlocks();
        }
        setChangedAndSync();
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active && wardType != WardType.NONE;
        if (!this.active) {
            this.wardDurationRemainingTicks = 0;
            cleanupTemporaryWardBlocks();
        }
        setChangedAndSync();
    }

    public int getInternalManaBuffer() {
        return internalManaBuffer;
    }

    public int getRange() {
        return range;
    }

    public int getWardDurationRemainingTicks() {
        return wardDurationRemainingTicks;
    }

    public int getWardCooldownRemainingTicks() {
        return wardCooldownRemainingTicks;
    }

    public int getManaGeneratedThisActivation() {
        return manaGeneratedThisActivation;
    }

    public int getAlertCooldownRemainingTicks() {
        return alertCooldownRemainingTicks;
    }

    public long getLastWardTick() {
        return lastWardTick;
    }

    public int getLastWardEntitiesAffected() {
        return lastWardEntitiesAffected;
    }

    public int getLastWardBlocksAffected() {
        return lastWardBlocksAffected;
    }

    public String getLastWardDebug() {
        return lastWardDebug;
    }

    public long getLastUpkeepTick() {
        return lastUpkeepTick;
    }

    public int getLastUpkeepCost() {
        return lastUpkeepCost;
    }

    public boolean wasLastUpkeepPaid() {
        return lastUpkeepPaid;
    }

    public String getLastUpkeepDebug() {
        return lastUpkeepDebug;
    }

    public void recordWardDebug(int entitiesAffected, int blocksAffected, String debug) {
        lastWardTick = level == null ? -1L : level.getGameTime();
        lastWardEntitiesAffected = Math.max(0, entitiesAffected);
        lastWardBlocksAffected = Math.max(0, blocksAffected);
        lastWardDebug = debug == null || debug.isBlank() ? "ok" : debug;
        setChanged();
    }

    public void recordWardUpkeep(int cost, boolean paid, String debug) {
        lastUpkeepTick = level == null ? -1L : level.getGameTime();
        lastUpkeepCost = Math.max(0, cost);
        lastUpkeepPaid = paid;
        lastUpkeepDebug = debug == null || debug.isBlank() ? (paid ? "paid" : "not paid") : debug;
        setChanged();
    }

    public boolean hasRecentPaidUpkeep(int graceTicks) {
        if (!lastUpkeepPaid || level == null || lastUpkeepTick < 0) {
            return false;
        }
        return level.getGameTime() - lastUpkeepTick <= Math.max(1, graceTicks);
    }

    public int getTemporaryWardBlockCount() {
        return temporaryWardBlocks.size();
    }

    public boolean trackTemporaryWardBlock(BlockPos pos) {
        boolean added = temporaryWardBlocks.add(pos.immutable());
        if (added) {
            setChangedAndSync();
        }
        return added;
    }

    @Override public boolean ownsTemporaryWardBlock(BlockPos pos) {
        return temporaryWardBlocks.contains(pos);
    }

    @Override public Set<BlockPos> temporaryWardBlocks() { return Set.copyOf(temporaryWardBlocks); }

    public void cleanupTemporaryWardBlocks() {
        if (level == null || level.isClientSide || temporaryWardBlocks.isEmpty()) {
            return;
        }

        temporaryWardBlocks.removeIf(pos -> {
            if (!level.isLoaded(pos)) return false;
            BlockState state = level.getBlockState(pos);
            if (isTrackedTemporaryBlock(state)) level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            return true;
        });
        ActiveWardIndex.remove(level, worldPosition);
        setChangedAndSync();
    }

    public void setAlertCooldownRemainingTicks(int alertCooldownRemainingTicks) {
        this.alertCooldownRemainingTicks = Math.max(0, alertCooldownRemainingTicks);
        setChangedAndSync();
    }

    public long getCreatedGameTime() {
        return createdGameTime;
    }

    public void setCreatedGameTime(long createdGameTime) {
        this.createdGameTime = createdGameTime;
        setChangedAndSync();
    }

    public Map<DustDefinition, Integer> getComponents() {
        return Collections.unmodifiableMap(components);
    }

    public boolean hasComponent(DustType type, DustPurity minimumPurity) {
        return getComponentCount(type, minimumPurity) > 0;
    }

    public int getComponentCount(DustType type, DustPurity minimumPurity) {
        int total = 0;
        for (Map.Entry<DustDefinition, Integer> entry : components.entrySet()) {
            if (entry.getKey().satisfies(type, minimumPurity)) {
                total += entry.getValue();
            }
        }
        return total;
    }

    public int getTotalComponents() {
        int total = 0;
        for (int count : components.values()) {
            total += count;
        }
        return total;
    }

    public boolean addComponent(DustDefinition definition) {
        return addComponent(definition, true);
    }

    public boolean addComponent(DustDefinition definition, boolean respectLimit) {
        if (respectLimit && getTotalComponents() >= SelariumCommonConfig.SIGIL_MAX_COMPONENTS.get()) {
            return false;
        }

        components.merge(definition, 1, Integer::sum);
        setChangedAndSync();
        return true;
    }

    public boolean removeOneComponent(DustType type, DustPurity minimumPurity) {
        DustDefinition selected = null;
        for (DustDefinition definition : components.keySet()) {
            if (definition.satisfies(type, minimumPurity)) {
                selected = definition;
                break;
            }
        }

        if (selected == null) {
            return false;
        }

        int count = components.getOrDefault(selected, 0);
        if (count <= 1) {
            components.remove(selected);
        } else {
            components.put(selected, count - 1);
        }
        setChangedAndSync();
        return true;
    }

    public boolean removeOneComponent(DustDefinition definition) {
        if (active) return false;
        int count = components.getOrDefault(definition, 0);
        if (count <= 0) return false;
        if (count == 1) components.remove(definition); else components.put(definition, count - 1);
        if (wardType != WardType.NONE && WardDefinitions.get(wardType)
                .map(value -> value.requirements().stream().anyMatch(requirement -> !requirement.matches(this)))
                .orElse(true)) {
            wardType = WardType.NONE;
        }
        setChangedAndSync();
        return true;
    }

    public int addInternalMana(int amount, int maxBuffer) {
        int safeMaxBuffer = Math.max(0, maxBuffer);
        if (internalManaBuffer > safeMaxBuffer) {
            internalManaBuffer = safeMaxBuffer;
            setChangedAndSync();
        }

        if (amount <= 0 || internalManaBuffer >= safeMaxBuffer) {
            return 0;
        }

        int accepted = Math.min(amount, safeMaxBuffer - internalManaBuffer);
        internalManaBuffer += accepted;
        setChangedAndSync();
        return accepted;
    }

    public int extractInternalMana(int amount) {
        if (amount <= 0 || internalManaBuffer <= 0) {
            return 0;
        }

        int extracted = Math.min(amount, internalManaBuffer);
        internalManaBuffer -= extracted;
        setChangedAndSync();
        return extracted;
    }

    public void startWard(WardDefinition definition) {
        wardType = definition.type();
        active = true;
        wardDurationRemainingTicks = Math.max(0, definition.durationTicks());
        wardCooldownRemainingTicks = 0;
        manaGeneratedThisActivation = 0;
        alertCooldownRemainingTicks = 0;
        lastUpkeepTick = -1L;
        lastUpkeepCost = 0;
        lastUpkeepPaid = false;
        lastUpkeepDebug = "not paid yet";
        range = Math.max(1, definition.range());
        setChangedAndSync();
    }

    public void deactivateWard(int cooldownTicks) {
        cleanupTemporaryWardBlocks();
        active = false;
        wardDurationRemainingTicks = 0;
        wardCooldownRemainingTicks = Math.max(wardCooldownRemainingTicks, cooldownTicks);
        setChangedAndSync();
    }

    public void recordManaGeneratedThisActivation(int amount) {
        if (amount <= 0) {
            return;
        }
        manaGeneratedThisActivation = Math.max(0, manaGeneratedThisActivation + amount);
        setChangedAndSync();
    }

    public void tickWardTimers() {
        boolean changed = false;
        if (wardCooldownRemainingTicks > 0) {
            wardCooldownRemainingTicks--;
            changed = true;
        }
        if (alertCooldownRemainingTicks > 0) {
            alertCooldownRemainingTicks--;
            changed = true;
        }
        if (active && wardDurationRemainingTicks > 0) {
            wardDurationRemainingTicks--;
            changed = true;
        }
        if (changed && level != null && level.getGameTime() % 20L == 0L) {
            setChanged();
        }
    }

    private boolean shouldTickWardState() {
        return active || wardCooldownRemainingTicks > 0 || alertCooldownRemainingTicks > 0 || !temporaryWardBlocks.isEmpty();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (owner != null) {
            tag.putUUID("Owner", owner);
        }
        ListTag componentList = new ListTag();
        for (Map.Entry<DustDefinition, Integer> entry : components.entrySet()) {
            CompoundTag componentTag = new CompoundTag();
            componentTag.putString("Type", entry.getKey().type().getSerializedName());
            componentTag.putString("Purity", entry.getKey().purity().getSerializedName());
            componentTag.putInt("Count", Math.max(0, entry.getValue()));
            componentList.add(componentTag);
        }
        tag.put("DustComponents", componentList);
        tag.putString("WardType", wardType.getSerializedName());
        tag.putBoolean("Active", active);
        tag.putInt("InternalManaBuffer", internalManaBuffer);
        tag.putInt("Range", range);
        tag.putLong("CreatedGameTime", createdGameTime);
        tag.putInt("WardDurationRemainingTicks", wardDurationRemainingTicks);
        tag.putInt("WardCooldownRemainingTicks", wardCooldownRemainingTicks);
        tag.putInt("ManaGeneratedThisActivation", manaGeneratedThisActivation);
        tag.putInt("AlertCooldownRemainingTicks", alertCooldownRemainingTicks);
        ListTag temporaryBlocks = new ListTag();
        for (BlockPos temporaryPos : temporaryWardBlocks) {
            CompoundTag temporaryTag = new CompoundTag();
            temporaryTag.putInt("X", temporaryPos.getX());
            temporaryTag.putInt("Y", temporaryPos.getY());
            temporaryTag.putInt("Z", temporaryPos.getZ());
            temporaryBlocks.add(temporaryTag);
        }
        tag.put("TemporaryWardBlocks", temporaryBlocks);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        components.clear();
        if (tag.contains("DustComponents", Tag.TAG_LIST)) {
            ListTag componentList = tag.getList("DustComponents", Tag.TAG_COMPOUND);
            for (int index = 0; index < componentList.size(); index++) {
                CompoundTag componentTag = componentList.getCompound(index);
                addLoadedComponent(componentTag.getString("Type"), componentTag.getString("Purity"), componentTag.getInt("Count"));
            }
        } else {
            CompoundTag legacyComponents = tag.getCompound("Components");
            for (String key : legacyComponents.getAllKeys()) {
                addLoadedComponent(key, DustPurity.BASIC.getSerializedName(), legacyComponents.getInt(key));
            }
        }
        wardType = WardType.bySerializedName(tag.getString("WardType"));
        active = tag.getBoolean("Active") && wardType != WardType.NONE;
        internalManaBuffer = Math.max(0, Math.min(SelariumCommonConfig.AMBIENT_WARD_MAX_INTERNAL_BUFFER.get(), tag.getInt("InternalManaBuffer")));
        range = Math.max(1, tag.getInt("Range"));
        createdGameTime = tag.getLong("CreatedGameTime");
        wardDurationRemainingTicks = Math.max(0, tag.getInt("WardDurationRemainingTicks"));
        wardCooldownRemainingTicks = Math.max(0, tag.getInt("WardCooldownRemainingTicks"));
        manaGeneratedThisActivation = Math.max(0, tag.getInt("ManaGeneratedThisActivation"));
        alertCooldownRemainingTicks = Math.max(0, tag.getInt("AlertCooldownRemainingTicks"));
        temporaryWardBlocks.clear();
        if (tag.contains("TemporaryWardBlocks", Tag.TAG_LIST)) {
            ListTag temporaryBlocks = tag.getList("TemporaryWardBlocks", Tag.TAG_COMPOUND);
            for (int index = 0; index < temporaryBlocks.size(); index++) {
                CompoundTag temporaryTag = temporaryBlocks.getCompound(index);
                temporaryWardBlocks.add(new BlockPos(temporaryTag.getInt("X"), temporaryTag.getInt("Y"), temporaryTag.getInt("Z")));
            }
        }
        if (active && WardDefinitions.get(wardType).isEmpty()) {
            active = false;
            wardDurationRemainingTicks = 0;
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        CompoundTag tag = packet.getTag();
        if (tag != null) {
            load(tag);
        }
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private void addLoadedComponent(String typeName, String purityName, int count) {
        if (count <= 0) {
            return;
        }

        DustType.fromSerializedName(typeName).ifPresent(type ->
                DustPurity.fromSerializedName(purityName).ifPresent(purity ->
                        components.merge(new DustDefinition(type, purity), count, Integer::sum)));
    }

    private static boolean isTrackedTemporaryBlock(BlockState state) {
        return state.is(SelariumBlocks.TEMPORARY_CITADEL_WALL.get()) || state.is(SelariumBlocks.TANGIBLE_BARRIER_BLOCK.get());
    }
}
