package dev.alexanderdiaz.variorum.module.objectives.monument;

import dev.alexanderdiaz.variorum.Variorum;
import dev.alexanderdiaz.variorum.module.team.Team;
import dev.alexanderdiaz.variorum.module.team.TeamsModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.Optional;

public class MonumentListener implements Listener {
    private final MonumentObjective monument;

    public MonumentListener(MonumentObjective monument) {
        this.monument = monument;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (!monument.isMonumentBlock(block)) {
            return;
        }

        // Check if block is valid monument material
        if (!monument.isValidMaterial(block.getType())) {
            return;
        }

        // Get the player's team
        TeamsModule teamsModule = Variorum.getMatch().getRequiredModule(TeamsModule.class);
        Optional<Team> playerTeam = teamsModule.getPlayerTeam(player);

        // Players can't break their own monuments
        if (playerTeam.isPresent() && playerTeam.get().equals(monument.getOwner())) {
            event.setCancelled(true);
            player.sendMessage(Component.text("You cannot break your own monuments!", NamedTextColor.RED));
            return;
        }

        // Mark monument as broken and broadcast
        monument.markBroken();

        Component message = Component.text()
                .append(Component.text(monument.getName(), monument.getOwner().textColor()))
                .append(Component.text(" monument was destroyed by ", NamedTextColor.GRAY))
                .append(player.displayName())
                .append(Component.text("!", NamedTextColor.GRAY))
                .build();

        Variorum.getMatch().getWorld().getPlayers()
                .forEach(p -> p.sendMessage(message));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();

        // Prevent placing blocks at monument locations
        if (monument.isMonumentBlock(block)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Component.text("You cannot place blocks here!", NamedTextColor.RED));
        }
    }
}