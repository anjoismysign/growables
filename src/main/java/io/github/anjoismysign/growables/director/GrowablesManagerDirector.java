package io.github.anjoismysign.growables.director;

import io.github.anjoismysign.bloblib.entities.GenericManagerDirector;
import io.github.anjoismysign.growables.Growables;

public class GrowablesManagerDirector extends GenericManagerDirector<Growables> {
    private final ConfigManager configManager;
    private final HookManager hookManager;
    private final StructureTracker structureTracker;
    private final GrowableManager growableManager;
    private final GrowableInstanceManager growableInstanceManager;

    public GrowablesManagerDirector(Growables blobPlugin) {
        super(blobPlugin);
        configManager = new ConfigManager(this);
        hookManager = new HookManager(this);
        structureTracker = new StructureTracker(this);
        growableManager = new GrowableManager(this);
        growableInstanceManager = new GrowableInstanceManager(this);
    }

    @Override
    public void reload() {
        growableInstanceManager.reload();
    }

    @Override
    public void unload() {
        growableInstanceManager.unload();
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public HookManager getHookManager() {
        return hookManager;
    }

    public StructureTracker getStructureTracker() {
        return structureTracker;
    }

    public GrowableManager getGrowableManager() {
        return growableManager;
    }

    public GrowableInstanceManager getGrowableInstanceManager() {
        return growableInstanceManager;
    }
}