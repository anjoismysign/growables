package io.github.anjoismysign.growables.entity;

import io.github.anjoismysign.bloblib.entities.positionable.Locatable;
import io.github.anjoismysign.bloblib.entities.positionable.Positionable;
import io.github.anjoismysign.bloblib.exception.ConfigurationFieldException;
import io.github.anjoismysign.growables.Growables;
import io.github.anjoismysign.growables.director.GrowableInstanceManager;
import io.github.anjoismysign.holoworld.asset.DataAsset;
import io.github.anjoismysign.holoworld.asset.IdentityGenerator;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public record Growable(
        @NotNull String identifier,
        @NotNull String phatLoot,
        @NotNull List<Stage> stages,
        @NotNull String blobSound,
        float height
) implements DataAsset {

    public int totalStages() {
        return stages.size();
    }

    public Info info() {
        var x = new Info();
        x.setStages(stages);
        x.setPhatLoot(phatLoot);
        x.setBlobSound(blobSound);
        x.setHeight(height);
        return x;
    }

    public void instantiate(@NotNull Block block,
                            @NotNull SimpleDirection direction) {
        Location location = block.getLocation();
        Locatable locatable = (Locatable) Positionable.of(block.getLocation());
        String identifier = locatable.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
        GrowableInstance instance = new GrowableInstance(
                locatable,
                this.identifier,
                identifier,
                direction,
                new int[] {0}
        );
        GrowableInstanceManager manager = Growables.getInstance().getManagerDirector().getGrowableInstanceManager();
        manager.serialize(instance);
        manager.registerDevelopable(SimpleDevelopable.of(instance));
    }

    public static class Info implements IdentityGenerator<Growable> {

        private List<Stage> stages;
        private String phatLoot;
        private String blobSound;
        private float height;

        @Override
        public @NotNull Growable generate(@NotNull String identifier) {
            Objects.requireNonNull(stages, "'stages' cannot be null");
            Objects.requireNonNull(phatLoot, "'phatLoot' cannot be null");
            Objects.requireNonNull(blobSound, "'blobSound' cannot be null");
            if (height <= 0.0){
                throw new ConfigurationFieldException("'height' must be greater than 0!");
            }
            return new Growable(identifier, phatLoot, stages, blobSound, height);
        }

        public List<Stage> getStages() {
            return stages;
        }

        public void setStages(List<Stage> stages) {
            this.stages = stages;
        }

        public String getPhatLoot() {
            return phatLoot;
        }

        public void setPhatLoot(String phatLoot) {
            this.phatLoot = phatLoot;
        }

        public String getBlobSound() {
            return blobSound;
        }

        public void setBlobSound(String blobSound) {
            this.blobSound = blobSound;
        }

        public float getHeight() {
            return height;
        }

        public void setHeight(float height) {
            this.height = height;
        }
    }
}