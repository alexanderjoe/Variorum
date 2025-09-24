package dev.alexanderdiaz.variorum.event.zone;

import dev.alexanderdiaz.variorum.module.zones.Zone;
import org.bukkit.entity.Player;

public class ZoneEnterEvent extends ZoneEvent {
  public ZoneEnterEvent(Player player, Zone zone) {
    super(player, zone);
  }
}
