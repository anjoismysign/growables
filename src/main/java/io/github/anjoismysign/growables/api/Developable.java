package io.github.anjoismysign.growables.api;

import org.bukkit.entity.Interaction;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an object that can progress through multiple growth stages
 * and can be interacted with. Implementations define how growth stages
 * are managed, interacted with, and how interaction is handled.
 */
public interface Developable {

    /**
     * Gets the current growth stage of this object.
     *
     * @return the current growth stage as an integer
     */
    int getStageIndex();

    /**
     * Advances the object by one growth stage if possible.
     *
     * @return true if the stage was advanced, false if already at the final stage
     */
    boolean advanceOneStage();

    /**
     * Grows the object to its maximum growth stage.
     */
    void growToFullStage();

    /**
     * Gets the last stage index
     * @return the last stage index
     */
    int getLastStageIndex();

    /**
     * Gets the hitbox associated with this object for interaction purposes.
     *
     * @return a non-null {@link Interaction} representing the hitbox
     */
    @NotNull
    Interaction getHitbox();

}
