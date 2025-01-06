package dev.alexanderdiaz.variorum.module.results;

import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.ModuleFactory;
import org.w3c.dom.Element;

import java.util.Optional;

public class ResultsFactory implements ModuleFactory<ResultsModule> {
    @Override
    public Optional<ResultsModule> build(Match match, Element root) {
        // Result module should be created when there are teams and objectives
        if (root.getElementsByTagName("teams").getLength() > 0
                && root.getElementsByTagName("objectives").getLength() > 0) {
            return Optional.of(new ResultsModule(match));
        }
        return Optional.empty();
    }
}