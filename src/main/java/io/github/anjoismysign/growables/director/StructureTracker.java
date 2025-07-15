package io.github.anjoismysign.growables.director;

import io.github.anjoismysign.aesthetic.DirectoryAssistant;
import io.github.anjoismysign.bloblib.entities.GenericManager;
import io.github.anjoismysign.growables.Growables;
import org.bukkit.structure.Structure;
import org.bukkit.structure.StructureManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class StructureTracker extends GenericManager<Growables, GrowablesManagerDirector> {
    private final StructureManager structureManager = getPlugin().getServer().getStructureManager();
    private Map<String, Structure> structures;

    protected StructureTracker(GrowablesManagerDirector director) {
        super(director);
    }

    public void reload() {
        try {
            File dataFolder = getPlugin().getDataFolder();
            File structuresFolder = new File(dataFolder, "Structures");
            if (!structuresFolder.exists())
                structuresFolder.mkdirs();
            String[] extensions = {"nbt"};
//            Map<Integer, List<File>> temp = new HashMap<>();
            Collection<File> recursiveFiles = DirectoryAssistant.of(structuresFolder).listRecursively(extensions);
            Logger logger = getPlugin().getLogger();
//            recursiveFiles.forEach(file -> {
//                String path = file.getPath();
//                String name = file.getName().replace(".nbt", "");
//                String[] split = name.split("_");
//                if (split.length != 2) {
//                    logger.severe("\"" + path + "\" must only have 1 underscore ('_')");
//                    return;
//                }
//                String value = split[1];
//                int stage;
//                try {
//                    stage = Integer.parseInt(value);
//                } catch (NumberFormatException exception) {
//                    logger.severe("\"" + path + "\" must start with an integer number after the underscore ('_')");
//                    return;
//                }
//                temp.computeIfAbsent(stage, k -> new ArrayList<>()).add(file);
//            });
//            List<Integer> keys = temp.keySet().stream().sorted().toList();
//            List<File> files = new ArrayList<>();
//            keys.forEach(k -> {
//                var list = temp.get(k);
//                files.addAll(list);
//            });
            structures = new HashMap<>();
//            files.forEach(this::load);
            recursiveFiles.forEach(this::load);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    /**
     * Load a structure from a file.
     *
     * @param file The file to load the structure from.
     */
    public void load(@NotNull File file) {
        Objects.requireNonNull(file, "'file' cannot be null");
        try {
            boolean tinyDebug = getManagerDirector().getConfigManager().tinyDebug();
            String path = file.getPath();
            String[] split = path.split(Pattern.quote(File.separator));
            StringBuilder keyBuilder = new StringBuilder();
            for (int i = 3; i < split.length; i++) {
                String s = split[i];
                keyBuilder.append(s).append("/");
            }
            keyBuilder = keyBuilder.deleteCharAt(keyBuilder.length() - 1);
            String key = keyBuilder.toString().replace("output/Structures/", "");
            Structure structure;
            try {
                structure = structureManager.loadStructure(file);
            } catch (IOException exception) {
                exception.printStackTrace();
                return;
            }
//            structures.computeIfAbsent(key, k -> new ArrayList<>()).add(structure);
            structures.put(key, structure);
            if (tinyDebug)
                getPlugin().getLogger().warning("Loaded structure " + keyBuilder);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    /**
     * Get a structure by key.
     *
     * @param key The key of the structure.
     * @return The structure, or null if it does not exist.
     */
    @Nullable
    public Structure getStructure(String key) {
        return structures.get(key);
    }

}