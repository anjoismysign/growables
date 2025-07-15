package io.github.anjoismysign.growables.director;

import io.github.anjoismysign.bloblib.entities.GenericManager;
import io.github.anjoismysign.growables.Growables;
import io.github.anjoismysign.growables.phatloots.Found;
import io.github.anjoismysign.growables.phatloots.NotFound;
import io.github.anjoismysign.growables.phatloots.PhatLootsHook;
import org.bukkit.Bukkit;

public class HookManager extends GenericManager<Growables, GrowablesManagerDirector> {

    private final PhatLootsHook phatLootsHook;

    protected HookManager(GrowablesManagerDirector director) {
        super(director);
        if (Bukkit.getPluginManager().isPluginEnabled("PhatLoots")){
            phatLootsHook = new Found();
        } else {
            phatLootsHook = new NotFound();
        }
    }

    public PhatLootsHook getPhatLootsHook() {
        return phatLootsHook;
    }
}
