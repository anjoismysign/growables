package io.github.anjoismysign.growables.entity;

import io.github.anjoismysign.growables.Growables;
import org.bukkit.structure.Structure;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Stage {
    private String structure;
    private int length;

    @NotNull
    public Structure structureOrThrow() {
        return Objects.requireNonNull(Growables.getInstance().getManagerDirector().getStructureTracker().getStructure(structure), "'" + structure + "' is not a Structure");
    }

    public @NotNull String getStructure() {
        return structure;
    }

    public int getLength() {
        return length;
    }

    public void setStructure(@NotNull String structure) {
        this.structure = structure;
    }

    public void setLength(int length) {
        this.length = length;
    }
}