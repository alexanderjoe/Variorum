package dev.alexanderdiaz.variorum.event.team;

import dev.alexanderdiaz.variorum.module.team.Team;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Nullable;

public class PlayerChangeTeamEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    @Getter
    private final Player player;
    @Getter
    @Nullable
    private final Team fromTeam;
    @Getter
    private final Team toTeam;

    public PlayerChangeTeamEvent(Player player, @Nullable Team fromTeam, Team toTeam) {
        this.player = player;
        this.fromTeam = fromTeam;
        this.toTeam = toTeam;
    }

    @Override
    public HandlerList getHandlers() {
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
