package io.github.anjoismysign.growables.director;

import io.github.anjoismysign.bloblib.annotation.BManager;
import io.github.anjoismysign.bloblib.managers.Manager;

@BManager
public class ConfigManager extends Manager {



    public ConfigManager(){
    }

    private static ConfigManager INSTANCE;

    public static ConfigManager getInstance(){
        if (INSTANCE == null)
            INSTANCE = new ConfigManager();
        return INSTANCE;
    }

}
