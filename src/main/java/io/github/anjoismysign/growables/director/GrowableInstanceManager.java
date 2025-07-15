package io.github.anjoismysign.growables.director;

import io.github.anjoismysign.bloblib.annotation.BManager;
import io.github.anjoismysign.bloblib.entities.positionable.Locatable;
import io.github.anjoismysign.bloblib.managers.Manager;
import io.github.anjoismysign.bloblib.managers.PluginManager;
import io.github.anjoismysign.bloblib.managers.asset.BukkitIdentityManager;
import io.github.anjoismysign.growables.Growables;
import io.github.anjoismysign.growables.entity.Growable;
import io.github.anjoismysign.holoworld.asset.DataAsset;
import io.github.anjoismysign.holoworld.asset.IdentityGeneration;
import io.github.anjoismysign.holoworld.asset.IdentityGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@BManager
public class LocatableGrowableManager extends Manager {
    private final BukkitIdentityManager<GrowableInstance> identityManager;

    public LocatableGrowableManager(){
        identityManager = PluginManager.getInstance().addIdentityManager(Info.class, Growables.getInstance(), "GrowableInstance", true);
    }

    @Nullable
    public GrowableInstance getGrowable(@NotNull String identifier){
        var generation = identityManager.fetchGeneration(identifier);
        if (generation == null)
            return null;
        return generation.asset();
    }

    public void serialize(@NotNull GrowableInstance growable){
        Objects.requireNonNull(growable, "'growable' cannot be null");
        String identifier = growable.identifier();
        identityManager.add(new IdentityGeneration<>(identifier, growable.info()));
    }

    private static LocatableGrowableManager INSTANCE;

    public static LocatableGrowableManager getInstance(){
        if (INSTANCE == null)
            INSTANCE = new LocatableGrowableManager();
        return INSTANCE;
    }

    public record GrowableInstance(@NotNull Locatable locatable,
                                   @NotNull String growable,
                                   @NotNull String identifier) implements DataAsset {

        public Location location(){
            return locatable.toLocation();
        }

        @Nullable
        public GrowableManager.Growable getGrowable(){
            GrowableManager.Growable growable = GrowableManager.getInstance().getGrowable(this.growable);
        }

        public Info info(){
            Info info = new Info();
            info.setGrowable(growable);
            info.setWorld(locatable.getWorld().getName());
            info.setYaw(locatable.getYaw());
            info.setPitch(locatable.getPitch());
            info.setX(locatable.getX());
            info.setY(locatable.getY());
            info.setZ(locatable.getZ());
            return info;
        }

    }

    public static class Info implements IdentityGenerator<GrowableInstance> {
        private String world, growable;
        private float yaw,pitch;
        private double x,y,z;

        @Override
        public @NotNull LocatableGrowableManager.GrowableInstance generate(@NotNull String identifier) {
            Objects.requireNonNull(world, "'world' cannot be null");
            Objects.requireNonNull(growable, "'growable' cannot be null");
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
                    return Objects.requireNonNull(Bukkit.getWorld(world), "'"+world+"' was deleted during runtime");
                }
            };
            return new GrowableInstance(locatable, growable, identifier);
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
    }

}
