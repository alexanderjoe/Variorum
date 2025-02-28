package dev.alexanderdiaz.variorum.module.state;

import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.team.TeamsModule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class GameListener implements Listener {
    private final Match match;
    private final GameStateModule gameStateModule;

    public GameListener(Match match, GameStateModule gameStateModule) {
        this.match = match;
        this.gameStateModule = gameStateModule;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {}

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (gameStateModule.getCurrentState() == GameState.PLAYING) {
            if (gameStateModule.getPlayingHandler().shouldEndDueToPlayerCount()) {
                gameStateModule.setState(GameState.ENDED);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (gameStateModule.getCurrentState() != GameState.PLAYING) {
            event.setCancelled(true);
            return;
        }

        TeamsModule teamsModule = match.getRequiredModule(TeamsModule.class);
        if (teamsModule.getPlayerTeam(event.getPlayer()).isEmpty()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (gameStateModule.getCurrentState() != GameState.PLAYING) {
            event.setCancelled(true);
            return;
        }

        TeamsModule teamsModule = match.getRequiredModule(TeamsModule.class);
        if (teamsModule.getPlayerTeam(event.getPlayer()).isEmpty()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (gameStateModule.getCurrentState() != GameState.PLAYING && event.getEntity() instanceof Player) {
            event.setCancelled(true);
        }
    }
}
