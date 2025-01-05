package dev.alexanderdiaz.variorum.listener;

import dev.alexanderdiaz.variorum.Variorum;
import dev.alexanderdiaz.variorum.map.VariorumMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class VariorumListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.joinMessage(null);

        if (Variorum.getMatch() == null) return;

        Variorum.get().getLogger().info(event.getPlayer().getName() + " joined, match " + Variorum.getMatch().toString() + " is active.");
        teleportToSpawn(event.getPlayer());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (Variorum.getMatch() == null) return;

        teleportToSpawn(event.getPlayer());
    }

    private void teleportToSpawn(org.bukkit.entity.Player player) {
        if (Variorum.getMatch() == null) return;

        World matchWorld = Variorum.getMatch().getWorld();
        VariorumMap map = Variorum.getMatch().getMap();
        VariorumMap.Point spawnPoint = map.getSpawns().getDefaultSpawn().getPoint();
        double yaw = map.getSpawns().getDefaultSpawn().getYaw();

        Location spawn = new Location(matchWorld,
                spawnPoint.getX(),
                spawnPoint.getY(),
                spawnPoint.getZ(),
                (float) yaw,
                0.0f
        );

        player.teleport(spawn);
    }
}