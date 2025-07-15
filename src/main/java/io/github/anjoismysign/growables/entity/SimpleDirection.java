package io.github.anjoismysign.growables.entity;

import org.bukkit.block.BlockFace;
import org.bukkit.block.structure.StructureRotation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public enum SimpleDirection {
    NORTH(StructureRotation.NONE, BlockFace.NORTH, 0, 0),
    EAST(StructureRotation.CLOCKWISE_90, BlockFace.EAST, 90, 1),
    SOUTH(StructureRotation.CLOCKWISE_180, BlockFace.SOUTH, 180, 2),
    WEST(StructureRotation.COUNTERCLOCKWISE_90, BlockFace.WEST, 270, 3);

    private static final Map<StructureRotation, SimpleDirection> structureRotations = new HashMap<>();
    private static final Map<BlockFace, SimpleDirection> blockFaces = new HashMap<>();
    private static final Map<Integer, SimpleDirection> ordinals = new HashMap<>();

    static {
        for (SimpleDirection direction : values()) {
            structureRotations.put(direction.getStructureRotation(), direction);
            blockFaces.put(direction.getBlockFace(), direction);
            ordinals.put(direction.getOrdinal(), direction);
        }
    }

    private final StructureRotation structureRotation;
    private final BlockFace blockFace;
    private final float yaw;
    private final int ordinal;

    @Nullable
    public static SimpleDirection ofStructureRotation(@NotNull StructureRotation rotation) {
        Objects.requireNonNull(rotation, "'rotation' cannot be null");
        return structureRotations.get(rotation);
    }

    @Nullable
    public static SimpleDirection ofBlockFace(@NotNull BlockFace blockFace) {
        Objects.requireNonNull(blockFace, "'blockFace' cannot be null");
        return blockFaces.get(blockFace);
    }

    @Nullable
    public static SimpleDirection ofOrdinal(int ordinal) {
        return ordinals.get(ordinal);
    }

    SimpleDirection(StructureRotation rotation,
                    BlockFace blockFace,
                    float yaw,
                    int ordinal) {
        this.structureRotation = rotation;
        this.blockFace = blockFace;
        this.yaw = yaw;
        this.ordinal = ordinal;
    }

    public StructureRotation getStructureRotation() {
        return structureRotation;
    }

    public BlockFace getBlockFace() {
        return blockFace;
    }

    public float getYaw() {
        return yaw;
    }

    public int getOrdinal() {
        return ordinal;
    }
}
