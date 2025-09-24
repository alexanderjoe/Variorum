package dev.alexanderdiaz.variorum.event.countdown;

import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.state.countdown.Countdown;
import lombok.Getter;
import org.bukkit.event.Cancellable;

@Getter
public class CountdownTickEvent extends CountdownEvent implements Cancellable {
  private final int secondsLeft;
  private final boolean announced;
  private boolean cancelled = false;

  public CountdownTickEvent(Match match, Countdown countdown, int secondsLeft, boolean announced) {
    super(match, countdown);
    this.secondsLeft = secondsLeft;
    this.announced = announced;
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
