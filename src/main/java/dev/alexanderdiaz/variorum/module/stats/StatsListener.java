package dev.alexanderdiaz.variorum.module.stats;

import dev.alexanderdiaz.variorum.event.match.MatchCompleteEvent;
import dev.alexanderdiaz.variorum.module.objectives.monument.MonumentDestroyedEvent;
import dev.alexanderdiaz.variorum.module.objectives.wool.WoolPlaceEvent;
import dev.alexanderdiaz.variorum.module.team.Team;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class StatsListener implements Listener {
  private final StatsModule module;

  public StatsListener(StatsModule module) {
    this.module = module;
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    module.getPlayerStats(event.getPlayer());
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onEntityDamage(EntityDamageByEntityEvent event) {
    if (!(event.getEntity() instanceof Player victim)) return;

    PlayerStats victimStats = module.getPlayerStats(victim);
    victimStats.addDamageTaken(event.getFinalDamage());

    if (event.getDamager() instanceof Player attacker) {
      PlayerStats attackerStats = module.getPlayerStats(attacker);
      attackerStats.addDamageDealt(event.getFinalDamage());
    } else if (event.getDamager() instanceof Arrow arrow
        && arrow.getShooter() instanceof Player shooter) {
      PlayerStats shooterStats = module.getPlayerStats(shooter);
      shooterStats.addDamageDealt(event.getFinalDamage());

      double distance = arrow.getLocation().distance(shooter.getLocation());
      shooterStats.updateLongestShot(distance);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerDeath(PlayerDeathEvent event) {
    Player victim = event.getPlayer();
    Player killer = victim.getKiller();
    EntityDamageEvent.DamageCause cause = victim.getLastDamageCause() != null
        ? victim.getLastDamageCause().getCause()
        : EntityDamageEvent.DamageCause.CUSTOM;

    PlayerStats victimStats = module.getPlayerStats(victim);
    victimStats.incrementDeaths();

    if (killer != null) {
      PlayerStats killerStats = module.getPlayerStats(killer);
      killerStats.incrementKills();
    }

    Component deathMessage = generateDeathMessage(victim, killer, cause);
    event.deathMessage(deathMessage);
  }

  @EventHandler
  public void onMonumentDestroyed(MonumentDestroyedEvent event) {
    event.getMatch().getWorld().getPlayers().stream()
        .filter(player -> module.getPlayerTeam(player) != null)
        .filter(
            player -> module.getPlayerTeam(player).equals(event.getMonument().getOwner()))
        .forEach(player -> module.getPlayerStats(player).incrementObjectives());
  }

  @EventHandler
  public void onWoolPlace(WoolPlaceEvent event) {
    module.getPlayerStats(event.getPlayer()).incrementObjectives();
  }

  private Component generateDeathMessage(
      Player victim, Player killer, EntityDamageEvent.DamageCause cause) {
    Team victimTeam = module.getPlayerTeam(victim);
    Component victimName = victim
        .displayName()
        .color(victimTeam != null ? victimTeam.textColor() : NamedTextColor.WHITE);

    if (killer != null) {
      Team killerTeam = module.getPlayerTeam(killer);
      Component killerName = killer
          .displayName()
          .color(killerTeam != null ? killerTeam.textColor() : NamedTextColor.WHITE);

      if (cause == EntityDamageEvent.DamageCause.PROJECTILE) {
        Location victimLoc = victim.getLocation();
        Location killerLoc = killer.getLocation();
        double distance = victimLoc.distance(killerLoc);

        return Component.text()
            .append(victimName)
            .append(Component.text(" was shot by ", NamedTextColor.GRAY))
            .append(killerName)
            .append(Component.text(String.format(" (%.2f blocks)", distance), NamedTextColor.GRAY))
            .build();
      }

      return Component.text()
          .append(victimName)
          .append(Component.text(" was killed by ", NamedTextColor.GRAY))
          .append(killerName)
          .build();
    }

    // environmental deaths
    return switch (cause) {
      case FALL ->
        Component.text()
            .append(victimName)
            .append(Component.text(" fell to their death", NamedTextColor.GRAY))
            .build();
      case VOID ->
        Component.text()
            .append(victimName)
            .append(Component.text(" fell into the void", NamedTextColor.GRAY))
            .build();
      case LAVA ->
        Component.text()
            .append(victimName)
            .append(Component.text(" tried to swim in lava", NamedTextColor.GRAY))
            .build();
      case DROWNING ->
        Component.text()
            .append(victimName)
            .append(Component.text(" drowned", NamedTextColor.GRAY))
            .build();
      case FIRE, FIRE_TICK ->
        Component.text()
            .append(victimName)
            .append(Component.text(" burned to death", NamedTextColor.GRAY))
            .build();
      default ->
        Component.text()
            .append(victimName)
            .append(Component.text(" died", NamedTextColor.GRAY))
            .build();
    };
  }

  @EventHandler
  public void onMatchComplete(MatchCompleteEvent event) {
    module.getPlayerStats().forEach((uuid, stats) -> {
      Player player = (Player) event.getMatch().getWorld().getEntity(uuid);

      if (player != null) {
        player.sendMessage(stats.generateSummary());
      }
    });
  }
}
