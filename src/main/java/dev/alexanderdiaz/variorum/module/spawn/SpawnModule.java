package dev.alexanderdiaz.variorum.module.spawn;

import dev.alexanderdiaz.variorum.Variorum;
import dev.alexanderdiaz.variorum.map.VariorumMap;
import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.Module;
import dev.alexanderdiaz.variorum.module.team.Team;
import dev.alexanderdiaz.variorum.module.team.TeamsModule;
import dev.alexanderdiaz.variorum.util.Events;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Optional;

@RequiredArgsConstructor
public class SpawnModule implements Module {
    @Getter
    private final Match match;
    private SpawnListener listener;

    @Override
    public void enable() {
        this.listener = new SpawnListener(this);
        Events.register(listener);
    }

    @Override
    public void disable() {
        if (listener != null) {
            Events.unregister(listener);
        }
    }

    public void handleMatchStart() {
        World matchWorld = match.getWorld();
        // Get teams module to check player participation
        TeamsModule teamsModule = match.getRequiredModule(TeamsModule.class);

        matchWorld.getPlayers().forEach(player -> {
            // Only teleport players who are on a team
            if (teamsModule.getPlayerTeam(player).isPresent()) {
                teleportToSpawn(player);
            }
        });
    }

    public Location getSpawnLocation(Player player) {
        World matchWorld = match.getWorld();
        VariorumMap map = match.getMap();

        // Try to find team-specific spawn
        TeamsModule teamsModule = match.getRequiredModule(TeamsModule.class);
        Optional<Team> playerTeam = teamsModule.getPlayerTeam(player);

        if (playerTeam.isPresent()) {
            // Find team spawn
            Optional<VariorumMap.Spawns.TeamSpawn> teamSpawn = map.getSpawns().getTeamSpawns()
                    .stream()
                    .filter(spawn -> spawn.getTeam().equals(playerTeam.get().id()))
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

        return getDefaultSpawn();
    }

    public Location getDefaultSpawn() {
        World matchWorld = match.getWorld();
        VariorumMap map = match.getMap();

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

        TeamsModule teamsModule = match.getRequiredModule(TeamsModule.class);
        Team playerTeam = teamsModule.getPlayerTeam(player).orElse(null);

        Variorum.get().getLogger().info("Player is on team: " + playerTeam);
        Location spawn = getSpawnLocation(player);
        player.teleport(spawn);
        Variorum.get().getLogger().info("Teleported " + player.getName() + " to spawn at " +
                String.format("%.1f, %.1f, %.1f (yaw: %.1f) in %s",
                        spawn.getX(), spawn.getY(), spawn.getZ(),
                        spawn.getYaw(), matchWorld.getName()));
    }
}
