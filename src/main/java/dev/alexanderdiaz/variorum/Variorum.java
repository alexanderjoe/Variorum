package dev.alexanderdiaz.variorum;

import dev.alexanderdiaz.variorum.command.util.VariorumCommandGraph;
import dev.alexanderdiaz.variorum.listener.VariorumListener;
import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.match.MatchManager;
import dev.alexanderdiaz.variorum.util.Events;
import lombok.Getter;
import net.megavex.scoreboardlibrary.api.ScoreboardLibrary;
import net.megavex.scoreboardlibrary.api.exception.NoPacketAdapterAvailableException;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

public final class Variorum extends JavaPlugin {
    private static Variorum instance;
    private VariorumCommandGraph commandGraph;

    @Getter
    private MatchManager matchManager;

    @Getter
    private ScoreboardLibrary scoreboardLibrary;

    public static Variorum get() {
        return instance;
    }

    @Nullable
    public static Match getMatch() {
        return get().getMatchManager().getCurrentMatch();
    }

    @Override
    public void onEnable() {
        instance = this;
        this.matchManager = new MatchManager(this);

        createDirectories();

        // Commands
        try {
            this.commandGraph = new VariorumCommandGraph(this);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Exception while registering commands", e);
            getServer().getPluginManager().disablePlugin(this);
        }
        // Listeners
        Events.register(new VariorumListener());

        getServer().getScheduler().runTaskLater(get(), () -> {
            matchManager.cycleToNextMatch();
        }, 20);

        try {
            scoreboardLibrary = ScoreboardLibrary.loadScoreboardLibrary(instance);
        } catch (NoPacketAdapterAvailableException e) {
            instance.getLogger().log(Level.SEVERE, "Exception while loading scoreboard library", e);
        }
    }

    @Override
    public void onDisable() {
        if (matchManager.getCurrentMatch() != null) {
            matchManager.getCurrentMatch().end();
        }

        scoreboardLibrary.close();
        cleanupOldMatches();
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

            Files.walk(matchesPath)
                    .sorted((a, b) -> -a.compareTo(b)) // Reverse order to delete contents first
                    .forEach(path -> {
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
