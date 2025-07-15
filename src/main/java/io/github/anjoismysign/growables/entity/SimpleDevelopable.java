package io.github.anjoismysign.growables.entity;

import io.github.anjoismysign.growables.director.GrowableInstanceManager;
import org.bukkit.entity.Interaction;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Remember the plant analogy? This is the actual/physical plant viewed on a org.bukkit.World
 * @see GrowableInstanceManager.GrowableInstance
 * @param owner The GrowableInstance that owns this physical representation
 * @param boxPointer The hitbox, like for harvesting.
 * @param stagePointer The current growth stage.
 */
public record Developable(
        @NotNull GrowableInstanceManager.GrowableInstance owner,
        @NotNull Interaction[] boxPointer,
        @NotNull Integer[] stagePointer
        ) {

    public Interaction getBox() {
        return boxPointer[0];
    }

    public void setBox(Interaction interaction) {
        this.boxPointer[0] = interaction;
    }

    public int getGrowthStage() {
        return stagePointer[0];
    }

    public void advanceOneStage(){
        setStage(getGrowthStage()+1);
    }

    public void growToFullStage(){
        setStage(owner.getGrowableOrThrow().stages());
    }

    public void reset(){
        setStage(0);
    }

    private void setStage(@NotNull Integer stage) {
        Objects.requireNonNull(stage, "'stage' cannot be null");
        this.stagePointer[0] = stage;
    }
}
