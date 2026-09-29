package com.seleris.selarium.worldgen;

import com.seleris.selarium.config.SelariumCommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

final class ArcaneTreeTemplate {
    private ArcaneTreeTemplate() {
    }

    static boolean generate(LevelAccessor level, RandomSource random, BlockPos basePos, int requestedHeight, boolean allowAncientVariant) {
        Variant selected = chooseVariant(random, allowAncientVariant);
        if (tryGenerateVariant(level, random, basePos, requestedHeight, selected)) {
            return true;
        }
        for (Variant fallback : selected.fallbacks()) {
            if ((fallback != Variant.ANCIENT || allowAncientVariant) && tryGenerateVariant(level, random, basePos, requestedHeight, fallback)) {
                return true;
            }
        }
        return false;
    }

    private static boolean tryGenerateVariant(LevelAccessor level, RandomSource random, BlockPos basePos, int requestedHeight, Variant variant) {
        int height = variant.height(requestedHeight, random);
        int turns = random.nextInt(4);
        TreePlan plan = buildPlan(random, height, variant);
        if (!canPlace(level, basePos, plan, turns)) {
            return false;
        }
        place(level, basePos, plan, turns);
        return true;
    }

    private static Variant chooseVariant(RandomSource random, boolean allowAncientVariant) {
        int standard = Math.max(0, SelariumCommonConfig.ARCANE_TREE_STANDARD_VARIANT_WEIGHT.get());
        int tall = Math.max(0, SelariumCommonConfig.ARCANE_TREE_TALL_VARIANT_WEIGHT.get());
        int spreading = Math.max(0, SelariumCommonConfig.ARCANE_TREE_SPREADING_VARIANT_WEIGHT.get());
        int ancient = allowAncientVariant ? Math.max(0, SelariumCommonConfig.ARCANE_TREE_ANCIENT_VARIANT_WEIGHT.get()) : 0;
        int total = standard + tall + spreading + ancient;
        if (total <= 0) {
            return Variant.STANDARD;
        }

        int roll = random.nextInt(total);
        if (roll < standard) {
            return Variant.STANDARD;
        }
        roll -= standard;
        if (roll < tall) {
            return Variant.TALL;
        }
        roll -= tall;
        if (roll < spreading) {
            return Variant.SPREADING;
        }
        return Variant.ANCIENT;
    }

    private static TreePlan buildPlan(RandomSource random, int height, Variant variant) {
        LinkedHashMap<Vec, Part> parts = new LinkedHashMap<>();

        addRootFlare(parts, variant);
        addSinuousTrunk(parts, height, variant);

        switch (variant) {
            case STANDARD -> addStandardCrown(parts, random, height);
            case TALL -> addTallCrown(parts, random, height);
            case SPREADING -> addSpreadingCrown(parts, random, height);
            case ANCIENT -> addAncientCrown(parts, random, height);
        }

        addLowSkirt(parts, random, height, variant);
        pruneDisconnectedLeaves(parts);
        return new TreePlan(parts);
    }

    private static void pruneDisconnectedLeaves(LinkedHashMap<Vec, Part> parts) {
        Set<Vec> connectedLeaves = new HashSet<>();
        ArrayDeque<Vec> queue = new ArrayDeque<>();

        for (Map.Entry<Vec, Part> entry : parts.entrySet()) {
            if (entry.getValue().kind() != Kind.LOG) {
                continue;
            }
            for (Direction direction : Direction.values()) {
                Vec neighbor = entry.getKey().relative(direction);
                Part part = parts.get(neighbor);
                if (part != null && part.kind() == Kind.LEAF && connectedLeaves.add(neighbor)) {
                    queue.addLast(neighbor);
                }
            }
        }

        while (!queue.isEmpty()) {
            Vec current = queue.removeFirst();
            for (Direction direction : Direction.values()) {
                Vec neighbor = current.relative(direction);
                Part part = parts.get(neighbor);
                if (part != null && part.kind() == Kind.LEAF && connectedLeaves.add(neighbor)) {
                    queue.addLast(neighbor);
                }
            }
        }

        parts.entrySet().removeIf(entry -> entry.getValue().kind() == Kind.LEAF && !connectedLeaves.contains(entry.getKey()));
    }

    private static void addStandardCrown(Map<Vec, Part> parts, RandomSource random, int height) {
        Vec westTip = addBranch(parts, trunkOffset(height - 4, height, Variant.STANDARD), Direction.WEST, 4, Direction.NORTH, 1, true);
        Vec southTip = addBranch(parts, trunkOffset(height - 3, height, Variant.STANDARD), Direction.SOUTH, 3, Direction.WEST, 2, false);
        Vec eastTip = addBranch(parts, trunkOffset(height - 2, height, Variant.STANDARD), Direction.EAST, 4, Direction.SOUTH, 1, true);
        Vec northTip = addBranch(parts, trunkOffset(height - 1, height, Variant.STANDARD), Direction.NORTH, 3, Direction.EAST, random.nextBoolean() ? 2 : 1, false);
        Vec top = trunkOffset(height, height, Variant.STANDARD);

        addLeafBlob(parts, top.above(), 3, 2, 3, 101, 2, 0.30D);
        addLeafBlob(parts, top.offset(1, 0, -1), 2, 2, 3, 211, 2, 0.34D);
        addLeafBlob(parts, top.offset(-2, -1, 1), 3, 1, 2, 307, 3, 0.38D);
        addBranchCluster(parts, random, 401, 2, westTip, southTip, eastTip, northTip);
    }

    private static void addTallCrown(Map<Vec, Part> parts, RandomSource random, int height) {
        Vec lower = addBranch(parts, trunkOffset(height - 6, height, Variant.TALL), Direction.SOUTH, 3, Direction.EAST, 1, true);
        Vec west = addBranch(parts, trunkOffset(height - 4, height, Variant.TALL), Direction.WEST, 4, Direction.NORTH, 1, true);
        Vec east = addBranch(parts, trunkOffset(height - 3, height, Variant.TALL), Direction.EAST, 4, Direction.SOUTH, 2, true);
        Vec north = addBranch(parts, trunkOffset(height - 2, height, Variant.TALL), Direction.NORTH, 3, Direction.WEST, 1, true);
        Vec high = addBranch(parts, trunkOffset(height - 1, height, Variant.TALL), Direction.WEST, 2, Direction.NORTH, 1, false);
        Vec top = trunkOffset(height, height, Variant.TALL);

        addLeafBlob(parts, top.above(), 2, 3, 2, 503, 2, 0.31D);
        addLeafBlob(parts, top.offset(1, 1, -1), 3, 2, 2, 541, 2, 0.35D);
        addLeafBlob(parts, top.offset(-1, -1, 2), 2, 2, 3, 557, 3, 0.36D);
        addBranchCluster(parts, random, 601, 2, lower, west, east, north, high);
        addHangingLeaves(parts, west.offset(-1, -1, 0), 2, 683);
        addHangingLeaves(parts, east.offset(1, -1, 1), 2 + random.nextInt(2), 691);
    }

    private static void addSpreadingCrown(Map<Vec, Part> parts, RandomSource random, int height) {
        Vec west = addBranch(parts, trunkOffset(height - 4, height, Variant.SPREADING), Direction.WEST, 5, Direction.NORTH, 2, false);
        Vec east = addBranch(parts, trunkOffset(height - 5, height, Variant.SPREADING), Direction.EAST, 5, Direction.SOUTH, 2, false);
        Vec south = addBranch(parts, trunkOffset(height - 3, height, Variant.SPREADING), Direction.SOUTH, 4, Direction.WEST, 2, true);
        Vec north = addBranch(parts, trunkOffset(height - 2, height, Variant.SPREADING), Direction.NORTH, 4, Direction.EAST, 2, false);
        Vec top = trunkOffset(height, height, Variant.SPREADING);

        addLeafBlob(parts, top, 3, 2, 3, 701, 2, 0.34D);
        addLeafBlob(parts, west, 3, 1, 2, 719, 2, 0.32D);
        addLeafBlob(parts, east, 3, 1, 2, 727, 2, 0.32D);
        addLeafBlob(parts, south.offset(0, 0, 1), 2, 1, 3, 733, 3, 0.36D);
        addLeafBlob(parts, north.offset(-1, 0, 0), 2, 1, 3, 739, 3, 0.38D);
        addHangingLeaves(parts, west.offset(-1, -1, 0), 3, 751);
        addHangingLeaves(parts, east.offset(1, -1, -1), 3, 757);
        addHangingLeaves(parts, south.offset(0, -1, 2), 2 + random.nextInt(2), 761);
        addHangingLeaves(parts, north.offset(-1, -1, -2), 2 + random.nextInt(2), 769);
    }

    private static void addAncientCrown(Map<Vec, Part> parts, RandomSource random, int height) {
        Vec west = addBranch(parts, trunkOffset(height - 7, height, Variant.ANCIENT), Direction.WEST, 5, Direction.NORTH, 2, true);
        Vec east = addBranch(parts, trunkOffset(height - 6, height, Variant.ANCIENT), Direction.EAST, 5, Direction.SOUTH, 2, true);
        Vec south = addBranch(parts, trunkOffset(height - 5, height, Variant.ANCIENT), Direction.SOUTH, 5, Direction.WEST, 2, true);
        Vec north = addBranch(parts, trunkOffset(height - 4, height, Variant.ANCIENT), Direction.NORTH, 5, Direction.EAST, 2, true);
        Vec highWest = addBranch(parts, trunkOffset(height - 2, height, Variant.ANCIENT), Direction.WEST, 3, Direction.SOUTH, 1, true);
        Vec highEast = addBranch(parts, trunkOffset(height - 1, height, Variant.ANCIENT), Direction.EAST, 3, Direction.NORTH, 1, true);
        Vec top = trunkOffset(height, height, Variant.ANCIENT);

        addLeafBlob(parts, top.above(), 4, 2, 3, 811, 2, 0.35D);
        addLeafBlob(parts, top.offset(-2, 0, 2), 3, 2, 3, 823, 2, 0.34D);
        addLeafBlob(parts, top.offset(2, -1, -1), 3, 2, 4, 839, 2, 0.36D);
        addLeafBlob(parts, west, 3, 2, 2, 853, 2, 0.30D);
        addLeafBlob(parts, east, 3, 2, 2, 859, 2, 0.30D);
        addLeafBlob(parts, south, 2, 1, 3, 863, 3, 0.35D);
        addLeafBlob(parts, north, 2, 1, 3, 877, 3, 0.35D);
        addBranchCluster(parts, random, 881, 3, highWest, highEast);
        addHangingLeaves(parts, west.offset(-1, -1, 0), 3, 887);
        addHangingLeaves(parts, east.offset(1, -1, 1), 3, 907);
        addHangingLeaves(parts, south.offset(0, -1, 2), 3, 919);
        addHangingLeaves(parts, north.offset(1, -1, -2), 2 + random.nextInt(2), 929);
    }

    private static boolean canPlace(LevelAccessor level, BlockPos basePos, TreePlan plan, int turns) {
        if (!ArcaneTreeGenerator.isValidTreeSupport(level.getBlockState(basePos.below()))) {
            return false;
        }
        for (Map.Entry<Vec, Part> entry : plan.parts().entrySet()) {
            Vec rotated = rotate(entry.getKey(), turns);
            BlockPos pos = basePos.offset(rotated.toBlockPos());
            if (!ArcaneTreeGenerator.canReplaceTreeBlock(level, pos)) {
                return false;
            }
            if (entry.getValue().kind() == Kind.LOG && rotated.y() == 0 && rotated.horizontalDistance() > 1
                    && level.getBlockState(pos.below()).isAir()) {
                return false;
            }
        }
        return true;
    }

    private static void place(LevelAccessor level, BlockPos basePos, TreePlan plan, int turns) {
        for (Map.Entry<Vec, Part> entry : plan.parts().entrySet()) {
            Vec rotated = rotate(entry.getKey(), turns);
            Part part = entry.getValue();
            BlockState state = switch (part.kind()) {
                case LOG -> ArcaneTreeGenerator.log(rotateAxis(part.axis(), turns));
                case LEAF -> ArcaneTreeGenerator.leaves(part.leafDistance());
            };
            level.setBlock(basePos.offset(rotated.toBlockPos()), state, 2);
        }
    }

    private static void addRootFlare(Map<Vec, Part> parts, Variant variant) {
        putLog(parts, new Vec(0, 0, 0), Direction.Axis.Y);
        putLog(parts, new Vec(1, 0, 0), Direction.Axis.Y);
        putLog(parts, new Vec(0, 0, 1), Direction.Axis.Y);
        putLog(parts, new Vec(-1, 0, 0), Direction.Axis.X);
        putLog(parts, new Vec(0, 0, -1), Direction.Axis.Z);

        addPath(parts, new Vec(0, 0, 0), new Vec(0, 0, -1), new Vec(0, 0, -2), new Vec(1, 0, -2));
        addPath(parts, new Vec(0, 0, 0), new Vec(-1, 0, 0), new Vec(-2, 0, 0), new Vec(-2, 0, 1));
        addPath(parts, new Vec(1, 0, 0), new Vec(2, 0, 0), new Vec(3, 0, 1));
        addPath(parts, new Vec(0, 0, 1), new Vec(0, 0, 2), new Vec(-1, 0, 2));
        addPath(parts, new Vec(0, 0, 0), new Vec(1, 0, -1), new Vec(2, 0, -1));

        if (variant == Variant.SPREADING || variant == Variant.ANCIENT) {
            addPath(parts, new Vec(0, 0, 0), new Vec(-1, 0, -1), new Vec(-2, 0, -2), new Vec(-3, 0, -2));
            addPath(parts, new Vec(0, 0, 1), new Vec(1, 0, 2), new Vec(2, 0, 3));
        }
        if (variant == Variant.ANCIENT) {
            putLog(parts, new Vec(1, 1, 0), Direction.Axis.Y);
            putLog(parts, new Vec(0, 1, 1), Direction.Axis.Y);
            addPath(parts, new Vec(1, 0, 0), new Vec(2, 0, -1), new Vec(3, 0, -2));
            addPath(parts, new Vec(-1, 0, 0), new Vec(-2, 0, 1), new Vec(-3, 0, 2));
        }
    }

    private static void addSinuousTrunk(Map<Vec, Part> parts, int height, Variant variant) {
        Vec previous = trunkOffset(0, height, variant);
        putLog(parts, previous, Direction.Axis.Y);
        for (int y = 1; y <= height; y++) {
            Vec current = trunkOffset(y, height, variant);
            if (current.x() != previous.x() || current.z() != previous.z()) {
                Vec bridgeStart = new Vec(previous.x(), y, previous.z());
                putLog(parts, bridgeStart, Direction.Axis.Y);
                addPath(parts, bridgeStart, new Vec(current.x(), y, current.z()));
            }
            putLog(parts, current, Direction.Axis.Y);
            if (variant == Variant.ANCIENT && y > 1 && y < height - 3 && y % 3 == 0) {
                putLog(parts, current.offset(-1, 0, 0), Direction.Axis.Y);
            }
            previous = current;
        }
    }

    private static Vec addBranch(Map<Vec, Part> parts, Vec start, Direction direction, int length, Direction bend, int bendLength, boolean liftEnd) {
        Vec current = start;
        for (int i = 0; i < length; i++) {
            Vec next = current.relative(direction);
            addPath(parts, current, next);
            current = next;
            if (i == 1 && liftEnd) {
                Vec lifted = current.above();
                addPath(parts, current, lifted);
                current = lifted;
            }
        }
        for (int i = 0; i < bendLength; i++) {
            Vec next = current.relative(bend);
            addPath(parts, current, next);
            current = next;
        }
        return current;
    }

    private static void addBranchCluster(Map<Vec, Part> parts, RandomSource random, int seed, int hangingBias, Vec... tips) {
        int offset = 0;
        for (Vec tip : tips) {
            addLeafBlob(parts, tip, 2, 1, 2, seed + offset, 2, 0.33D);
            if (random.nextBoolean()) {
                addLeafBlob(parts, tip.offset(0, -1, 0), 2, 1, 1, seed + offset + 47, 3, 0.38D);
            }
            addHangingLeaves(parts, tip.offset(1, -1, 0), Math.max(1, hangingBias - 1 + random.nextInt(2)), seed + offset + 3);
            addHangingLeaves(parts, tip.offset(-1, -1, 1), hangingBias + random.nextInt(2), seed + offset + 9);
            offset += 83;
        }
    }

    private static void addLowSkirt(Map<Vec, Part> parts, RandomSource random, int height, Variant variant) {
        int y = Math.max(3, height - variant.lowSkirtDrop());
        Vec center = trunkOffset(y, height, variant);
        addLeafBlob(parts, center.offset(-2, 0, -1), 2, 1, 2, 1009, 4, 0.44D);
        addLeafBlob(parts, center.offset(2, 1, 1), 2, 1, 2, 1107, 4, 0.44D);
        if (variant == Variant.SPREADING || variant == Variant.ANCIENT || random.nextBoolean()) {
            addHangingLeaves(parts, center.offset(-3, 0, 0), 2, 1191);
        }
        if (variant != Variant.TALL) {
            addHangingLeaves(parts, center.offset(2, 0, -2), 2, 1197);
        }
    }

    private static void addLeafBlob(Map<Vec, Part> parts, Vec center, int radiusX, int radiusY, int radiusZ, int seed, int distance, double edgeHoleChance) {
        for (int dx = -radiusX; dx <= radiusX; dx++) {
            for (int dy = -radiusY; dy <= radiusY; dy++) {
                for (int dz = -radiusZ; dz <= radiusZ; dz++) {
                    double normalized = square(dx / (double) radiusX)
                            + square(dy / (double) Math.max(1, radiusY))
                            + square(dz / (double) radiusZ);
                    if (normalized > 1.20D) {
                        continue;
                    }
                    int noise = Math.abs(hash(center.x() + dx, center.y() + dy, center.z() + dz, seed)) % 100;
                    if (normalized > 0.70D && noise < (int) (edgeHoleChance * 100.0D)) {
                        continue;
                    }
                    if (Math.abs(dx) == radiusX && Math.abs(dz) == radiusZ && noise < 72) {
                        continue;
                    }
                    putLeaf(parts, center.offset(dx, dy, dz), distance);
                }
            }
        }
    }

    private static void addHangingLeaves(Map<Vec, Part> parts, Vec start, int length, int seed) {
        for (int i = 0; i < length; i++) {
            Vec pos = start.below(i);
            int noise = Math.abs(hash(pos.x(), pos.y(), pos.z(), seed)) % 100;
            if (i > 0 && noise < 14) {
                continue;
            }
            putLeaf(parts, pos, 4);
        }
    }

    private static void addPath(Map<Vec, Part> parts, Vec... path) {
        for (int i = 1; i < path.length; i++) {
            Vec previous = path[i - 1];
            Vec current = path[i];
            putLog(parts, current, axisFor(previous, current));
        }
    }

    private static Vec trunkOffset(int y, int height, Variant variant) {
        int x = 0;
        int z = 0;
        int firstBend = Math.max(3, height / 3);
        int secondBend = Math.max(firstBend + 2, (height * 2) / 3);
        if (y >= firstBend && y < height - 1) {
            x = 1;
        }
        if (y >= secondBend) {
            z = variant == Variant.SPREADING ? 0 : 1;
        }
        if (variant == Variant.TALL && y >= secondBend + 1) {
            x = 0;
        }
        if (variant == Variant.ANCIENT && y >= 4 && y < height - 2) {
            z = 1;
            if (y >= height - 5) {
                x = -1;
            }
        }
        if (y >= height) {
            x = 0;
            z = 0;
        }
        return new Vec(x, y, z);
    }

    private static void putLog(Map<Vec, Part> parts, Vec pos, Direction.Axis axis) {
        parts.put(pos, new Part(Kind.LOG, axis, 0));
    }

    private static void putLeaf(Map<Vec, Part> parts, Vec pos, int distance) {
        parts.putIfAbsent(pos, new Part(Kind.LEAF, Direction.Axis.Y, distance));
    }

    private static Direction.Axis axisFor(Vec previous, Vec current) {
        if (previous.x() != current.x()) {
            return Direction.Axis.X;
        }
        if (previous.z() != current.z()) {
            return Direction.Axis.Z;
        }
        return Direction.Axis.Y;
    }

    private static Direction.Axis rotateAxis(Direction.Axis axis, int turns) {
        if (axis == Direction.Axis.Y || turns % 2 == 0) {
            return axis;
        }
        return axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
    }

    private static Vec rotate(Vec pos, int turns) {
        int normalized = Math.floorMod(turns, 4);
        return switch (normalized) {
            case 1 -> new Vec(-pos.z(), pos.y(), pos.x());
            case 2 -> new Vec(-pos.x(), pos.y(), -pos.z());
            case 3 -> new Vec(pos.z(), pos.y(), -pos.x());
            default -> pos;
        };
    }

    private static double square(double value) {
        return value * value;
    }

    private static int hash(int x, int y, int z, int seed) {
        int h = seed;
        h = h * 31 + x * 73428767;
        h = h * 31 + y * 912931;
        h = h * 31 + z * 438289;
        h ^= h >>> 16;
        return h;
    }

    private enum Variant {
        STANDARD,
        TALL,
        SPREADING,
        ANCIENT;

        int height(int requestedHeight, RandomSource random) {
            int requested = Math.max(6, requestedHeight);
            return switch (this) {
                case STANDARD -> clamp(requested, 7, 10);
                case TALL -> clamp(requested + 3 + random.nextInt(2), 10, 14);
                case SPREADING -> clamp(requested + 1, 8, 11);
                case ANCIENT -> clamp(requested + 5 + random.nextInt(2), 12, 16);
            };
        }

        Variant[] fallbacks() {
            return switch (this) {
                case STANDARD -> new Variant[0];
                case TALL -> new Variant[]{STANDARD};
                case SPREADING -> new Variant[]{STANDARD};
                case ANCIENT -> new Variant[]{SPREADING, TALL, STANDARD};
            };
        }

        int lowSkirtDrop() {
            return switch (this) {
                case STANDARD -> 5;
                case TALL -> 6;
                case SPREADING -> 4;
                case ANCIENT -> 7;
            };
        }
    }

    private enum Kind {
        LOG,
        LEAF
    }

    private record TreePlan(LinkedHashMap<Vec, Part> parts) {
    }

    private record Part(Kind kind, Direction.Axis axis, int leafDistance) {
    }

    private record Vec(int x, int y, int z) {
        Vec offset(int dx, int dy, int dz) {
            return new Vec(x + dx, y + dy, z + dz);
        }

        Vec above() {
            return offset(0, 1, 0);
        }

        Vec below(int amount) {
            return offset(0, -amount, 0);
        }

        Vec relative(Direction direction) {
            return offset(direction.getStepX(), direction.getStepY(), direction.getStepZ());
        }

        int horizontalDistance() {
            return Math.abs(x) + Math.abs(z);
        }

        BlockPos toBlockPos() {
            return new BlockPos(x, y, z);
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
