package dev.alexanderdiaz.variorum.module.scoreboard;

import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.ModuleFactory;
import java.util.Optional;
import org.w3c.dom.Element;

public class ScoreboardFactory implements ModuleFactory<ScoreboardModule> {
    @Override
    public Optional<ScoreboardModule> build(Match match, Element root) {
        return Optional.of(new ScoreboardModule(match));
    }
}
