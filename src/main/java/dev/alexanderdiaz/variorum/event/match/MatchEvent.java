package dev.alexanderdiaz.variorum.event.match;

import dev.alexanderdiaz.variorum.match.Match;
import lombok.Getter;
import org.bukkit.event.Event;

public abstract class MatchEvent extends Event {
    @Getter
    final Match match;

    protected MatchEvent(Match match) {
        this.match = match;
    }
}
