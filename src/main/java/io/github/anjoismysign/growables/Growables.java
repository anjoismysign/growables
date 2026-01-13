package io.github.anjoismysign.growables;

import io.github.anjoismysign.bloblib.managers.BlobPlugin;
import io.github.anjoismysign.growables.director.GrowablesManagerDirector;
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
        director = new GrowablesManagerDirector(this);
    }

    @Override
    public @NotNull GrowablesManagerDirector getManagerDirector() {
        return director;
    }
}
