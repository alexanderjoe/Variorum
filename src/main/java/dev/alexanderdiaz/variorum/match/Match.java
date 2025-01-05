package dev.alexanderdiaz.variorum.match;

import dev.alexanderdiaz.variorum.Variorum;
import dev.alexanderdiaz.variorum.map.VariorumMap;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

public class Match {
    @Getter
    private final VariorumMap map;
    @Getter
    private final World world;
    private boolean active = true;

    public Match(VariorumMap map, World world) {
        this.map = map;
        this.world = world;
    }

    public void end() {
        if (!active) return;
        active = false;

        // Teleport all players out
        world.getPlayers().forEach(player ->
                player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation()));

        // Unload world
        Bukkit.unloadWorld(world, false);

        // Delete world files
        try {
            Path worldPath = world.getWorldFolder().toPath();
            Files.walk(worldPath)
                    .sorted((a, b) -> -a.compareTo(b))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            Variorum.get().getLogger()
                                    .log(Level.WARNING, "Failed to delete: " + path, e);
                        }
                    });
        } catch (IOException e) {
            Variorum.get().getLogger()
                    .log(Level.SEVERE, "Failed to clean up world files", e);
        }
    }

    @Override
    public String toString() {
        return "Match{" +
                "map=" + map +
                ", world=" + world +
                ", active=" + active +
                '}';
    }
}
