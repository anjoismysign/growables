package io.github.anjoismysign.growables.director;

import io.github.anjoismysign.bloblib.annotation.BManager;
import io.github.anjoismysign.bloblib.entities.ConfigDecorator;
import io.github.anjoismysign.bloblib.managers.Manager;
import io.github.anjoismysign.bloblib.managers.ManagerDirector;
import io.github.anjoismysign.growables.Growables;
import org.bukkit.configuration.ConfigurationSection;

public class ConfigManager extends Manager {

    private static ConfigManager INSTANCE;
    private boolean tinyDebug;

    protected ConfigManager(ManagerDirector director){
        super(director);
        INSTANCE = this;
    }

    public static ConfigManager getInstance() {
        return INSTANCE;
    }

    public void reload() {
        ConfigDecorator configDecorator = getPlugin().getConfigDecorator();
        ConfigurationSection settingsSection = configDecorator.reloadAndGetSection("Settings");
        tinyDebug = settingsSection.getBoolean("Tiny-Debug");
    }

    public boolean tinyDebug() {
        return tinyDebug;
    }
}
