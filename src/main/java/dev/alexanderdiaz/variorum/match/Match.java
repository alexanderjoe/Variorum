package dev.alexanderdiaz.variorum.match;

import com.google.common.base.Preconditions;
import dev.alexanderdiaz.variorum.Variorum;
import dev.alexanderdiaz.variorum.event.match.MatchOpenEvent;
import dev.alexanderdiaz.variorum.map.VariorumMap;
import dev.alexanderdiaz.variorum.match.registry.MatchRegistry;
import dev.alexanderdiaz.variorum.module.Module;
import dev.alexanderdiaz.variorum.util.Events;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import lombok.Getter;
import lombok.ToString;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

@ToString
public class Match {
    @Getter
    private final VariorumMap map;

    @Getter
    private final MatchRegistry registry;

    @Getter
    private final World world;

    @Getter
    private boolean active = true;

    private final Map<Class<? extends Module>, Module> modules;
    private final List<Module> orderedModules;

    public Match(VariorumMap map, World world) {
        this.map = map;
        this.registry = new MatchRegistry(this);
        this.world = world;
        this.modules = new HashMap<>();
        this.orderedModules = new ArrayList<>();
    }

    public Collection<Player> getPlayers() {
        return new ArrayList<>(Bukkit.getOnlinePlayers());
    }

    /**
     * Adds a module to the match.
     *
     * @param module The module to add
     * @param <T> The type of module
     * @return The added module
     */
    public <T extends Module> T addModule(T module) {
        modules.put(module.getClass(), module);
        orderedModules.add(module);
        return module;
    }

    /**
     * Gets a module by its class.
     *
     * @param clazz The class of the module
     * @param <T> The type of module
     * @return The module if present
     */
    @SuppressWarnings("unchecked")
    public <T extends Module> Optional<T> getModule(Class<T> clazz) {
        return Optional.ofNullable((T) modules.get(clazz));
    }

    public <T extends Module> T getRequiredModule(Class<T> type) {
        Optional<T> module = getModule(type);
        Preconditions.checkArgument(module.isPresent(), "Required module is not present.");
        return module.get();
    }

    /** Starts the match and enables all modules. */
    public void start() {
        if (!active) return;

        for (Module module : orderedModules) {
            try {
                module.enable();
            } catch (Exception e) {
                Variorum.get()
                        .getLogger()
                        .log(
                                Level.SEVERE,
                                "Failed to enable module " + module.getClass().getSimpleName(),
                                e);
            }
        }

        MatchOpenEvent moe = new MatchOpenEvent(this);
        Events.call(moe);
    }

    /** Ends the match, disables all modules, and cleans up resources. */
    public void end() {
        if (!active) return;
        active = false;

        for (int i = orderedModules.size() - 1; i >= 0; i--) {
            Module module = orderedModules.get(i);
            try {
                module.disable();
            } catch (Exception e) {
                Variorum.get()
                        .getLogger()
                        .log(
                                Level.SEVERE,
                                "Failed to disable module " + module.getClass().getSimpleName(),
                                e);
            }
        }

        modules.clear();
        orderedModules.clear();

        world.getPlayers()
                .forEach(player -> player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation()));

        Bukkit.unloadWorld(world, false);

        try {
            Path worldPath = world.getWorldFolder().toPath();
            Files.walk(worldPath).sorted((a, b) -> -a.compareTo(b)).forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    Variorum.get().getLogger().log(Level.WARNING, "Failed to delete: " + path, e);
                }
            });
        } catch (IOException e) {
            Variorum.get().getLogger().log(Level.SEVERE, "Failed to clean up world files", e);
        }
    }

    public void broadcast(Component message) {
        for (Player player : this.getPlayers()) {
            player.sendMessage(message);
        }

        Bukkit.getConsoleSender().sendMessage(message);
    }
}
