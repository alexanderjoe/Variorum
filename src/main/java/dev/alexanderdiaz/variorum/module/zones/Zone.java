package dev.alexanderdiaz.variorum.module.zones;

import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.region.Region;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import org.bukkit.entity.Player;

public class Zone {
  @Getter
  private final Match match;

  @Getter
  private final String id;

  @Getter
  private final Region region;

  private final Set<ZoneCheck> checks;

  public Zone(Match match, String id, Region region) {
    this.match = match;
    this.id = id;
    this.region = region;
    this.checks = new HashSet<>();
  }

  public void addCheck(ZoneCheck check) {
    checks.add(check);
  }

  public Collection<ZoneCheck> getChecks() {
    return new HashSet<>(checks);
  }

  public boolean contains(Player player) {
    return region.contains(player.getLocation());
  }
}
