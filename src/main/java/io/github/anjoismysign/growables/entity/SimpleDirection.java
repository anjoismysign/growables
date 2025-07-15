package io.github.anjoismysign.growables.entity;

import org.bukkit.block.BlockFace;
import org.bukkit.block.structure.StructureRotation;

public enum Direction {
    NORTH(StructureRotation.NONE, BlockFace.NORTH, 0, 0),
    EAST(StructureRotation.CLOCKWISE_90, BlockFace.EAST, 90, 1),
    SOUTH(StructureRotation.CLOCKWISE_180, BlockFace.SOUTH, 180, 2),
    WEST(StructureRotation.COUNTERCLOCKWISE_90, BlockFace.WEST, 270, 3);

    private final StructureRotation rotation;
    private final BlockFace blockFace;
    private final float yaw;
    private final int ordinal;

    Direction(StructureRotation rotation,
              BlockFace blockFace,
              float yaw,
              int ordinal){
        this.rotation = rotation;
        this.blockFace = blockFace;
        this.yaw = yaw;
        this.ordinal = ordinal;
    }

    public StructureRotation getRotation() {
        return rotation;
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
