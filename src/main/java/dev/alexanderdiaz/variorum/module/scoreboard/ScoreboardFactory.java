package dev.alexanderdiaz.variorum.module.scoreboard;

import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.ModuleFactory;
import org.w3c.dom.Element;

import java.util.Optional;

public class ScoreboardFactory implements ModuleFactory<ScoreboardModule> {
    @Override
    public Optional<ScoreboardModule> build(Match match, Element root) {
        // Scoreboard module should be created when there are teams and objectives
        if (root.getElementsByTagName("teams").getLength() > 0
                && root.getElementsByTagName("objectives").getLength() > 0) {
            return Optional.of(new ScoreboardModule(match));
        }
        return Optional.empty();
    }
}