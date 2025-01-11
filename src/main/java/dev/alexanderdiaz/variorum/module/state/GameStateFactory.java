package dev.alexanderdiaz.variorum.module.state;

import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.ModuleFactory;
import java.util.Optional;
import org.w3c.dom.Element;

public class GameStateFactory implements ModuleFactory<GameStateModule> {
    @Override
    public Optional<GameStateModule> build(Match match, Element root) {
        // Game state module is always created as it's a core feature
        return Optional.of(new GameStateModule(match));
    }
}
