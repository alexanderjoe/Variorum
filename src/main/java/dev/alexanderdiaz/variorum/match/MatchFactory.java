package dev.alexanderdiaz.variorum.match;

import dev.alexanderdiaz.variorum.Variorum;
import dev.alexanderdiaz.variorum.map.VariorumMap;
import dev.alexanderdiaz.variorum.module.Module;
import dev.alexanderdiaz.variorum.module.ModuleFactory;
import dev.alexanderdiaz.variorum.module.chat.ChatFactory;
import dev.alexanderdiaz.variorum.module.loadouts.LoadoutsFactory;
import dev.alexanderdiaz.variorum.module.objectives.ObjectivesFactory;
import dev.alexanderdiaz.variorum.module.results.ResultsFactory;
import dev.alexanderdiaz.variorum.module.scoreboard.ScoreboardFactory;
import dev.alexanderdiaz.variorum.module.spawn.SpawnFactory;
import dev.alexanderdiaz.variorum.module.state.GameStateFactory;
import dev.alexanderdiaz.variorum.module.team.TeamsModuleFactory;
import lombok.Getter;
import org.bukkit.World;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;
import java.util.logging.Level;

public class MatchFactory {
    private final Map<Class<? extends ModuleFactory<?>>, ModuleFactory<?>> factories;
    @Getter
    private List<ModuleFactory<?>> orderedFactories;

    public MatchFactory() {
        this.factories = new HashMap<>();
        this.orderedFactories = new ArrayList<>();

        // Register module factories
        registerDefaults();
    }

    private void registerDefaults() {
        // Register all module factories
        register(TeamsModuleFactory.class);
        register(SpawnFactory.class);
        register(ChatFactory.class);
        register(GameStateFactory.class);
        register(ObjectivesFactory.class);
        register(ResultsFactory.class);
        register(ScoreboardFactory.class);
        register(LoadoutsFactory.class);
    }

    public <F extends ModuleFactory<M>, M extends Module> void register(Class<F> clazz) {
        try {
            ModuleFactory<?> factory = clazz.getDeclaredConstructor().newInstance();
            factories.put(clazz, factory);
            orderedFactories.add(factory);
            Variorum.get().getLogger().info("Registered module factory: " + clazz.getSimpleName());
        } catch (Exception e) {
            throw new RuntimeException("Failed to register module factory: " + clazz.getName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public <F extends ModuleFactory<M>, M extends Module> F getFactory(Class<F> clazz) {
        ModuleFactory<?> factory = factories.get(clazz);
        if (factory == null) {
            throw new IllegalStateException("No factory registered for " + clazz.getName());
        }
        return (F) factory;
    }

    public Match create(VariorumMap map, File mapConfig, World world) throws Exception {
        // Create a secure document builder
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // Security features
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        DocumentBuilder builder = factory.newDocumentBuilder();

        // Parse the map configuration
        Document document = builder.parse(mapConfig);
        Element root = document.getDocumentElement();

        // Create a new match instance
        Match match = new Match(map, world);

        // Build and add modules
        for (ModuleFactory<?> moduleFactory : orderedFactories) {
            try {
                Optional<?> module = moduleFactory.build(match, root);
                if (module.isPresent()) {
                    match.addModule((Module) module.get());
                    Variorum.get().getLogger().info("Added module: " + module.get().getClass().getSimpleName());
                } else {
                    Variorum.get().getLogger().info("Module factory " + moduleFactory.getClass().getSimpleName() + " did not create a module");
                }
            } catch (Exception e) {
                Variorum.get().getLogger().log(Level.SEVERE, "Failed to build module using " + moduleFactory.getClass().getName(), e);
                e.printStackTrace();
            }
        }

        return match;
    }
}