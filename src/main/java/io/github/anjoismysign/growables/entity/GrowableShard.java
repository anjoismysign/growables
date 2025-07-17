package io.github.anjoismysign.growables.entity;

import io.github.anjoismysign.holoworld.asset.DataAsset;
import io.github.anjoismysign.holoworld.asset.IdentityGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record GrowableShard(@NotNull String worldName,
                            @NotNull Map<BlockVector, GrowableInstance> instances)
        implements DataAsset {

    @Nullable
    public World getWorld() {
        return Bukkit.getWorld(worldName);
    }

    @Nullable
    public GrowableInstance instanceOfLocation(@Nullable Location location) {
        if (location == null)
            return null;
        BlockVector blockVector = location.toVector().toBlockVector();
        return instances.get(blockVector);
    }

    public void add(@NotNull GrowableInstance instance){
        Objects.requireNonNull(instance, "'instance' cannot be null");
        BlockVector blockVector = instance.locatable().toLocation().toVector().toBlockVector();
        instances.put(blockVector, instance);
    }

    public void add(@NotNull GrowableInstance.Info info){
        Objects.requireNonNull(info, "'info' cannot be null");
        add(info.generate());
    }

    @Override
    public @NotNull String identifier() {
        return worldName;
    }

    public Info info() {
        Info info = new Info();
        info.setWorld(worldName);
        info.setInstances(instances.values().stream().map(GrowableInstance::info).toList());
        return info;
    }

    public static class Info implements IdentityGenerator<GrowableShard> {
        private String world;
        private List<GrowableInstance.Info> instances;

        public String getWorld() {
            return world;
        }

        public void setWorld(String world) {
            this.world = world;
        }

        @Override
        public @NotNull GrowableShard generate(@NotNull String identifier) {
            GrowableShard shard = new GrowableShard(world, new HashMap<>());
            instances.forEach(shard::add);
            return shard;
        }

        public List<GrowableInstance.Info> getInstances() {
            return instances;
        }

        public void setInstances(List<GrowableInstance.Info> instances) {
            this.instances = instances;
        }
    }
}
