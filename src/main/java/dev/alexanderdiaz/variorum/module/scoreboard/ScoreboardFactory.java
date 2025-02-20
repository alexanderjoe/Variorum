package dev.alexanderdiaz.variorum.module.scoreboard;

import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.ModuleFactory;
import dev.alexanderdiaz.variorum.util.xml.XmlElement;
import java.util.Optional;

public class ScoreboardFactory implements ModuleFactory<ScoreboardModule> {
    @Override
    public Optional<ScoreboardModule> build(Match match, XmlElement root) {
        return Optional.of(new ScoreboardModule(match));
    }
}
