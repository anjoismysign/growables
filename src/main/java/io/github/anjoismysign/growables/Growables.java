package io.github.anjoismysign.growables;

import io.github.anjoismysign.bloblib.managers.BlobPlugin;
import io.github.anjoismysign.bloblib.managers.PluginManager;
import io.github.anjoismysign.bloblib.managers.asset.BukkitIdentityManager;
import io.github.anjoismysign.growables.director.GrowablesManagerDirector;
import io.github.anjoismysign.growables.entity.Growable;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class Growables
        extends BlobPlugin {

    private GrowablesManagerDirector director;

    public static Growables getInstance() {
        return JavaPlugin.getPlugin(Growables.class);
    }

    @Override
    public void onEnable() {
        BukkitIdentityManager<Growable> growableIdentityManager = PluginManager.getInstance().addIdentityManager(Growable.Info.class, this, "Growable", false);
        director = new GrowablesManagerDirector(this);
    }

    @Override
    public @NotNull GrowablesManagerDirector getManagerDirector() {
        return director;
    }
}
