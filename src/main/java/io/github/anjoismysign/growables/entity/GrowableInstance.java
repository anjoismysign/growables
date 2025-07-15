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
            @NotNull SimpleDirection direction
    ) implements DataAsset {

        @NotNull
        public Growable getGrowableOrThrow() {
            return Objects.requireNonNull(Growables.getInstance().getManagerDirector().getGrowableManager().get(this.growable), "'growable' doesn't point to a valid Growable");
        }

        public Info info() {
            var x = new Info();
            x.setGrowable(growable);
            x.setWorld(locatable.getWorld().getName());
            x.setYaw(locatable.getYaw());
            x.setPitch(locatable.getPitch());
            x.setX(locatable.getX());
            x.setY(locatable.getY());
            x.setZ(locatable.getZ());
            x.setDirection(direction);
            return x;
        }

    public static class Info implements IdentityGenerator<GrowableInstance> {
        private String world, growable;
        private float yaw, pitch;
        private double x, y, z;
        private SimpleDirection direction;

        @Override
        public @NotNull GrowableInstance generate(@NotNull String identifier) {
            Objects.requireNonNull(world, "'world' cannot be null");
            Objects.requireNonNull(growable, "'growable' cannot be null");
            Objects.requireNonNull(direction, "'direction' cannot be null");
            Locatable locatable = new Locatable() {
                @Override
                public double getX() {
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
                    return yaw;
                }

                @Override
                public float getPitch() {
                    return pitch;
                }

                @Override
                public @NotNull World getWorld() {
                    return Objects.requireNonNull(Bukkit.getWorld(world), "'" + world + "' was deleted during runtime");
                }
            };
            return new GrowableInstance(locatable, growable, identifier, direction);
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

        public float getYaw() {
            return yaw;
        }

        public void setYaw(float yaw) {
            this.yaw = yaw;
        }

        public float getPitch() {
            return pitch;
        }

        public void setPitch(float pitch) {
            this.pitch = pitch;
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }

        public double getZ() {
            return z;
        }

        public void setZ(double z) {
            this.z = z;
        }

        public SimpleDirection getDirection() {
            return direction;
        }

        public void setDirection(SimpleDirection direction) {
            this.direction = direction;
        }
    }

    }