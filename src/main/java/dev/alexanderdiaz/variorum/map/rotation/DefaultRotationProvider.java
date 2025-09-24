package dev.alexanderdiaz.variorum.map.rotation;

import dev.alexanderdiaz.variorum.Variorum;
import dev.alexanderdiaz.variorum.map.MapManager;
import dev.alexanderdiaz.variorum.map.VariorumMap;
import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.match.MatchFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class DefaultRotationProvider extends AbstractRotationProvider {
  public DefaultRotationProvider(MapManager mm, MatchFactory factory) {
    super(mm, factory);
  }

  @Override
  public Rotation provideRotation() {
    List<Match> maps = new ArrayList<>();

    for (VariorumMap map : mapManager.getAllMaps().values()) {
      try {
        Match match = this.createMatch(map);
        maps.add(match);
      } catch (Exception e) {
        Variorum.get().getLogger().log(Level.WARNING, "Failed to load map: " + map.getName(), e);
      }
    }

    if (maps.isEmpty()) {
      Variorum.get().getLogger().log(Level.WARNING, "No valid maps found in maps directory");
    } else {
      Variorum.get().getLogger().log(Level.INFO, "Loaded " + maps.size() + " maps into rotation");
    }

    return this.defineRotation(maps);
  }
}
