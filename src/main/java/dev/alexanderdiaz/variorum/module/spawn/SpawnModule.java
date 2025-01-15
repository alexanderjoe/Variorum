package dev.alexanderdiaz.variorum.module.spawn;

import dev.alexanderdiaz.variorum.Variorum;
import dev.alexanderdiaz.variorum.event.player.PlayerSpawnStartEvent;
import dev.alexanderdiaz.variorum.map.VariorumMap;
import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.Module;
import dev.alexanderdiaz.variorum.module.loadouts.LoadoutsModule;
import dev.alexanderdiaz.variorum.module.team.Team;
import dev.alexanderdiaz.variorum.module.team.TeamsModule;
import dev.alexanderdiaz.variorum.util.Events;
import dev.alexanderdiaz.variorum.util.Players;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

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
        TeamsModule teamsModule = match.getRequiredModule(TeamsModule.class);

        matchWorld.getPlayers().forEach(player -> {
            if (teamsModule.getPlayerTeam(player).isPresent()) {
                spawnPlayer(player, true);
            }
        });
    }

    public Location getSpawnLocation(Player player) {
        World matchWorld = match.getWorld();
        VariorumMap map = match.getMap();

        TeamsModule teamsModule = match.getRequiredModule(TeamsModule.class);
        Optional<Team> playerTeam = teamsModule.getPlayerTeam(player);

        if (playerTeam.isPresent()) {
            Optional<VariorumMap.Spawns.TeamSpawn> teamSpawn = map.getSpawns().getTeamSpawns().stream()
                    .filter(spawn -> spawn.getTeam().equals(playerTeam.get().id()))
                    .findFirst();

            if (teamSpawn.isPresent()) {
                VariorumMap.Point spawnPoint = teamSpawn.get().getRegion().getPoint();
                double yaw = teamSpawn.get().getRegion().getYaw();
                return new Location(
                        matchWorld, spawnPoint.getX(), spawnPoint.getY(), spawnPoint.getZ(), (float) yaw, 0.0f);
            }
        }

        return getDefaultSpawn();
    }

    private void applySpawnLoadout(Player player, VariorumMap.Spawns.SpawnRegion spawnRegion) {
        match.getModule(LoadoutsModule.class).ifPresent(loadoutsModule -> {
            Team team = match.getRequiredModule(TeamsModule.class)
                    .getPlayerTeam(player)
                    .orElse(null);

            String loadoutId = spawnRegion.getLoadout() != null ? spawnRegion.getLoadout() : "default";
            if (match.getRegistry().has(loadoutId)) {
                loadoutsModule.applyLoadout(player, loadoutId, team);
            }
        });
    }

    public Location getDefaultSpawn() {
        World matchWorld = match.getWorld();
        VariorumMap map = match.getMap();

        VariorumMap.Point defaultPoint = map.getSpawns().getDefaultSpawn().getPoint();
        double defaultYaw = map.getSpawns().getDefaultSpawn().getYaw();

        return new Location(
                matchWorld, defaultPoint.getX(), defaultPoint.getY(), defaultPoint.getZ(), (float) defaultYaw, 0.0f);
    }

    public void spawnPlayer(Player player, boolean giveLoadout) {
        World matchWorld = match.getWorld();
        if (matchWorld == null) {
            Variorum.get().getLogger().warning("Match world is null when trying to spawn " + player.getName());
            return;
        }

        TeamsModule teamsModule = match.getRequiredModule(TeamsModule.class);
        Team playerTeam = teamsModule.getPlayerTeam(player).orElse(null);

        Players.reset(player);

        PlayerSpawnStartEvent call = new PlayerSpawnStartEvent(player, playerTeam, giveLoadout, true);
        Events.call(call);

        Location spawn = getSpawnLocation(player);
        player.teleport(spawn);

        if (playerTeam != null && giveLoadout) {
            VariorumMap map = match.getMap();
            Optional<VariorumMap.Spawns.TeamSpawn> teamSpawn = map.getSpawns().getTeamSpawns().stream()
                    .filter(s -> s.getTeam().equals(playerTeam.id()))
                    .findFirst();

            if (teamSpawn.isPresent()) {
                applySpawnLoadout(player, teamSpawn.get().getRegion());
            } else {
                applySpawnLoadout(player, map.getSpawns().getDefaultSpawn());
            }
        }

        Variorum.get()
                .getLogger()
                .info("Spawned " + player.getName() + " at "
                        + String.format(
                                "%.1f, %.1f, %.1f (yaw: %.1f) in %s",
                                spawn.getX(), spawn.getY(), spawn.getZ(), spawn.getYaw(), matchWorld.getName()));
    }
}
