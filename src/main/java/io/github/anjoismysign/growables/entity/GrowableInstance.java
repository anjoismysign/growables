package io.github.anjoismysign.growables.entity;

import io.github.anjoismysign.bloblib.entities.positionable.Locatable;
import io.github.anjoismysign.growables.Growables;
import io.github.anjoismysign.holoworld.asset.DataAsset;
import io.github.anjoismysign.holoworld.asset.IdentityGenerator;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * The class that holds the data used to instantiate a Developable.
 * <p>
 * We should imagine it as a plant that has growth stages.
 * It may only be harvested on last stage, then it resets to first stage.
 *
 * @param locatable  The Locatable holding the real Location
 * @param growable   The Growable identifier
 * @param identifier The GrowableInstance identifier
 * @param direction  The direction used for creating Developables
 */
public record GrowableInstance(
        @NotNull Locatable locatable,
        @NotNull String growable,
        @NotNull String identifier,
        @NotNull SimpleDirection direction,
        int[] stagePointer
) implements DataAsset {

    public int stage(){
        return stagePointer[0];
    }

    @NotNull
    public Growable getGrowableOrThrow() {
        return Objects.requireNonNull(
                Growables.getInstance()
                        .getManagerDirector()
                        .getGrowableManager()
                        .get(this.growable),
                "'growable' doesn't point to a valid Growable"
        );
    }

    public Info info() {
        var x = new Info();
        x.setGrowable(growable);
        x.setWorld(locatable.getWorld().getName());
        x.setX((int) locatable.getX());
        x.setY((int) locatable.getY());
        x.setZ((int) locatable.getZ());
        x.setDirection(direction);
        x.setStage(stage());
        return x;
    }

    public static class Info {
        private String world, growable, identifier;
        private int x, y, z, stage;
        private SimpleDirection direction;

        public @NotNull GrowableInstance generate() {
            Objects.requireNonNull(world, "'world' cannot be null");
            Objects.requireNonNull(growable, "'growable' cannot be null");
            Objects.requireNonNull(direction, "'direction' cannot be null");

            Locatable locatable = new Locatable() {
                @Override
                public double getX() {
                    // return int as double
                    return x;
                }

                @Override
                public double getY() {
                    return y;
                }

                @Override
                public double getZ() {
                    return z;
                }

                @Override
                public float getYaw() {
                    return 0;
                }

                @Override
                public float getPitch() {
                    return 0;
                }

                @Override
                public @NotNull World getWorld() {
                    return Objects.requireNonNull(
                            Bukkit.getWorld(world),
                            "'" + world + "' was deleted during runtime"
                    );
                }
            };

            return new GrowableInstance(locatable, growable, identifier, direction, new int[]{stage});
        }

        public String getWorld() {
            return world;
        }

        public void setWorld(String world) {
            this.world = world;
        }

        public String getGrowable() {
            return growable;
        }

        public void setGrowable(String growable) {
            this.growable = growable;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getZ() {
            return z;
        }

        public void setZ(int z) {
            this.z = z;
        }

        public SimpleDirection getDirection() {
            return direction;
        }

        public void setDirection(SimpleDirection direction) {
            this.direction = direction;
        }

        public int getStage() {
            return stage;
        }

        public void setStage(int stage) {
            this.stage = stage;
        }

        public String getIdentifier() {
            return identifier;
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }
    }
}
