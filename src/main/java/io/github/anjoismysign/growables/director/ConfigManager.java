package io.github.anjoismysign.growables.director;

import io.github.anjoismysign.bloblib.entities.GenericManager;
import io.github.anjoismysign.growables.Growables;
import io.github.anjoismysign.growables.configuration.GrowablesConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ConfigManager extends GenericManager<Growables, GrowablesManagerDirector> {

    private boolean tinyDebug;
    private GrowablesConfiguration configuration;

    protected ConfigManager(GrowablesManagerDirector director) {
        super(director);
        reload();
    }

    public void reload() {
        Growables plugin = getPlugin();
        plugin.saveResource("config.yml", false);
        plugin.saveResource("setup.yml", false);

        FileConfiguration config = plugin.getConfig();
        tinyDebug = config.getBoolean("Options.Tiny-Debug", false);

        File hologramConfigFile = new File(plugin.getDataFolder(), "setup.yml");
        Constructor constructor = new Constructor(GrowablesConfiguration.class, new LoaderOptions());
        Yaml yaml = new Yaml(constructor);
        try (FileInputStream inputStream = new FileInputStream(hologramConfigFile)) {
            configuration = yaml.load(inputStream);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public boolean tinyDebug() {
        return tinyDebug;
    }

    public GrowablesConfiguration getConfiguration() {
        return configuration;
    }
}
