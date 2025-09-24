package dev.alexanderdiaz.variorum.event.countdown;

import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.state.countdown.Countdown;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public abstract class CountdownEvent extends Event {
  private static final HandlerList handlers = new HandlerList();

  protected final Match match;
  protected final Countdown countdown;

  protected CountdownEvent(Match match, Countdown countdown) {
    this.match = match;
    this.countdown = countdown;
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }
}
