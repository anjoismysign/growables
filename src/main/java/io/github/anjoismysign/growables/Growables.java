package io.github.anjoismysign.growables;

import io.github.anjoismysign.bloblib.managers.BlobPlugin;
import io.github.anjoismysign.bloblib.managers.IManagerDirector;
import io.github.anjoismysign.growables.director.GrowablesManagerDirector;
import org.bukkit.plugin.java.JavaPlugin;

public class BlobGrowables
        extends BlobPlugin {

    private GrowablesManagerDirector director;
    private IManagerDirector proxy;

    public static BlobGrowables getInstance() {
        return JavaPlugin.getPlugin(BlobGrowables.class);
    }

    @Override
    public void onEnable() {
        this.director = new GrowablesManagerDirector(this);
        this.proxy = director.proxy();
    }

    @Override
    public void onDisable() {
        director.unload();
    }

    @Override
    public IManagerDirector getManagerDirector() {
        return proxy;
    }

}
