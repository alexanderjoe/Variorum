package dev.alexanderdiaz.variorum.module.zones;

import dev.alexanderdiaz.variorum.event.zone.ZoneEnterEvent;
import dev.alexanderdiaz.variorum.event.zone.ZoneLeaveEvent;
import dev.alexanderdiaz.variorum.util.Events;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class ZoneListener implements Listener {
    private final ZoneModule module;

    public ZoneListener(ZoneModule module) {
        this.module = module;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        // Only check if the block position changed (not just looking around)
        Location from = event.getFrom();
        Location to = event.getTo();
        if (from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        handleZoneTransitions(player, from, to);
    }

    private void handleZoneTransitions(Player player, Location from, Location to) {
        // Get all zones at the old and new positions
        var oldZones = module.getZones().values().stream()
                .filter(zone -> zone.getRegion().contains(from))
                .toList();

        var newZones = module.getZones().values().stream()
                .filter(zone -> zone.getRegion().contains(to))
                .toList();

        // Fire exit events for zones the player left
        oldZones.stream()
                .filter(zone -> !newZones.contains(zone))
                .forEach(zone -> Events.call(new ZoneLeaveEvent(player, zone)));

        // Fire enter events for zones the player entered
        newZones.stream()
                .filter(zone -> !oldZones.contains(zone))
                .forEach(zone -> Events.call(new ZoneEnterEvent(player, zone)));
    }
}
