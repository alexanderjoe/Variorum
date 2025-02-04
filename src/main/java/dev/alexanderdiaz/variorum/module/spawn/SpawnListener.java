package dev.alexanderdiaz.variorum.module.spawn;

import dev.alexanderdiaz.variorum.event.match.MatchOpenEvent;
import dev.alexanderdiaz.variorum.event.team.PlayerChangeTeamEvent;
import dev.alexanderdiaz.variorum.module.state.GameState;
import dev.alexanderdiaz.variorum.module.state.GameStateChangeEvent;
import dev.alexanderdiaz.variorum.module.state.GameStateModule;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

@RequiredArgsConstructor
public class SpawnListener implements Listener {
    private final SpawnModule module;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (!module.getMatch().isLoaded()) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, Component.text("The match is still loading!"));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.joinMessage(null);
        module.spawnPlayer(event.getPlayer(), false, true);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        event.setRespawnLocation(module.getSpawnLocation(event.getPlayer()));
        module.spawnPlayer(event.getPlayer(), true, true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChangeTeam(PlayerChangeTeamEvent event) {
        GameStateModule gameState = module.getMatch().getRequiredModule(GameStateModule.class);

        if (event.getToTeam() == null) {
            module.spawnPlayer(event.getPlayer(), false, false);
            return;
        }

        if (GameState.PLAYING.equals(gameState.getCurrentState())) {
            module.spawnPlayer(event.getPlayer(), true, true);
        }
    }

    @EventHandler
    public void onGameStateChange(GameStateChangeEvent event) {
        if (event.getNewState() == GameState.PLAYING) {
            module.handleMatchStart();
        } else if (event.getNewState() == GameState.ENDED) {
            module.handleMatchEnded();
        }
    }

    @EventHandler
    public void onMatchOpen(MatchOpenEvent event) {
        Bukkit.getOnlinePlayers().forEach(player -> module.spawnPlayer(player, false, true));
    }
}
