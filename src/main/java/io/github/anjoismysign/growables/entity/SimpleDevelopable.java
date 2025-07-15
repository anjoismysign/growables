package io.github.anjoismysign.growables.entity;

import io.github.anjoismysign.bloblib.entities.message.BlobSound;
import io.github.anjoismysign.bloblib.utilities.Structrador;
import io.github.anjoismysign.growables.Growables;
import io.github.anjoismysign.growables.api.Developable;
import io.github.anjoismysign.growables.api.event.DevelopableGrowEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.structure.Mirror;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Physical representation of a growable plant in the Bukkit world.
 *
 * @param owner        The GrowableInstance that owns this physical representation
 * @param boxPointer   The hitbox, used for harvesting.
 * @param stagePointer The current growth stage.
 */
public record SimpleDevelopable(
        @NotNull GrowableInstance owner,
        @NotNull Interaction[] boxPointer,
        @NotNull Stage[] stagePointer,
        @NotNull List<Entity> entities,
        int[] timerPointer,
        @NotNull Hologram hologram,
        BukkitTask[] taskPointer
) implements Developable {

    public static SimpleDevelopable of(@NotNull GrowableInstance instance) {
        Location baseLocation = instance.locatable().toLocation().toCenterLocation();
        baseLocation.setY(baseLocation.getBlockY());
        Location hologramLocation = baseLocation.clone();
        hologramLocation.setY(
                hologramLocation.getBlockY()
                        + Growables.getInstance().getManagerDirector()
                        .getConfigManager().getConfiguration()
                        .getHolograms().getY()
        );

        Hologram hologram = Hologram.random(hologramLocation);
        Growable growableDefinition = instance.getGrowableOrThrow();
        List<Stage> stages = growableDefinition.stages();
        Stage initialStage = stages.getLast();

        Interaction interaction = (Interaction)
                baseLocation.getWorld().spawnEntity(baseLocation, EntityType.INTERACTION);
        interaction.setPersistent(false);
        interaction.setInteractionHeight(0.00001F);

        List<Entity> entities = new ArrayList<>();
        entities.add(interaction);

        Interaction[] boxPointer = new Interaction[]{interaction};
        Stage[] stagePointer = new Stage[]{initialStage};
        int[] timerPointer = new int[]{0};
        BukkitTask[] taskPointer = new BukkitTask[]{null};

        SimpleDevelopable developable = new SimpleDevelopable(
                instance, boxPointer, stagePointer, entities, timerPointer, hologram, taskPointer
        );

        developable.restartTask();
        hologram.update(developable);

        return developable;
    }

    private static void placeStructure(
            @NotNull Stage stage,
            @NotNull GrowableInstance instance,
            @NotNull SimpleDevelopable developable
    ) {
        Location structureLocation = instance.locatable().toLocation();
        new Structrador(stage.structureOrThrow(), Growables.getInstance())
                .chainedPlace(
                        structureLocation,
                        true,
                        instance.direction().getStructureRotation(),
                        Mirror.NONE,
                        0,
                        1,
                        new Random(),
                        Integer.MAX_VALUE,
                        1,
                        blockState -> {
                        },
                        entity -> {
                            entity.setPersistent(false);
                            developable.entities.add(entity);
                        }
                );
    }

    /**
     * Starts or restarts the growth task.
     * Advances through the first (N–1) stages proportionally,
     * then calls growToFullStage() at 100% and stops.
     */
    private void restartTask() {
        if (taskPointer[0] != null) {
            taskPointer[0].cancel();
        }

        List<Stage> allStages = owner.getGrowableOrThrow().stages();
        int totalStages = allStages.size();
        int finalStageIndex = totalStages - 1;
        int growthStageCount = finalStageIndex;

        // Sum only the lengths of the first (totalStages - 1) stages
        int totalTicksForProgress = allStages.subList(0, growthStageCount).stream()
                .mapToInt(Stage::getLength)
                .sum();

        timerPointer[0] = 0;

        taskPointer[0] = new BukkitRunnable() {
            private int lastAppliedStageIndex = -1;

            @Override
            public void run() {
                int currentTick = timerPointer[0]++;

                if (currentTick % 10 == 0) {
                    hologram.update(SimpleDevelopable.this);
                }

                int cappedTick = Math.min(currentTick, totalTicksForProgress);
                double progressFraction = (double) cappedTick / totalTicksForProgress;

                int desiredStageIndex;
                if (cappedTick >= totalTicksForProgress) {
                    // At or past 100%, finalize growth
                    desiredStageIndex = finalStageIndex;
                } else {
                    desiredStageIndex = (int) (progressFraction * growthStageCount);
                }

                if (desiredStageIndex != lastAppliedStageIndex) {
                    // If we hit the final stage, use growToFullStage()
                    if (desiredStageIndex == finalStageIndex) {
                        growToFullStage();
                        this.cancel();
                        return;
                    }
                    setStage(desiredStageIndex);
                    lastAppliedStageIndex = desiredStageIndex;
                }
            }
        }.runTaskTimer(Growables.getInstance(), 0L, 1L);
    }

    public void clear() {
        taskPointer[0].cancel();
        removeEntitiesExceptHitbox();
        getHitbox().remove();
        @Nullable TextDisplay display = hologram().display();
        if (display == null) {
            return;
        }
        display.remove();
    }

    private void removeEntitiesExceptHitbox() {
        entities.stream()
                .filter(entity -> entity.getType() != EntityType.INTERACTION)
                .toList()
                .forEach(entity -> {
                    entities.remove(entity);
                    entity.remove();
                });
    }

    public int getStageIndex() {
        return owner.getGrowableOrThrow()
                .stages().indexOf(stagePointer[0]);
    }

    public boolean advanceOneStage() {
        int currentIndex = getStageIndex();
        int lastIndex = owner.getGrowableOrThrow().totalStages() - 1;
        if (currentIndex >= lastIndex) {
            taskPointer[0].cancel();
            return false;
        }
        setStage(currentIndex + 1);
        return true;
    }

    public void growToFullStage() {
        taskPointer[0].cancel();
        setStage(owner.getGrowableOrThrow().totalStages() - 1);
        getHitbox().setInteractionHeight(owner.getGrowableOrThrow().height());
    }

    public int getLastStageIndex() {
        return owner.getGrowableOrThrow().totalStages() - 1;
    }

    public @NotNull Interaction getHitbox() {
        return Objects.requireNonNull(boxPointer[0], "Hitbox is unexpectedly null");
    }

    public void resetGrowth() {
        setStage(0);
        getHitbox().setInteractionHeight(0.00001f);
        restartTask();
        hologram.update(this);
    }

    public void setStage(int stageIndex) {
        DevelopableGrowEvent growEvent = new DevelopableGrowEvent(this, stageIndex);
        Bukkit.getPluginManager().callEvent(growEvent);

        removeEntitiesExceptHitbox();

        Stage newStage = owner.getGrowableOrThrow()
                .stages().get(growEvent.getStage());
        this.stagePointer[0] = newStage;

        placeStructure(newStage, owner, this);
    }

    public String growthProgressBar() {
        List<Stage> allStages = owner.getGrowableOrThrow().stages();
        int finalStageIndex = allStages.size() - 1;
        int growthStageCount = finalStageIndex;

        // Sum only the first N–1 stages for “progress”
        int totalTicksForProgress = allStages.subList(0, growthStageCount).stream()
                .mapToInt(Stage::getLength)
                .sum();

        // Count completed ticks, capped at totalTicksForProgress
        int completedTicks = Math.min(timerPointer[0], totalTicksForProgress);

        double percentage = ((double) completedTicks / totalTicksForProgress) * 100.0;
        return new DecimalFormat("#,##0.##").format(percentage);
    }

    public void harvest(@NotNull Player player,
                        @NotNull Location location) {
        resetGrowth();
        String reference = owner.getGrowableOrThrow().blobSound();
        BlobSound blobSound = BlobSound.by(reference);
        blobSound.handle(player, location);
        Growables.getInstance().getManagerDirector().getHookManager().getPhatLootsHook().lootAt(
                location,
                owner.getGrowableOrThrow().phatLoot(),
                player);
    }
}
