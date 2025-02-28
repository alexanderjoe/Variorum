package dev.alexanderdiaz.variorum.event.countdown;

import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.state.countdown.Countdown;
import lombok.Getter;

@Getter
public class CountdownCancelEvent extends CountdownEvent {
    private final int remainingSeconds;

    public CountdownCancelEvent(Match match, Countdown countdown, int remainingSeconds) {
        super(match, countdown);
        this.remainingSeconds = remainingSeconds;
    }
}
