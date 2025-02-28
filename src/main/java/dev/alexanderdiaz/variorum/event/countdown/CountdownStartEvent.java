package dev.alexanderdiaz.variorum.event.countdown;

import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.state.countdown.Countdown;
import lombok.Getter;

@Getter
public class CountdownStartEvent extends CountdownEvent {
    private final int startSeconds;

    public CountdownStartEvent(Match match, Countdown countdown, int startSeconds) {
        super(match, countdown);
        this.startSeconds = startSeconds;
    }
}
