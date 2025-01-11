package dev.alexanderdiaz.variorum.module.objectives.wool;

import dev.alexanderdiaz.variorum.match.Match;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
@Getter
public class WoolPlaceEvent extends Event {
    private final Match match;
    private final WoolObjective objective;
    private final Player player;

    private static final HandlerList handlers = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
