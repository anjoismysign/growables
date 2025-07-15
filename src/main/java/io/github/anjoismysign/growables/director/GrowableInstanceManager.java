package io.github.anjoismysign.growables.director;

import io.github.anjoismysign.bloblib.entities.GenericManager;
import io.github.anjoismysign.bloblib.entities.translatable.TranslatableItem;
import io.github.anjoismysign.bloblib.managers.PluginManager;
import io.github.anjoismysign.bloblib.managers.asset.BukkitIdentityManager;
import io.github.anjoismysign.growables.Growables;
import io.github.anjoismysign.growables.api.event.DevelopableHarvestEvent;
import io.github.anjoismysign.growables.entity.Growable;
import io.github.anjoismysign.growables.entity.GrowableInstance;
import io.github.anjoismysign.growables.entity.SimpleDevelopable;
import io.github.anjoismysign.growables.entity.SimpleDirection;
import io.github.anjoismysign.holoworld.asset.IdentityGeneration;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class GrowableInstanceManager extends GenericManager<Growables, GrowablesManagerDirector> implements Listener {
    private final BukkitIdentityManager<GrowableInstance> identityManager;
    private final Set<SimpleDevelopable> developables = new HashSet<>();

    public GrowableInstanceManager(GrowablesManagerDirector director) {
        super(director);
        identityManager = PluginManager.getInstance().addIdentityManager(GrowableInstance.Info.class, getPlugin(), "GrowableInstance", true);
        Bukkit.getPluginManager().registerEvents(this, getPlugin());
        Bukkit.getScheduler().runTask(getPlugin(), this::reload);
    }

    public void reload() {
        developables.forEach(SimpleDevelopable::clear);
        developables.clear();
        GrowablesManagerDirector director = getManagerDirector();
        director.getStructureTracker().reload();
        director.getGrowableManager().identityManager.reload();
        identityManager.reload();
        identityManager.forEach(instance -> {
            SimpleDevelopable developable = SimpleDevelopable.of(instance);
            developables.add(developable);
        });
    }

    public void unload(){
        developables.forEach(SimpleDevelopable::clear);
    }

    public void registerDevelopable(@NotNull SimpleDevelopable developable){
        Objects.requireNonNull(developable, "'developable' cannot be null");
        developables.add(developable);
    }

    @Nullable
    public GrowableInstance get(@NotNull String identifier) {
        var generation = identityManager.fetchGeneration(identifier);
        if (generation == null)
            return null;
        return generation.asset();
    }

    public void serialize(@NotNull GrowableInstance instance) {
        Objects.requireNonNull(instance, "'owner' cannot be null");
        String identifier = instance.identifier();
        identityManager.add(new IdentityGeneration<>(identifier, instance.info()));
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
        if (!developable.taskPointer()[0].isCancelled()){
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
