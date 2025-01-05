package dev.alexanderdiaz.variorum.map.rotation;

import dev.alexanderdiaz.variorum.Variorum;
import dev.alexanderdiaz.variorum.match.MatchFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public abstract class AbstractRotationProvider implements RotationProvider {
    protected final MatchFactory factory;
    protected final Variorum plugin;

    protected AbstractRotationProvider(MatchFactory factory, Variorum plugin) {
        this.factory = factory;
        this.plugin = plugin;
    }

    protected Rotation defaultRotation() {
        List<String> maps = new ArrayList<>();
        File mapsFolder = new File(plugin.getDataFolder(), "maps");

        if (!mapsFolder.exists() || !mapsFolder.isDirectory()) {
            plugin.getLogger().log(Level.WARNING, "Maps folder not found or is not a directory");
            return new Rotation(maps);
        }

        File[] mapFolders = mapsFolder.listFiles(File::isDirectory);
        if (mapFolders == null) {
            plugin.getLogger().log(Level.WARNING, "Failed to list map folders");
            return new Rotation(maps);
        }

        for (File mapFolder : mapFolders) {
            if (new File(mapFolder, "map.xml").exists()) {
                maps.add(mapFolder.getName());
            }
        }

        if (maps.isEmpty()) {
            plugin.getLogger().log(Level.WARNING, "No valid maps found in maps directory");
        } else {
            plugin.getLogger().log(Level.INFO, "Loaded " + maps.size() + " maps into rotation");
        }

        return new Rotation(maps);
    }
}