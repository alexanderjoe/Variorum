package dev.alexanderdiaz.variorum.match;

import dev.alexanderdiaz.variorum.Variorum;
import dev.alexanderdiaz.variorum.map.VariorumMap;
import dev.alexanderdiaz.variorum.module.Module;
import dev.alexanderdiaz.variorum.module.ModuleBuildException;
import dev.alexanderdiaz.variorum.module.ModuleFactory;
import dev.alexanderdiaz.variorum.module.chat.ChatFactory;
import dev.alexanderdiaz.variorum.module.loadouts.LoadoutsFactory;
import dev.alexanderdiaz.variorum.module.objectives.ObjectivesFactory;
import dev.alexanderdiaz.variorum.module.regions.RegionFactory;
import dev.alexanderdiaz.variorum.module.results.ResultsFactory;
import dev.alexanderdiaz.variorum.module.scoreboard.ScoreboardFactory;
import dev.alexanderdiaz.variorum.module.spawn.SpawnFactory;
import dev.alexanderdiaz.variorum.module.state.GameStateFactory;
import dev.alexanderdiaz.variorum.module.stats.StatsFactory;
import dev.alexanderdiaz.variorum.module.team.TeamsFactory;
import dev.alexanderdiaz.variorum.module.zones.ZoneFactory;
import dev.alexanderdiaz.variorum.util.xml.XmlElement;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.Getter;
import org.w3c.dom.Document;

public class MatchFactory {
    private final Map<Class<? extends ModuleFactory<?>>, ModuleFactory<?>> factories;

    @Getter
    private final List<ModuleFactory<?>> orderedFactories;

    public MatchFactory() {
        this.factories = new HashMap<>();
        this.orderedFactories = new ArrayList<>();

        // higher priority
        register(RegionFactory.class);
        register(TeamsFactory.class);

        // normal priority
        register(SpawnFactory.class);
        register(ChatFactory.class);
        register(GameStateFactory.class);
        register(ObjectivesFactory.class);
        register(ResultsFactory.class);
        register(ScoreboardFactory.class);
        register(StatsFactory.class);
        register(LoadoutsFactory.class);
        register(ZoneFactory.class);
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

    public Match create(VariorumMap map) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // Security features
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document document = builder.parse(map.getSource().getXmlFile());
        XmlElement root = new XmlElement(document.getDocumentElement());

        Match match = new Match(map, this);

        for (ModuleFactory<?> moduleFactory : orderedFactories) {
            try {
                Optional<? extends Module> module = moduleFactory.build(match, root);
                module.ifPresent(match::addModule);
            } catch (Exception e) {
                throw new ModuleBuildException(moduleFactory, e);
            }
        }

        return match;
    }
}
