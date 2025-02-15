package dev.alexanderdiaz.variorum.module.zones;

import dev.alexanderdiaz.variorum.Variorum;
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
        Location from = event.getFrom();
        Location to = event.getTo();
        if (from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        if (handleZoneTransitions(player, from, to)) {
            event.setCancelled(true);
            player.teleport(from);
        }
    }

    private boolean handleZoneTransitions(Player player, Location from, Location to) {
        var oldZones = module.getZones().values().stream()
                .filter(zone -> zone.getRegion().contains(from))
                .toList();

        var newZones = module.getZones().values().stream()
                .filter(zone -> zone.getRegion().contains(to))
                .toList();

        boolean anyCancelled = false;

        // player leaves a zone they were in
        for (Zone zone : oldZones) {
            if (!newZones.contains(zone)) {
                Variorum.get()
                        .getLogger()
                        .info(player.getName() + " has exited zone " + zone.getId()); // todo remove debug
                ZoneLeaveEvent event = new ZoneLeaveEvent(player, zone);
                Events.call(event);
                if (event.isCancelled()) {
                    anyCancelled = true;
                }
            }
        }

        // player enters new zones
        for (Zone zone : newZones) {
            if (!oldZones.contains(zone)) {
                Variorum.get()
                        .getLogger()
                        .info(player.getName() + " has entered zone " + zone.getId()); // todo remove debug
                ZoneEnterEvent event = new ZoneEnterEvent(player, zone);
                Events.call(event);
                if (event.isCancelled()) {
                    anyCancelled = true;
                }
            }
        }

        return anyCancelled;
    }
}
