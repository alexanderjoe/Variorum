package dev.alexanderdiaz.variorum.map.rotation;

import dev.alexanderdiaz.variorum.Variorum;
import dev.alexanderdiaz.variorum.map.MapManager;
import dev.alexanderdiaz.variorum.map.VariorumMap;
import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.match.MatchFactory;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import javax.annotation.Nullable;

public abstract class AbstractRotationProvider implements RotationProvider {
  protected final MatchFactory factory;
  protected final MapManager mapManager;

  protected AbstractRotationProvider(MapManager mm, MatchFactory factory) {
    this.mapManager = mm;
    this.factory = factory;
  }

  Rotation defineRotation(List<Match> maps) {
    if (maps.isEmpty()) {
      Variorum.get()
          .getLogger()
          .warning("No valid maps found in rotation file, using default rotation");
      throw new IllegalStateException("No valid maps found in rotation file");
    }

    Variorum.get().getLogger().info("Loaded " + maps.size() + " maps from rotation file");
    return new Rotation(maps);
  }

  @Nullable Match createMatch(VariorumMap map) {
    try {
      return this.factory.create(map);
    } catch (Exception e) {
      Variorum.get()
          .getLogger()
          .log(Level.WARNING, "Failed to create match for map " + map.getName(), e);
    }
    return null;
  }

  @Nullable Match createMatch(String name) {
    Optional<VariorumMap> map = this.mapManager.getMapByName(name);
    if (map.isEmpty()) {
      Variorum.get().getLogger().warning("Map " + name + " not found");
      return null;
    }
    return this.createMatch(map.get());
  }
}
