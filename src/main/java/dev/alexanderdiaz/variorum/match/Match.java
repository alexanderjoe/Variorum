package dev.alexanderdiaz.variorum.match;

import com.google.common.base.Preconditions;
import dev.alexanderdiaz.variorum.Variorum;
import dev.alexanderdiaz.variorum.event.match.MatchLoadEvent;
import dev.alexanderdiaz.variorum.event.match.MatchOpenEvent;
import dev.alexanderdiaz.variorum.map.VariorumMap;
import dev.alexanderdiaz.variorum.match.registry.MatchRegistry;
import dev.alexanderdiaz.variorum.module.Module;
import dev.alexanderdiaz.variorum.util.Events;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import lombok.Getter;
import lombok.ToString;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;

@ToString
public class Match {
    @Getter
    private final VariorumMap map;

    @Getter
    private final MatchFactory factory;

    @Getter
    private final MatchRegistry registry;

    @Getter
    private final String id;

    @Getter
    private boolean loaded = false;

    private final Map<Class<? extends Module>, Module> modules;
    private final List<Module> orderedModules;

    public Match(VariorumMap map, MatchFactory factory) {
        this.map = map;
        this.factory = factory;
        this.registry = new MatchRegistry(this);
        this.modules = new HashMap<>();
        this.orderedModules = new ArrayList<>();
        this.id = UUID.randomUUID().toString().substring(0, 6);
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

    // TODO: relocate these eventually
    private static final String MATCHES_FOLDER = "matches";
    private static final String VOID_GENERATOR = "VoidWorldGenerator";

    public void load() {
        try {
            String matchPath = MATCHES_FOLDER + "/" + this.id;

            File sourceWorldFolder = map.getSource().getFolder();
            if (!sourceWorldFolder.exists()) {
                throw new IllegalStateException("Source world folder does not exist: " + sourceWorldFolder);
            }

            File mapConfig = new File(sourceWorldFolder, "map.xml");
            if (!mapConfig.exists()) {
                throw new IllegalStateException("Map config does not exist: " + mapConfig);
            }
            Variorum.get().getLogger().info("Found map config at: " + mapConfig.getAbsolutePath());

            File targetWorldFolder = new File(Bukkit.getWorldContainer(), matchPath);
            targetWorldFolder.getParentFile().mkdirs();

            if (targetWorldFolder.exists()) {
                Files.walk(targetWorldFolder.toPath())
                        .sorted((a, b) -> -a.compareTo(b))
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                Variorum.get().getLogger().log(Level.WARNING, "Failed to delete: " + path, e);
                            }
                        });
            }

            Files.walk(sourceWorldFolder.toPath()).forEach(sourcePath -> {
                Path relativePath = sourceWorldFolder.toPath().relativize(sourcePath);
                Path targetPath = targetWorldFolder.toPath().resolve(relativePath);
                try {
                    if (Files.isDirectory(sourcePath)) {
                        Files.createDirectories(targetPath);
                    } else {
                        Files.copy(sourcePath, targetPath);
                    }
                } catch (IOException e) {
                    Variorum.get().getLogger().log(Level.WARNING, "Failed to copy: " + sourcePath, e);
                }
            });

            File matchesDir = new File(Bukkit.getWorldContainer(), MATCHES_FOLDER);
            if (!matchesDir.exists()) {
                matchesDir.mkdirs();
            }

            WorldCreator worldCreator = new WorldCreator(matchPath);
            ChunkGenerator voidGenerator =
                    Bukkit.getPluginManager().getPlugin(VOID_GENERATOR).getDefaultWorldGenerator(matchPath, null);

            worldCreator.generator(voidGenerator);
            World world = worldCreator.createWorld();

            if (world == null) {
                throw new IllegalStateException("Failed to load world: " + matchPath);
            }

            world.setAutoSave(false);
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
            world.setGameRule(GameRule.DO_INSOMNIA, false);
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);

            Variorum.get().getLogger().info("Loaded map config: " + map.getName());

            MatchLoadEvent matchLoadEvent = new MatchLoadEvent(this);
            Events.call(matchLoadEvent);

            Variorum.get().getLogger().info("Successfully loaded map: " + this.map.getName() + " with ID: " + this.id);
            this.loaded = true;
        } catch (Exception e) {
            Variorum.get().getLogger().log(Level.SEVERE, "Failed to load map: " + this.map.getName(), e);
        }
    }

    /** Starts the match and enables all modules. */
    public void start() {
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

        // TODO: was this needed?
        //        world.getPlayers()
        //                .forEach(player -> player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation()));

        this.loaded = false;
    }

    public void unload() {
        Bukkit.unloadWorld(getWorldName(), false);
    }

    private String getWorldName() {
        return MATCHES_FOLDER + "/" + this.id;
    }

    public World getWorld() {
        return Bukkit.getWorld(getWorldName());
    }

    public void broadcast(Component message) {
        for (Player player : this.getPlayers()) {
            player.sendMessage(message);
        }

        Bukkit.getConsoleSender().sendMessage(message);
    }
}
