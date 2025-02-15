package dev.alexanderdiaz.variorum.event.zone;

import dev.alexanderdiaz.variorum.module.zones.Zone;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public abstract class ZoneEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

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

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
