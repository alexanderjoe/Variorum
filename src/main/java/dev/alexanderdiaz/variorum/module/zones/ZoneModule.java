package dev.alexanderdiaz.variorum.module.zones;

import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.Module;
import dev.alexanderdiaz.variorum.util.Events;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

public class ZoneModule implements Module {
    private final Match match;
    private ZoneListener listener;

    @Getter
    private final Map<String, Zone> zones;

    public ZoneModule(Match match) {
        this.match = match;
        this.zones = new HashMap<>();
    }

    public void addZone(Zone zone) {
        zones.put(zone.getId(), zone);
    }

    public Collection<Zone> getZonesContaining(org.bukkit.entity.Player player) {
        return zones.values().stream().filter(zone -> zone.contains(player)).toList();
    }

    @Override
    public void enable() {
        this.listener = new ZoneListener(this);
        Events.register(listener);

        // Enable all checks in all zones
        zones.values().stream().flatMap(zone -> zone.getChecks().stream()).forEach(ZoneCheck::enable);
    }

    @Override
    public void disable() {
        if (listener != null) {
            Events.unregister(listener);
        }

        // Disable all checks in all zones
        zones.values().stream().flatMap(zone -> zone.getChecks().stream()).forEach(ZoneCheck::disable);

        zones.clear();
    }
}
