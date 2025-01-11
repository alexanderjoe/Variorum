package dev.alexanderdiaz.variorum.module.results;

import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.ModuleFactory;
import java.util.Optional;
import org.w3c.dom.Element;

public class ResultsFactory implements ModuleFactory<ResultsModule> {
    @Override
    public Optional<ResultsModule> build(Match match, Element root) {
        if (root.getElementsByTagName("teams").getLength() > 0
                && root.getElementsByTagName("objectives").getLength() > 0) {
            return Optional.of(new ResultsModule(match));
        }
        return Optional.empty();
    }
}
