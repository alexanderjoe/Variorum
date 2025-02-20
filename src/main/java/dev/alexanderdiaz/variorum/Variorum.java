package dev.alexanderdiaz.variorum;

import dev.alexanderdiaz.variorum.command.util.VariorumCommandGraph;
import dev.alexanderdiaz.variorum.listener.VariorumListener;
import dev.alexanderdiaz.variorum.map.MapManager;
import dev.alexanderdiaz.variorum.map.rotation.DefaultRotationProvider;
import dev.alexanderdiaz.variorum.map.rotation.JsonFileRotationProvider;
import dev.alexanderdiaz.variorum.map.rotation.Rotation;
import dev.alexanderdiaz.variorum.map.rotation.RotationProvider;
import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.match.MatchFactory;
import dev.alexanderdiaz.variorum.match.MatchManager;
import dev.alexanderdiaz.variorum.util.Events;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import javax.annotation.Nullable;
import lombok.Getter;
import net.megavex.scoreboardlibrary.api.ScoreboardLibrary;
import net.megavex.scoreboardlibrary.api.exception.NoPacketAdapterAvailableException;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Variorum extends JavaPlugin {

    private static Variorum instance;
    private VariorumCommandGraph commandGraph;

    @Getter
    private MapManager mapManager;

    @Getter
    private MatchManager matchManager;

    @Getter
    private MatchFactory matchFactory;

    @Getter
    private ScoreboardLibrary scoreboardLibrary;

    private final File ROTATION_FILE = new File(getDataFolder(), "rotation.json");

    public static Variorum get() {
        return instance;
    }

    @Nullable public static Match getMatch() {
        return get().getMatchManager().getRotation().getMatch();
    }

    @Override
    public void onEnable() {
        instance = this;

        String mapLibrary = get().getDataFolder().toPath().resolve("maps").toString();
        this.mapManager = new MapManager(mapLibrary);
        this.mapManager.loadLibrary();
        this.matchFactory = new MatchFactory();

        final RotationProvider rotationProvider =
                new JsonFileRotationProvider(ROTATION_FILE, this.mapManager, this.matchFactory);
        Rotation rotation;
        try {
            rotation = rotationProvider.provideRotation();
        } catch (IllegalStateException e) {
            rotation = new DefaultRotationProvider(this.mapManager, this.matchFactory).provideRotation();
            getLogger().log(Level.WARNING, "Failed to load rotation from file: " + e.getMessage());
        }
        this.matchManager = new MatchManager(this.matchFactory, rotation);

        createDirectories();
        Events.register(new VariorumListener());
        loadScoreboardComponent();

        try {
            this.matchManager.start();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Failed to start rotation", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try {
            this.commandGraph = new VariorumCommandGraph(this);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Exception while registering commands", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (this.matchManager != null) {
            this.matchManager.shutdown();
        }

        scoreboardLibrary.close();
        cleanupOldMatches();
    }

    private void loadScoreboardComponent() {
        try {
            scoreboardLibrary = ScoreboardLibrary.loadScoreboardLibrary(instance);
        } catch (NoPacketAdapterAvailableException e) {
            instance.getLogger().log(Level.SEVERE, "Exception while loading scoreboard library", e);
        }
    }

    private void createDirectories() {
        try {
            Path dataFolder = getDataFolder().toPath();
            Files.createDirectories(dataFolder.resolve("maps"));
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Failed to create plugin directories", e);
        }
    }

    private void cleanupOldMatches() {
        Path matchesPath = Bukkit.getWorldContainer().toPath().resolve("matches");

        if (!Files.exists(matchesPath)) {
            return;
        }

        try {
            Bukkit.getWorlds().stream()
                    .filter(world -> world.getName().startsWith("matches/"))
                    .forEach(world -> {
                        getLogger().info("Unloading world: " + world.getName());
                        Bukkit.unloadWorld(world, false);
                    });

            System.gc();

            Files.walk(matchesPath).sorted((a, b) -> -a.compareTo(b)).forEach(path -> {
                try {
                    Files.delete(path);
                    getLogger().fine("Deleted: " + path);
                } catch (IOException e) {
                    getLogger().warning("Failed to delete: " + path + " - " + e.getMessage());
                }
            });

            Files.createDirectories(matchesPath);
            getLogger().info("Cleaned up matches directory");

        } catch (IOException e) {
            getLogger().severe("Failed to clean up old matches: " + e.getMessage());
        }
    }
}
