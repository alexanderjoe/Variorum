package dev.alexanderdiaz.variorum.module.state;

import dev.alexanderdiaz.variorum.match.Match;
import lombok.Getter;

/** Represents a transition between game states. */
@Getter
public class GameStateTransition {
  private final GameState fromState;
  private final GameState toState;
  private final Match match;

  /**
   * Creates a new state transition.
   *
   * @param match The match this transition is for
   * @param fromState The state transitioning from
   * @param toState The state transitioning to
   */
  public GameStateTransition(Match match, GameState fromState, GameState toState) {
    this.match = match;
    this.fromState = fromState;
    this.toState = toState;
  }

  /** Checks if this transition is from the specified state. */
  public boolean isFrom(GameState state) {
    return fromState == state;
  }

  /** Checks if this transition is to the specified state. */
  public boolean isTo(GameState state) {
    return toState == state;
  }

  /** Checks if this transition is between the specified states. */
  public boolean isBetween(GameState from, GameState to) {
    return fromState == from && toState == to;
  }

  /** Makes a transition by preparing the from state to exit and the to state to enter. */
  public void execute(GameStateManager manager) {
    // First handle exiting the current state
    manager.getHandler(fromState).ifPresent(handler -> handler.onExit(this));

    // Then handle entering the new state
    manager.getHandler(toState).ifPresent(handler -> handler.onEnter(this));
  }
}
