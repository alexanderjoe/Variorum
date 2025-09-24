package dev.alexanderdiaz.variorum.event.match;

import dev.alexanderdiaz.variorum.match.Match;
import org.bukkit.event.HandlerList;

public class MatchLoadEvent extends MatchEvent {
  private static final HandlerList handlers = new HandlerList();

  public MatchLoadEvent(Match match) {
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
