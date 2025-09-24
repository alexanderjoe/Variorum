package dev.alexanderdiaz.variorum.module.zones.checks;

import dev.alexanderdiaz.variorum.module.team.Team;
import dev.alexanderdiaz.variorum.module.team.TeamsModule;
import dev.alexanderdiaz.variorum.module.zones.AbstractZoneCheck;
import dev.alexanderdiaz.variorum.module.zones.CheckType;
import dev.alexanderdiaz.variorum.module.zones.Zone;
import dev.alexanderdiaz.variorum.util.Events;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import lombok.ToString;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

@ToString
public class BuildCheck extends AbstractZoneCheck implements Listener {
  private final Set<Team> allowedTeams;
  private final Set<Team> deniedTeams;
  private final Component breakMessage;
  private final Component placeMessage;
  private final boolean allowBreak;
  private final boolean allowPlace;
  private final Set<Material> whitelist;
  private final Set<Material> blacklist;

  public BuildCheck(
      Zone zone,
      Set<Team> allowedTeams,
      Set<Team> deniedTeams,
      String breakMessage,
      String placeMessage,
      boolean allowBreak,
      boolean allowPlace,
      Set<Material> whitelist,
      Set<Material> blacklist) {
    super(zone);
    this.allowedTeams = new HashSet<>(allowedTeams);
    this.deniedTeams = new HashSet<>(deniedTeams);
    this.breakMessage = Component.text(
        breakMessage != null ? breakMessage : "You cannot break blocks here!", NamedTextColor.RED);
    this.placeMessage = Component.text(
        placeMessage != null ? placeMessage : "You cannot place blocks here!", NamedTextColor.RED);
    this.allowBreak = allowBreak;
    this.allowPlace = allowPlace;
    this.whitelist = new HashSet<>(whitelist);
    this.blacklist = new HashSet<>(blacklist);
  }

  @Override
  public CheckType getType() {
    return CheckType.BUILD;
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
  public void onBlockBreak(BlockBreakEvent event) {
    Block block = event.getBlock();
    if (!zone.getRegion().contains(block.getLocation())) {
      return;
    }

    if (!canModifyBlocks(event.getPlayer(), block.getType(), true)) {
      event.setCancelled(true);
      event.getPlayer().sendMessage(breakMessage);
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onBlockPlace(BlockPlaceEvent event) {
    Block block = event.getBlock();
    if (!zone.getRegion().contains(block.getLocation())) {
      return;
    }

    if (!canModifyBlocks(event.getPlayer(), block.getType(), false)) {
      event.setCancelled(true);
      event.getPlayer().sendMessage(placeMessage);
    }
  }

  private boolean canModifyBlocks(Player player, Material material, boolean isBreaking) {
    if (isBreaking && !allowBreak) return false;
    if (!isBreaking && !allowPlace) return false;

    if (!whitelist.isEmpty() && !whitelist.contains(material)) return false;
    if (!blacklist.isEmpty() && blacklist.contains(material)) return false;

    Optional<Team> playerTeam = zone.getMatch()
        .getModule(TeamsModule.class)
        .flatMap(module -> module.getPlayerTeam(player));

    if (playerTeam.isEmpty()) return false;
    if (deniedTeams.contains(playerTeam.get())) return false;
    if (!allowedTeams.isEmpty() && !allowedTeams.contains(playerTeam.get())) return false;

    return true;
  }
}
