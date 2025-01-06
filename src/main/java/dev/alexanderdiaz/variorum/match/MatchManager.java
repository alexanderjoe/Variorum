package dev.alexanderdiaz.variorum.match;

import dev.alexanderdiaz.variorum.Variorum;
import dev.alexanderdiaz.variorum.event.match.MatchLoadEvent;
import dev.alexanderdiaz.variorum.map.VariorumMap;
import dev.alexanderdiaz.variorum.map.VariorumMapFactory;
import dev.alexanderdiaz.variorum.map.rotation.DefaultRotationProvider;
import dev.alexanderdiaz.variorum.map.rotation.RotationProvider;
import dev.alexanderdiaz.variorum.util.Events;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.generator.ChunkGenerator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.logging.Level;

public class MatchManager {
    private final Variorum plugin;
    private final MatchFactory matchFactory;
    private final RotationProvider rotationProvider;

    @Getter
    private Match currentMatch;
    private static final String MATCHES_FOLDER = "matches";
    private static final String VOID_GENERATOR = "VoidWorldGenerator";

    public MatchManager(Variorum plugin) {
        this.plugin = plugin;
        this.matchFactory = new MatchFactory();
        this.rotationProvider = new DefaultRotationProvider(matchFactory, plugin);
    }

    public void cycleToNextMatch() {
        Match oldMatch = currentMatch;

        String nextMap = rotationProvider.provideRotation().getNextMap();
        loadMap(nextMap);

        if (currentMatch != null && oldMatch != null) {
            oldMatch.end();
        }
    }

    public void loadMap(String mapName) {
        // Don't end the current match here - it will be ended after player transfer

        try {
            String matchId = UUID.randomUUID().toString().substring(0, 8).toLowerCase();
            String matchWorldName = mapName.toLowerCase() + "_" + matchId;
            String matchPath = MATCHES_FOLDER + "/" + matchWorldName;

            File sourceWorldFolder = new File(plugin.getDataFolder(), "maps" + File.separator + mapName);
            if (!sourceWorldFolder.exists()) {
                throw new IllegalStateException("Source world folder does not exist: " + sourceWorldFolder);
            }

            // Get map config
            File mapConfig = new File(sourceWorldFolder, "map.xml");
            if (!mapConfig.exists()) {
                throw new IllegalStateException("Map config does not exist: " + mapConfig);
            }
            plugin.getLogger().info("Found map config at: " + mapConfig.getAbsolutePath());

            File targetWorldFolder = new File(Bukkit.getWorldContainer(), matchPath);
            targetWorldFolder.getParentFile().mkdirs();

            if (targetWorldFolder.exists()) {
                Files.walk(targetWorldFolder.toPath())
                        .sorted((a, b) -> -a.compareTo(b))
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                plugin.getLogger().log(Level.WARNING, "Failed to delete: " + path, e);
                            }
                        });
            }

            Files.walk(sourceWorldFolder.toPath())
                    .forEach(sourcePath -> {
                        Path relativePath = sourceWorldFolder.toPath().relativize(sourcePath);
                        Path targetPath = targetWorldFolder.toPath().resolve(relativePath);
                        try {
                            if (Files.isDirectory(sourcePath)) {
                                Files.createDirectories(targetPath);
                            } else {
                                Files.copy(sourcePath, targetPath);
                            }
                        } catch (IOException e) {
                            plugin.getLogger().log(Level.WARNING, "Failed to copy: " + sourcePath, e);
                        }
                    });

            // Ensure matches directory exists
            File matchesDir = new File(Bukkit.getWorldContainer(), MATCHES_FOLDER);
            if (!matchesDir.exists()) {
                matchesDir.mkdirs();
            }

            // Load world with void generator
            WorldCreator worldCreator = new WorldCreator(matchPath);
            ChunkGenerator voidGenerator = Bukkit.getPluginManager()
                    .getPlugin(VOID_GENERATOR)
                    .getDefaultWorldGenerator(matchPath, null);

            worldCreator.generator(voidGenerator);
            World world = worldCreator.createWorld();

            if (world == null) {
                throw new IllegalStateException("Failed to load world: " + matchPath);
            }

            world.setAutoSave(false);

            // Load the map config
            VariorumMap map = VariorumMapFactory.load(mapConfig);
            plugin.getLogger().info("Loaded map config: " + map.getName());

            // Create match with factory
            currentMatch = matchFactory.create(map, mapConfig, world);
            // Start the match (which enables modules)
            currentMatch.start();

            MatchLoadEvent matchLoadEvent = new MatchLoadEvent(currentMatch);
            Events.call(matchLoadEvent);

            plugin.getLogger().info("Successfully loaded map: " + mapName + " with ID: " + matchId);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load map: " + mapName, e);
            e.printStackTrace();
        }
    }
}