package io.github.anjoismysign.growables.entity;

import io.github.anjoismysign.bloblib.entities.message.BlobSound;
import io.github.anjoismysign.bloblib.utilities.Structrador;
import io.github.anjoismysign.growables.Growables;
import io.github.anjoismysign.growables.api.Developable;
import io.github.anjoismysign.growables.api.event.DevelopableGrowEvent;
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
 *
 * @param owner        The GrowableInstance that owns this physical representation
 * @param boxPointer   The hitbox, used for harvesting.
 * @param stageIndexPointer The current growth stage.
 */
public record SimpleDevelopable(
        @NotNull GrowableInstance owner,
        @NotNull Interaction[] boxPointer,
        int[] stageIndexPointer,
        @NotNull List<Entity> entities,
        @NotNull Hologram hologram,
        @NotNull Random random
        ) implements Developable {

    public static SimpleDevelopable of(@NotNull GrowableInstance instance) {
        int currentStage = instance.stage();

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

        Interaction interaction = (Interaction)
                baseLocation.getWorld().spawnEntity(baseLocation, EntityType.INTERACTION);
        interaction.setPersistent(false);

        Interaction[] boxPointer = new Interaction[]{interaction};
        int[] stageIndexPointer = new int[]{currentStage};

        SimpleDevelopable developable = new SimpleDevelopable(
                instance,
                boxPointer,
                stageIndexPointer,
                new ArrayList<>(),
                hologram,
                new Random()
        );

        int lastStage = developable.getLastStageIndex();
        if (currentStage == lastStage){
            developable.growToFullStage();
            hologram.update(developable);
        } else {
            developable.setStage(currentStage);
            developable.getHitbox().setInteractionHeight(0.00001f);
            hologram.update(developable);
        }
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
                        ThreadLocalRandom.current(),
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

    /** Called by the shared task in the manager to simulate random-tick growth. */
    public void randomTick() {
        int currentIndex = getStageIndex();
        int lastIndex = getLastStageIndex();
        if (currentIndex >= lastIndex){
            return;
        }

        Stage current = owner.getGrowableOrThrow().stages().get(currentIndex);
        int length = current.getLength();

        @Nullable Integer randomTickSpeed = owner.locatable().getWorld().getGameRuleValue(GameRule.RANDOM_TICK_SPEED);
        randomTickSpeed = randomTickSpeed == null ? 3 : randomTickSpeed;
        randomTickSpeed = randomTickSpeed == 0 ? 3 : randomTickSpeed;
        int chance = length / randomTickSpeed;
        if (chance > 0 && random.nextInt(chance) == 0) {
            advanceOneStage();
            hologram.update(this);
        }
    }

    public void unload() {
        owner.stagePointer()[0] = getStageIndex();
        Growables.getInstance().getManagerDirector().getGrowableInstanceManager().serialize(owner);
        removeEntities();
        getHitbox().remove();
        @Nullable TextDisplay display = hologram.display();
        if (display == null) {
            return;
        }
        display.remove();
    }

    private void removeEntities() {
        entities.stream()
                .toList()
                .forEach(entity -> {
                    entities.remove(entity);
                    entity.remove();
                });
    }

    public int getStageIndex() {
        return stageIndexPointer[0];
    }

    public boolean advanceOneStage() {
        int currentIndex = getStageIndex();
        int lastIndex = getLastStageIndex();
        if (currentIndex > lastIndex) {
            return false;
        }
        int desiredIndex = currentIndex+1;
        if (desiredIndex == lastIndex){
            growToFullStage();
            return true;
        }
        setStage(desiredIndex);
        return true;
    }

    public void growToFullStage() {
        setStage(getLastStageIndex());
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
        hologram.update(this);
    }

    public void setStage(int stageIndex) {
        DevelopableGrowEvent growEvent = new DevelopableGrowEvent(this, stageIndex);
        Bukkit.getPluginManager().callEvent(growEvent);

        removeEntities();

        int eventStage = growEvent.getStage();
        Stage newStage = owner.getGrowableOrThrow()
                .stages().get(eventStage);
        stageIndexPointer[0] = eventStage;

        placeStructure(newStage, owner, this);
    }

    public String growthProgressBar() {
        // optional: you can still compute an approximate progress display if desired
        return new DecimalFormat("#,##0.##").format(
                (double) getStageIndex() / getLastStageIndex() * 100.0
        );
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
