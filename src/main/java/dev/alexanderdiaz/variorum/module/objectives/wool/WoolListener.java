package dev.alexanderdiaz.variorum.module.objectives.wool;

import dev.alexanderdiaz.variorum.module.objectives.Objective;
import dev.alexanderdiaz.variorum.module.objectives.ObjectivesModule;
import dev.alexanderdiaz.variorum.module.team.Team;
import dev.alexanderdiaz.variorum.module.team.TeamsModule;
import dev.alexanderdiaz.variorum.util.Events;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/** Handles listening for events related to the wool objective. */
@RequiredArgsConstructor
public class WoolListener implements Listener {
  private final ObjectivesModule module;

  /**
   * This should handle checking if a player has touched a wool that is part of their wools
   * objective by means of checking if it enters their inventory.
   *
   * @param event The event we are listening for.
   */
  @EventHandler
  public void onPickUp(EntityPickupItemEvent event) {
    if (!(event.getEntity() instanceof Player player)) return;
    ItemStack item = event.getItem().getItemStack();

    module.getObjectives().stream()
        .filter(obj -> obj instanceof WoolObjective)
        .map(obj -> (WoolObjective) obj)
        .filter(wool -> wool.isValidBlock(item.getType()))
        .filter(wool -> wool.getTeam().isPresent())
        .forEach(wool -> {
          boolean isOnTeam = module
              .getMatch()
              .getRequiredModule(TeamsModule.class)
              .getPlayerTeam(player)
              .map(team -> team.equals(wool.getTeam().get()))
              .orElse(false);

          if (isOnTeam
              && (!wool.getSource().isPresent()
                  || wool.getSource().get().contains(event.getItem().getLocation()))) {
            Events.call(new WoolPickupEvent(module.getMatch(), wool, player));
          }
        });
  }

  /**
   * This should handle checking if a player has placed a wool at a wool destination and completed
   * the objective.
   *
   * @param event The event we are listening for.
   */
  @EventHandler
  public void onPlace(BlockPlaceEvent event) {
    Block block = event.getBlock();
    // Check all wool objectives
    for (Objective obj : module.getObjectives()) {
      if (!(obj instanceof WoolObjective wool)) continue;

      if (!wool.isValidDestination(block)) continue;

      if (wool.isCompleted()) {
        event.setCancelled(true);
        break;
      } else {
        event.setCancelled(true);

        Player player = event.getPlayer();
        Team team = this.module
            .getMatch()
            .getRequiredModule(TeamsModule.class)
            .getPlayerTeam(player)
            .orElse(null);

        if (team == null) return;

        if (!wool.canComplete(team)) {
          player.sendMessage(Component.text(
              "You are not allowed to complete this objective!", NamedTextColor.RED));
          break;
        }

        if (!wool.isValidBlock(block.getType())) {
          player.sendMessage(Component.text(
              "This is not the correct wool for this location!", NamedTextColor.RED));
          break;
        }

        wool.place(player);
        Events.call(new WoolPlaceEvent(module.getMatch(), wool, player));

        event.setCancelled(false);
      }
    }
  }

  /**
   * This should handle checking for when a player closes an inventory, checking if the inventory is
   * at a source location and refill is turned on.
   *
   * @param event The event we are listening for.
   */
  @EventHandler
  public void onInventoryInteract(InventoryCloseEvent event) {
    if (!(event.getInventory().getHolder() instanceof BlockState)) return;
    Player player = (Player) event.getPlayer();

    if (this.module.getMatch().getRequiredModule(TeamsModule.class).isSpectator(player)) return;

    Block block = ((BlockState) event.getInventory().getHolder()).getBlock();
    Inventory inventory = event.getInventory();

    if (inventory.getViewers().size() > 1) {
      return;
    }

    for (Objective obj : module.getObjectives()) {
      if (!(obj instanceof WoolObjective wool)) continue;

      if (!wool.isRefillEnabled()) {
        continue;
      }

      if (wool.getSource().isEmpty()) {
        continue;
      }

      if (!wool.isValidSource(block)) {
        continue;
      }

      ItemStack woolStack = new ItemStack(Material.valueOf(wool.getColor().toString() + "_WOOL"));

      while (inventory.firstEmpty() != -1) {
        inventory.addItem(woolStack);
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onInventoryClick(InventoryClickEvent event) {
    ItemStack item = event.getCurrentItem();
    if (item == null) return;

    var holder = event.getInventory().getHolder();
    Block block;

    if (holder instanceof Chest) {
      block = ((Chest) holder).getBlock();
    } else if (holder instanceof DoubleChest) {
      block = ((DoubleChest) holder).getLocation().getBlock();
    } else {
      // not an inventory to be concerned with
      return;
    }

    Player player = (Player) event.getWhoClicked();
    Team team = module
        .getMatch()
        .getRequiredModule(TeamsModule.class)
        .getPlayerTeam(player)
        .orElse(null);

    if (team == null) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onInventoryOpen(InventoryOpenEvent event) {
    if (!(event.getInventory().getHolder() instanceof BlockState)) return;
    Player player = (Player) event.getPlayer();
    Team team = module
        .getMatch()
        .getRequiredModule(TeamsModule.class)
        .getPlayerTeam(player)
        .orElse(null);

    if (team == null) return;

    Block block = ((BlockState) event.getInventory().getHolder()).getBlock();

    for (Objective obj : module.getObjectives()) {
      if (!(obj instanceof WoolObjective wool)) continue;

      if (wool.getSource().isEmpty()) {
        continue;
      }

      if (!wool.isValidSource(block)) {
        continue;
      }

      if (!wool.canComplete(team)) {
        player.sendMessage(
            Component.text("You are not allowed to interact with this chest!", NamedTextColor.RED));
        event.setCancelled(true);
        break;
      }
    }
  }

  @EventHandler
  public void onBlockBreak(BlockBreakEvent event) {
    Block block = event.getBlock();
    Player player = event.getPlayer();

    for (Objective obj : module.getObjectives()) {
      if (!(obj instanceof WoolObjective wool)) continue;

      if (wool.getSource().isEmpty()) {
        continue;
      }

      if (!wool.isValidSource(block)) {
        continue;
      }

      player.sendMessage(
          Component.text("You are not allowed to break this block!", NamedTextColor.RED));
      event.setCancelled(true);
      break;
    }
  }
}
