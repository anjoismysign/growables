package io.github.anjoismysign.growables.director;

import com.google.gson.Gson;
import io.github.anjoismysign.bloblib.entities.GenericManager;
import io.github.anjoismysign.bloblib.entities.translatable.TranslatableItem;
import io.github.anjoismysign.growables.Growables;
import io.github.anjoismysign.growables.api.event.DevelopableHarvestEvent;
import io.github.anjoismysign.growables.entity.Growable;
import io.github.anjoismysign.growables.entity.GrowableInstance;
import io.github.anjoismysign.growables.entity.GrowableShard;
import io.github.anjoismysign.growables.entity.SimpleDevelopable;
import io.github.anjoismysign.growables.entity.SimpleDirection;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class GrowableInstanceManager extends GenericManager<Growables, GrowablesManagerDirector> implements Listener {
    private final File shardsDirectory;
    private final Map<String, GrowableShard> shards;
    private final Set<SimpleDevelopable> developables = new HashSet<>();
    private BukkitTask tickTask;

    public GrowableInstanceManager(GrowablesManagerDirector director) {
        super(director);
        shardsDirectory = new File(director.getPlugin().getDataFolder(), "developable");
        shards = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, getPlugin());

        Bukkit.getScheduler().runTask(getPlugin(), this::reload);
    }

    public void reload() {
        unload();
        developables.clear();
        tickTask = Bukkit.getScheduler().runTaskTimer(getPlugin(), () -> {
            developables.forEach(SimpleDevelopable::randomTick);
        }, 0L, 20L);
        GrowablesManagerDirector director = getManagerDirector();
        director.getStructureTracker().reload();
        director.getGrowableManager().identityManager.reload();
        if (!shardsDirectory.isDirectory())
            shardsDirectory.mkdirs();
        Bukkit.getWorlds().forEach(world -> {
            String worldName = world.getName();
            File shardFile = new File(shardsDirectory, worldName+".json");
            Gson gson = new Gson();
            if (!shardFile.isFile()){
                GrowableShard shard = new GrowableShard(worldName, new HashMap<>());
                try (FileWriter writer = new FileWriter(shardFile)) {
                    gson.toJson(shard.info(), writer);
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
                shards.put(worldName, shard);
            } else {
                try (FileReader reader = new FileReader(shardFile)){
                    GrowableShard.Info info = gson.fromJson(reader, GrowableShard.Info.class);
                    GrowableShard shard = info.generate(worldName);
                    shards.put(worldName, shard);
                } catch (IOException exception){
                    exception.printStackTrace();
                }
            }
        });
        shards.forEach((worldName, shard)->{
            shard.instances().forEach((blockvector, instance)->{
                SimpleDevelopable developable = SimpleDevelopable.of(instance);
                developables.add(developable);
            });
        });
    }

    public void unload(){
        if (tickTask != null) {
            tickTask.cancel();
        }
        developables.forEach(SimpleDevelopable::unload);
        shards.forEach((worldName, shard)->{
            File shardFile = new File(shardsDirectory, worldName+".json");
            Gson gson = new Gson();
            try (FileWriter writer = new FileWriter(shardFile)) {
                gson.toJson(shard.info(), writer);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
            shards.put(worldName, shard);
        });
    }

    public void registerDevelopable(@NotNull SimpleDevelopable developable){
        Objects.requireNonNull(developable, "'developable' cannot be null");
        developables.add(developable);
    }

    public void serialize(@NotNull GrowableInstance instance) {
        Objects.requireNonNull(instance, "'owner' cannot be null");
        String identifier = instance.identifier();
        String worldName = instance.locatable().getWorld().getName();
        @Nullable GrowableShard shard = shards.get(worldName);
        Objects.requireNonNull(shard, "'shard' cannot be null");
        shard.add(instance);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event){
        Player player = event.getPlayer();
        ItemStack hand = event.getItemInHand();
        TranslatableItem item = TranslatableItem.byItemStack(hand);
        if (item == null)
            return;
        String identifier = item.identifier();
        @Nullable Growable growable = getManagerDirector().getGrowableManager().get(identifier);
        if (growable == null)
            return;
        Block block = event.getBlockPlaced();
        BlockFace facing = player.getFacing();
        SimpleDirection direction = SimpleDirection.ofBlockFace(facing);
        growable.instantiate(block, direction);
    }

    @EventHandler
    public void onHarvest(EntityDamageByEntityEvent event) {
        if (event.getDamager().getType() != EntityType.PLAYER)
            return;
        Entity entity = event.getEntity();
        if (entity.getType() != EntityType.INTERACTION)
            return;
        Interaction interaction = (Interaction) entity;
        @Nullable SimpleDevelopable developable = developables
                .stream()
                .filter(simpleDevelopable -> simpleDevelopable.getHitbox().getUniqueId().equals(interaction.getUniqueId()))
                .findFirst().orElse(null);
        if (developable == null) {
            return;
        }
        if (developable.getStageIndex() != developable.getLastStageIndex()){
            return;
        }
        Player player = (Player) event.getDamager();
        DevelopableHarvestEvent harvestEvent = new DevelopableHarvestEvent(developable, player);
        Bukkit.getPluginManager().callEvent(harvestEvent);
        if (harvestEvent.isCancelled()) {
            return;
        }
        developable.harvest(player, entity.getLocation());
    }
}
