package dev.alexanderdiaz.variorum.event.countdown;

import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.state.countdown.Countdown;

public class CountdownCompleteEvent extends CountdownEvent {
  public CountdownCompleteEvent(Match match, Countdown countdown) {
    super(match, countdown);
  }
}
