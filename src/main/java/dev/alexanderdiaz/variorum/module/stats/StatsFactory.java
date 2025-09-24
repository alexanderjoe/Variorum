package dev.alexanderdiaz.variorum.module.stats;

import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.ModuleFactory;
import dev.alexanderdiaz.variorum.util.xml.XmlElement;
import java.util.Optional;

public class StatsFactory implements ModuleFactory<StatsModule> {
  @Override
  public Optional<StatsModule> build(Match match, XmlElement root) {
    return Optional.of(new StatsModule(match));
  }
}
