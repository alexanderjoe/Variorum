package dev.alexanderdiaz.variorum.module.stats;

import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.ModuleFactory;
import java.util.Optional;
import org.w3c.dom.Element;

public class StatsFactory implements ModuleFactory<StatsModule> {
    @Override
    public Optional<StatsModule> build(Match match, Element root) {
        return Optional.of(new StatsModule(match));
    }
}
