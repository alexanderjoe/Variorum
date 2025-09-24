package dev.alexanderdiaz.variorum.module.zones.checks;

import dev.alexanderdiaz.variorum.event.zone.ZoneEnterEvent;
import dev.alexanderdiaz.variorum.module.team.Team;
import dev.alexanderdiaz.variorum.module.team.TeamsModule;
import dev.alexanderdiaz.variorum.module.zones.AbstractZoneCheck;
import dev.alexanderdiaz.variorum.module.zones.CheckType;
import dev.alexanderdiaz.variorum.module.zones.Zone;
import dev.alexanderdiaz.variorum.util.Events;
import java.util.HashSet;
import java.util.Set;
import lombok.ToString;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

@ToString
public class EntryCheck extends AbstractZoneCheck implements Listener {
  private final Set<Team> allowedTeams;
  private final Set<Team> deniedTeams;
  private final Component message;
  private final boolean denySpectators;

  public EntryCheck(
      Zone zone,
      Set<Team> allowedTeams,
      Set<Team> deniedTeams,
      String message,
      boolean denySpectators) {
    super(zone);
    this.allowedTeams = new HashSet<>(allowedTeams);
    this.deniedTeams = new HashSet<>(deniedTeams);
    this.message = message != null
        ? Component.text(message, NamedTextColor.RED)
        : Component.text("You cannot enter this area!", NamedTextColor.RED);
    this.denySpectators = denySpectators;
  }

  @Override
  public CheckType getType() {
    return CheckType.ENTRY;
  }

  @Override
  public void enable() {
    Events.register(this);
  }

  @Override
  public void disable() {
    Events.unregister(this);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onZoneEnter(ZoneEnterEvent event) {
    if (!event.getZone().equals(zone)) {
      return;
    }

    var teamsModule = zone.getMatch().getModule(TeamsModule.class);
    if (teamsModule.isEmpty()) {
      return;
    }

    var playerTeam = teamsModule.get().getPlayerTeam(event.getPlayer());

    if (playerTeam.isEmpty()) {
      if (denySpectators) {
        event.setCancelled(true);
        event.getPlayer().sendMessage(message);
      }
      return;
    }

    if (deniedTeams.contains(playerTeam.get())) {
      event.setCancelled(true);
      event.getPlayer().sendMessage(message);
      return;
    }

    if (!allowedTeams.isEmpty() && !allowedTeams.contains(playerTeam.get())) {
      event.setCancelled(true);
      event.getPlayer().sendMessage(message);
    }
  }
}
