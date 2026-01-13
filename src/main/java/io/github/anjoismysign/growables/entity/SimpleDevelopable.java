package io.github.anjoismysign.growables.entity;

import io.github.anjoismysign.bloblib.entities.message.BlobSound;
import io.github.anjoismysign.growables.Growables;
import io.github.anjoismysign.growables.api.Developable;
import io.github.anjoismysign.growables.api.event.DevelopableGrowEvent;
import io.github.anjoismysign.util.Structrador;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.block.structure.Mirror;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Physical representation of a growable plant in the Bukkit world.
 * This class manages the visual stage, interaction hitbox, and lifecycle of a growable.
 */
public class SimpleDevelopable implements Developable {

    private static final float MIN_HITBOX_HEIGHT = 0.00001f; // Used to effectively hide the hitbox when growth is reset

    private final @NotNull GrowableInstance owner;
    private final @NotNull Interaction hitbox; // The physical hitbox entity for interaction
    private @NotNull Stage currentStage; // The current growth stage of the developable
    private final @NotNull List<Entity> entities; // Entities placed as part of the structure, for cleanup
    private final @NotNull Hologram hologram; // The hologram displaying growth progress
    private final @NotNull Random random; // Random instance for tick-based growth

    /**
     * Private constructor to ensure instances are created via the static factory method `of()`.
     *
     * @param owner The GrowableInstance that owns this physical representation.
     * @param hitbox The interaction hitbox for this developable.
     * @param currentStage The initial growth stage.
     * @param entities A list to track entities spawned as part of the structure.
     * @param hologram The hologram associated with this developable.
     * @param random The random number generator for growth mechanics.
     */
    private SimpleDevelopable(@NotNull GrowableInstance owner,
                              @NotNull Interaction hitbox,
                              @NotNull Stage currentStage,
                              @NotNull List<Entity> entities,
                              @NotNull Hologram hologram,
                              @NotNull Random random) {
        this.owner = Objects.requireNonNull(owner, "'owner' cannot be null");
        this.hitbox = Objects.requireNonNull(hitbox, "'hitbox' cannot be null");
        this.currentStage = Objects.requireNonNull(currentStage, "'currentStage' cannot be null");
        this.entities = Objects.requireNonNull(entities, "'entities' cannot be null");
        this.hologram = Objects.requireNonNull(hologram, "'hologram' cannot be null");
        this.random = Objects.requireNonNull(random, "'random' cannot be null");
    }

    /**
     * Factory method to create a new SimpleDevelopable instance.
     * This method handles the initial setup including creating the hitbox, hologram,
     * and placing the initial structure. By default, new developables are created
     * at their fully grown stage.
     *
     * @param instance The GrowableInstance data for the developable.
     * @return A newly created and initialized SimpleDevelopable.
     */
    public static SimpleDevelopable of(@NotNull GrowableInstance instance) {
        // Calculate base location for the structure and hitbox
        Location baseLocation = instance.locatable().toLocation().toCenterLocation();
        baseLocation.setY(baseLocation.getBlockY()); // Align to block bottom for structure placement

        // Get the definition of the growable from its identifier
        Growable growableDefinition = instance.getGrowableOrThrow();

        // Create the hitbox entity
        Interaction interactionHitbox = createHitbox(baseLocation);

        // Create the hologram at an appropriate offset height
        Hologram hologram = createHologramAtLocation(baseLocation, growableDefinition.height());

        // Initialize with the first stage, as growToFullStage() will immediately update it.
        Stage initialStageForConstructor = growableDefinition.stages().get(0);

        SimpleDevelopable developable = new SimpleDevelopable(
                instance,
                interactionHitbox,
                initialStageForConstructor, // Will be updated by growToFullStage()
                new ArrayList<>(),
                hologram,
                new Random()
        );

        // Immediately grow to the full stage for initial placement as per original logic.
        // This will also place the structure for the last stage.
        developable.growToFullStage();
        developable.hologram.update(developable); // Update hologram after initial stage is set
        return developable;
    }

    /**
     * Creates and returns an Interaction hitbox entity at the given location.
     *
     * @param location The location to spawn the hitbox.
     * @return The spawned Interaction entity.
     */
    private static @NotNull Interaction createHitbox(@NotNull Location location) {
        Interaction interaction = (Interaction) location.getWorld().spawnEntity(location, EntityType.INTERACTION);
        interaction.setPersistent(false); // Ensure the hitbox is removed when the plugin unloads or server restarts without explicit saving
        return interaction;
    }

    /**
     * Creates and returns a Hologram instance at a location offset from the base.
     * The offset is determined by configuration and the growable's height.
     *
     * @param baseLocation The base location for the hologram.
     * @param growableHeight The height of the growable structure for additional Y offset.
     * @return A new Hologram instance.
     */
    private static @NotNull Hologram createHologramAtLocation(@NotNull Location baseLocation, float growableHeight) {
        Location hologramLocation = baseLocation.clone();
        // Offset the hologram's Y position based on configurable height and growable's height
        hologramLocation.setY(
                hologramLocation.getBlockY()
                        + Growables.getInstance().getManagerDirector()
                        .getConfigManager().getConfiguration()
                        .getHolograms().getY()
                        + growableHeight // Add growable height for better positioning above the plant
        );
        return Hologram.random(hologramLocation);
    }

    /**
     * Places the structure of the given stage at the instance's location.
     * Any entities spawned by the structure are tracked for later removal.
     *
     * @param stage The Stage whose structure is to be placed.
     * @param instance The GrowableInstance providing the location and direction.
     * @param developable The SimpleDevelopable instance to add spawned entities to.
     */
    private static void placeStructure(
            @NotNull Stage stage,
            @NotNull GrowableInstance instance,
            @NotNull SimpleDevelopable developable
    ) {
        Location structureLocation = instance.locatable().toLocation();
        new Structrador(stage.structureOrThrow(), Growables.getInstance())
                .chainedPlace(
                        structureLocation,
                        true, // Ignore entities in world (don't overwrite)
                        instance.direction().getStructureRotation(),
                        Mirror.NONE, // No mirroring applied
                        0, // Palette index (usually 0 for default)
                        1, // Integrity (1 = 100% of blocks/entities placed)
                        ThreadLocalRandom.current(), // Random instance for integrity
                        Integer.MAX_VALUE, // Limit entities spawned (no practical limit here)
                        1, // Max blocks placed per operation (1 for simplicity)
                        blockState -> {
                            // This lambda is called for each block placed. Can be used for custom block handling.
                            // Currently, no custom logic is applied here.
                        },
                        entity -> {
                            // This lambda is called for each entity spawned by the structure.
                            entity.setPersistent(false); // Make the entity non-persistent so it doesn't save with the world
                            developable.entities.add(entity); // Track the entity for later cleanup when the growable is removed
                        }
                );
    }

    /**
     * Called by the shared task in the manager to simulate random-tick growth.
     * Progresses the growable by one stage based on a random chance and world's random tick speed.
     */
    public void randomTick() {
        int currentIndex = getStageIndex();
        int lastIndex = getLastStageIndex();

        // If already at or past the last stage, no further growth is needed.
        if (currentIndex >= lastIndex) {
            return;
        }

        // Get the length (growth time/difficulty) of the current stage.
        int length = currentStage.getLength();

        // Determine the world's random tick speed.
        // If not set or 0, default to 3 (Minecraft's default).
        @Nullable Integer randomTickSpeed = owner.locatable().getWorld().getGameRuleValue(GameRule.RANDOM_TICK_SPEED);
        randomTickSpeed = (randomTickSpeed == null || randomTickSpeed == 0) ? 3 : randomTickSpeed;

        // Calculate the chance for a stage advancement.
        // A higher 'length' means it takes longer, resulting in a higher number needed for the random roll (less likely).
        // A higher 'randomTickSpeed' means more ticks occur, making it more likely to advance (lower number for the roll).
        int chance = length / randomTickSpeed;

        // If chance is positive, roll the dice. If random.nextInt(chance) is 0, it advances.
        // This creates a 1/chance probability.
        if (chance > 0 && random.nextInt(chance) == 0) {
            advanceOneStage(); // Attempt to advance the stage
            hologram.update(this); // Update the hologram to reflect new progress
        }
    }

    /**
     * Clears all spawned entities, removes the hitbox, and the hologram associated with this developable.
     * This method should be called when the growable is removed from the world or unloaded.
     */
    public void clear() {
        removeEntities(); // Remove all entities placed by the structure
        getHitbox().remove(); // Remove the interaction hitbox from the world
        @Nullable TextDisplay display = hologram.display();
        if (display != null) {
            display.remove(); // Remove the hologram text display from the world
        }
    }

    /**
     * Removes all entities currently tracked by this developable (i.e., those spawned by its structure).
     */
    private void removeEntities() {
        // Iterate through a copy of the list to avoid ConcurrentModificationException when removing from the original list
        // while iterating over it.
        new ArrayList<>(entities).forEach(entity -> {
            entities.remove(entity); // Remove from our tracking list
            entity.remove(); // Remove the entity from the world
        });
    }

    @Override
    public int getStageIndex() {
        // Get the definition of the growable and find the 0-based index of the current stage.
        return owner.getGrowableOrThrow()
                .stages().indexOf(currentStage);
    }

    @Override
    public boolean advanceOneStage() {
        int currentIndex = getStageIndex();
        int lastIndex = getLastStageIndex();

        // If already at or past the last stage, cannot advance further.
        if (currentIndex >= lastIndex) {
            return false;
        }

        int desiredIndex = currentIndex + 1;
        // If the desired index is the last stage, grow directly to full.
        // Otherwise, set to the next incremental stage.
        setStage(desiredIndex);
        return true;
    }

    @Override
    public void growToFullStage() {
        setStage(getLastStageIndex()); // Set the stage to the maximum possible stage
        // Set the hitbox height to the growable's full height, making it fully interactable.
        getHitbox().setInteractionHeight(owner.getGrowableOrThrow().height());
    }

    @Override
    public int getLastStageIndex() {
        // The last stage index is the size of the stages list minus one (because it's 0-indexed).
        return owner.getGrowableOrThrow().totalStages() - 1;
    }

    @Override
    public @NotNull Interaction getHitbox() {
        return Objects.requireNonNull(hitbox, "Hitbox is unexpectedly null after initialization. This indicates a severe error.");
    }

    /**
     * Resets the growable back to its first stage and adjusts its hitbox.
     */
    public void resetGrowth() {
        setStage(0); // Set to the first stage (index 0)
        // Set the hitbox height to a minimal value to make it effectively hidden/non-interactable when reset.
        getHitbox().setInteractionHeight(MIN_HITBOX_HEIGHT);
        hologram.update(this); // Update the hologram to reflect the reset progress
    }

    /**
     * Sets the growable to a specific stage index, updates its physical structure in the world,
     * and fires a growth event to allow external interactions.
     *
     * @param stageIndex The 0-based index of the stage to set.
     */
    public void setStage(int stageIndex) {
        // 1. Fire a custom event to allow other plugins to interact or potentially cancel/modify the stage change.
        DevelopableGrowEvent growEvent = new DevelopableGrowEvent(this, stageIndex);
        Bukkit.getPluginManager().callEvent(growEvent);

        // If the event was cancelled, or if an external plugin altered the target stage, use the effective stage.
        int effectiveStageIndex = growEvent.getStage();

        // 2. Clear existing entities from the previous stage's structure to prevent duplicates or visual glitches.
        removeEntities();

        // 3. Get the new Stage object based on the effective stage index from the growable definition.
        Stage newStage = owner.getGrowableOrThrow()
                .stages().get(effectiveStageIndex);

        // 4. Update the internal reference to the current stage.
        this.currentStage = newStage;

        // 5. Place the structure for the new stage in the world at the growable's location.
        placeStructure(newStage, owner, this);
    }

    /**
     * Generates a string representing the growth progress as a percentage.
     *
     * @return A formatted string showing current progress, e.g., "50.00%".
     */
    public String growthProgressBar() {
        int lastStage = getLastStageIndex();
        // Avoid division by zero if there's only one stage (last stage index would be 0).
        if (lastStage == 0) {
            return "100.00%"; // Always 100% if it's a single-stage growable.
        }
        return new DecimalFormat("#,##0.##").format(
                (double) getStageIndex() / lastStage * 100.0
        ) + "%"; // Append percentage symbol for clarity
    }

    /**
     * Handles the harvesting logic for the developable.
     * This includes resetting its growth, playing associated sounds, and distributing loot
     * via the PhatLoots hook.
     *
     * @param player The player who harvested the developable.
     * @param location The location where the harvesting occurred (typically the growable's base location).
     */
    public void harvest(@NotNull Player player,
                        @NotNull Location location) {
        resetGrowth(); // Reset the growable to its initial stage (usually stage 0)

        // Play the associated BlobSound defined for this growable.
        String reference = owner.getGrowableOrThrow().blobSound();
        BlobSound blobSound = BlobSound.by(reference);
        blobSound.handle(player, location);

        // Distribute loot using the PhatLoots integration.
        Growables.getInstance().getManagerDirector().getHookManager().getPhatLootsHook().lootAt(
                location,
                owner.getGrowableOrThrow().phatLoot(),
                player);
    }
}