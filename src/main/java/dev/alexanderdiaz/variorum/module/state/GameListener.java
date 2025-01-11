package dev.alexanderdiaz.variorum.module.state;

import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.team.TeamsModule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class GameListener implements Listener {
    private final Match match;
    private final GameStateModule gameState;

    public GameListener(Match match, GameStateModule gameState) {
        this.match = match;
        this.gameState = gameState;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (GameState.WAITING.equals(gameState.getCurrentState())) {
            if (match.getWorld().getPlayers().size() >= GameStateModule.MIN_PLAYERS) {
                gameState.setState(GameState.COUNTDOWN);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!GameState.PLAYING.equals(gameState.getCurrentState())) {
            event.setCancelled(true);
            return;
        }

        TeamsModule teamsModule = match.getRequiredModule(TeamsModule.class);
        if (teamsModule.getPlayerTeam(event.getPlayer()).isEmpty()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!GameState.PLAYING.equals(gameState.getCurrentState())) {
            event.setCancelled(true);
            return;
        }

        TeamsModule teamsModule = match.getRequiredModule(TeamsModule.class);
        if (teamsModule.getPlayerTeam(event.getPlayer()).isEmpty()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!GameState.PLAYING.equals(gameState.getCurrentState()) && event.getEntity() instanceof Player) {
            event.setCancelled(true);
        }
    }
}
