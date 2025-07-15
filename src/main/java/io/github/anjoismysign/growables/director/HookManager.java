package io.github.anjoismysign.growables.director;

import io.github.anjoismysign.bloblib.annotation.BManager;
import io.github.anjoismysign.bloblib.entities.ConfigDecorator;
import io.github.anjoismysign.bloblib.managers.Manager;
import org.bukkit.configuration.ConfigurationSection;

@BManager
public class ConfigManager extends Manager {

    private static ConfigManager INSTANCE;
    private boolean tinyDebug;

    public static ConfigManager getInstance() {
        return INSTANCE;
    }

    public ConfigManager() {
        INSTANCE = this;
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
