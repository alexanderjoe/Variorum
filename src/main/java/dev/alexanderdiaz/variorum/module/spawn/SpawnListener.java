package dev.alexanderdiaz.variorum.module.spawn;

import dev.alexanderdiaz.variorum.event.team.PlayerChangeTeamEvent;
import dev.alexanderdiaz.variorum.module.state.GameState;
import dev.alexanderdiaz.variorum.module.state.GameStateChangeEvent;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
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
        if (!module.getMatch().isActive()) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, Component.text("The match is still loading!"));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.joinMessage(null);
        // Delay the teleport by 1 tick to ensure everything is loaded
        module.teleportToSpawn(event.getPlayer());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        event.setRespawnLocation(module.getSpawnLocation(event.getPlayer()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChangeTeam(PlayerChangeTeamEvent event) {
        // Teleport player to their new team spawn
        module.teleportToSpawn(event.getPlayer());
    }

    @EventHandler
    public void onGameStateChange(GameStateChangeEvent event) {
        if (event.getNewState() == GameState.PLAYING) {
            module.handleMatchStart();
        }
    }
}
