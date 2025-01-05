package dev.alexanderdiaz.variorum.module.spawn;

import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.ModuleFactory;
import org.w3c.dom.Element;

import java.util.Optional;

public class SpawnModuleFactory implements ModuleFactory<SpawnModule> {
    @Override
    public Optional<SpawnModule> build(Match match, Element root) {
        if (root.getElementsByTagName("spawns").getLength() > 0) {
            return Optional.of(new SpawnModule(match));
        }
        return Optional.empty();
    }
}