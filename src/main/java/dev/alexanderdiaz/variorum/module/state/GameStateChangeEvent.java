package dev.alexanderdiaz.variorum.module.state;

import dev.alexanderdiaz.variorum.match.Match;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class GameStateChangeEvent extends Event {
  private static final HandlerList handlers = new HandlerList();

  private final Match match;
  private final GameState oldState;
  private final GameState newState;

  public GameStateChangeEvent(Match match, GameState oldState, GameState newState) {
    this.match = match;
    this.oldState = oldState;
    this.newState = newState;
  }

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }
}
