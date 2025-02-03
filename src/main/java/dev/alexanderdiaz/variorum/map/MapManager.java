package dev.alexanderdiaz.variorum.map;

import dev.alexanderdiaz.variorum.Variorum;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

public class MapManager {
    private static final String MAP_CONFIG_NAME = "map.xml";
    private final Map<String, VariorumMap> mapLibrary;
    private final File libraryDirectory;

    public MapManager(String libraryPath) {
        this.mapLibrary = new HashMap<>();
        this.libraryDirectory = new File(libraryPath);

        if (!libraryDirectory.exists() || !libraryDirectory.isDirectory()) {
            throw new IllegalArgumentException("Invalid library path: " + libraryPath);
        }
    }

    public void loadLibrary() {
        mapLibrary.clear();
        searchForMaps(libraryDirectory);
    }

    private void searchForMaps(File directory) {
        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                File mapConfig = new File(file, MAP_CONFIG_NAME);
                if (mapConfig.exists()) {
                    try {
                        VariorumMap map = VariorumMapFactory.load(mapConfig);
                        mapLibrary.put(map.getName(), map);
                    } catch (Exception e) {
                        Variorum.get().getLogger().log(Level.WARNING, "Failed to load map: " + file.getName(), e);
                    }
                }
                searchForMaps(file);
            }
        }
    }

    public Optional<VariorumMap> getMapByName(String name) {
        return Optional.ofNullable(mapLibrary.get(name));
    }

    public Map<String, VariorumMap> getAllMaps() {
        return new HashMap<>(mapLibrary);
    }
}
