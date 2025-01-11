package dev.alexanderdiaz.variorum.module.objectives.monument;

import dev.alexanderdiaz.variorum.match.Match;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class MonumentDestroyedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Match match;
    private final MonumentObjective monument;

    public MonumentDestroyedEvent(Match match, MonumentObjective monument) {
        this.match = match;
        this.monument = monument;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
