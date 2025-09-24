package dev.alexanderdiaz.variorum.module.objectives.monument;

import dev.alexanderdiaz.variorum.module.objectives.ObjectivesModule;
import dev.alexanderdiaz.variorum.module.team.Team;
import dev.alexanderdiaz.variorum.module.team.TeamsModule;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class MonumentListener implements Listener {
  private final ObjectivesModule module;

  public MonumentListener(ObjectivesModule module) {
    this.module = module;
  }

  @EventHandler(ignoreCancelled = true)
  public void onBlockBreak(BlockBreakEvent event) {
    Block block = event.getBlock();

    Optional<MonumentObjective> monument = module.getObjectives().stream()
        .filter(obj -> obj instanceof MonumentObjective)
        .map(obj -> (MonumentObjective) obj)
        .filter(mon -> mon.isMonumentBlock(block))
        .findFirst();

    if (monument.isEmpty()) {
      return;
    }

    MonumentObjective monumentObj = monument.get();

    if (!monumentObj.isValidMaterial(block.getType())) {
      return;
    }

    handleBlockBreak(monumentObj, event);
  }

  private void handleBlockBreak(MonumentObjective objective, BlockBreakEvent event) {
    event.setCancelled(true);

    Player player = event.getPlayer();
    Block block = event.getBlock();

    Team team = this.module
        .getMatch()
        .getRequiredModule(TeamsModule.class)
        .getPlayerTeam(player)
        .orElse(null);

    if (team == null) {
      return;
    }

    if (!objective.canComplete(team)) {
      player.sendMessage(
          Component.text("You cannot break your own monuments!", NamedTextColor.RED));
      return;
    }

    block.setType(Material.AIR);

    objective.markBroken();

    Component message = Component.text()
        .append(Component.text(objective.getName(), objective.getOwner().textColor()))
        .append(Component.text(" monument was destroyed by ", NamedTextColor.GRAY))
        .append(player.displayName())
        .append(Component.text("!", NamedTextColor.GRAY))
        .build();

    this.module.getMatch().broadcast(message);
  }

  @EventHandler(ignoreCancelled = true)
  public void onBlockPlace(BlockPlaceEvent event) {
    Block block = event.getBlock();

    boolean isMonumentBlock = module.getObjectives().stream()
        .filter(obj -> obj instanceof MonumentObjective)
        .map(obj -> (MonumentObjective) obj)
        .anyMatch(monument -> monument.isMonumentBlock(block));

    if (isMonumentBlock) {
      event.setCancelled(true);
      event
          .getPlayer()
          .sendMessage(Component.text("You cannot place blocks here!", NamedTextColor.RED));
    }
  }
}
