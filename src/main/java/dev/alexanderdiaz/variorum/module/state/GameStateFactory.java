package dev.alexanderdiaz.variorum.module.state;

import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.ModuleFactory;
import dev.alexanderdiaz.variorum.util.xml.XmlElement;
import java.util.Optional;

public class GameStateFactory implements ModuleFactory<GameStateModule> {
  @Override
  public Optional<GameStateModule> build(Match match, XmlElement root) {
    return Optional.of(new GameStateModule(match));
  }
}
