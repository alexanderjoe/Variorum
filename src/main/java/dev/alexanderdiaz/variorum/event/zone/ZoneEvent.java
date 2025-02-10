package dev.alexanderdiaz.variorum.event.zone;

import dev.alexanderdiaz.variorum.module.zones.Zone;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public abstract class ZoneEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    @Getter
    protected final Player player;

    @Getter
    protected final Zone zone;

    protected ZoneEvent(Player player, Zone zone) {
        this.player = player;
        this.zone = zone;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
