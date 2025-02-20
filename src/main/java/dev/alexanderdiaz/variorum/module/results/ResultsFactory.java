package dev.alexanderdiaz.variorum.module.results;

import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.ModuleFactory;
import dev.alexanderdiaz.variorum.util.xml.XmlElement;
import java.util.Optional;

public class ResultsFactory implements ModuleFactory<ResultsModule> {
    @Override
    public Optional<ResultsModule> build(Match match, XmlElement root) {
        if (!root.getChildren("teams").isEmpty()
                && !root.getChildren("objectives").isEmpty()) {
            return Optional.of(new ResultsModule(match));
        }

        return Optional.empty();
    }
}
