package dev.alexanderdiaz.variorum.module.spawn;

import dev.alexanderdiaz.variorum.Variorum;
import dev.alexanderdiaz.variorum.map.VariorumMap;
import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.Module;
import dev.alexanderdiaz.variorum.module.team.TeamsModule;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.Optional;

@RequiredArgsConstructor
public class SpawnModule implements Module {
    private final Match match;
    private SpawnListener listener;

    @Override
    public void enable() {
        this.listener = new SpawnListener();
        Variorum.get().getServer().getPluginManager().registerEvents(listener, Variorum.get());
    }

    @Override
    public void disable() {
        if (listener != null) {
            PlayerJoinEvent.getHandlerList().unregister(listener);
            PlayerLoginEvent.getHandlerList().unregister(listener);
            PlayerRespawnEvent.getHandlerList().unregister(listener);
        }
    }

    private Location getSpawnLocation(Player player) {
        World matchWorld = match.getWorld();
        VariorumMap map = match.getMap();

        // Try to find team-specific spawn
        Optional<TeamsModule> teamsModule = match.getModule(TeamsModule.class);
        if (teamsModule.isPresent()) {
            Optional<TeamsModule.Team> playerTeam = teamsModule.get().getPlayerTeam(player);
            if (playerTeam.isPresent()) {
                // Find team spawn
                Optional<VariorumMap.Spawns.TeamSpawn> teamSpawn = map.getSpawns().getTeamSpawns()
                        .stream()
                        .filter(spawn -> spawn.getTeam().equals(playerTeam.get().getId()))
                        .findFirst();

                if (teamSpawn.isPresent()) {
                    VariorumMap.Point spawnPoint = teamSpawn.get().getRegion().getPoint();
                    double yaw = teamSpawn.get().getRegion().getYaw();
                    return new Location(matchWorld,
                            spawnPoint.getX(),
                            spawnPoint.getY(),
                            spawnPoint.getZ(),
                            (float) yaw,
                            0.0f);
                }
            }
        }

        // Fall back to default spawn if no team spawn is available
        VariorumMap.Point defaultPoint = map.getSpawns().getDefaultSpawn().getPoint();
        double defaultYaw = map.getSpawns().getDefaultSpawn().getYaw();
        return new Location(matchWorld,
                defaultPoint.getX(),
                defaultPoint.getY(),
                defaultPoint.getZ(),
                (float) defaultYaw,
                0.0f);
    }

    public void teleportToSpawn(Player player) {
        World matchWorld = match.getWorld();
        if (matchWorld == null) {
            Variorum.get().getLogger().warning("Match world is null when trying to teleport " + player.getName());
            return;
        }

        Location spawn = getSpawnLocation(player);
        player.teleport(spawn);
        Variorum.get().getLogger().info("Teleported " + player.getName() + " to spawn at " +
                String.format("%.1f, %.1f, %.1f (yaw: %.1f) in %s",
                        spawn.getX(), spawn.getY(), spawn.getZ(),
                        spawn.getYaw(), matchWorld.getName()));
    }

    private class SpawnListener implements Listener {
        @EventHandler(priority = EventPriority.HIGHEST)
        public void onPlayerLogin(PlayerLoginEvent event) {
            if (!match.isActive()) {
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, Component.text("The match is still loading!"));
            }
        }

        @EventHandler(ignoreCancelled = true)
        public void onPlayerJoin(PlayerJoinEvent event) {
            event.joinMessage(null);

            // Delay the teleport by 1 tick to ensure everything is loaded
            teleportToSpawn(event.getPlayer());
        }

        @EventHandler
        public void onPlayerRespawn(PlayerRespawnEvent event) {
            event.setRespawnLocation(getSpawnLocation(event.getPlayer()));
        }
    }
}