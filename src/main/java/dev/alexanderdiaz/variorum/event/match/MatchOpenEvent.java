package dev.alexanderdiaz.variorum.event.match;

import dev.alexanderdiaz.variorum.match.Match;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class MatchOpenEvent extends MatchEvent {
    private static final HandlerList handlers = new HandlerList();

    public MatchOpenEvent(Match match) {
        super(match);
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
