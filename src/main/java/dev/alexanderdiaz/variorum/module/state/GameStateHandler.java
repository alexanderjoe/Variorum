package dev.alexanderdiaz.variorum.module.state;

/** Interface for handlers that manage specific game states. */
public interface GameStateHandler {
  /**
   * Gets the state this handler is responsible for.
   *
   * @return The game state
   */
  GameState getState();

  /**
   * Called when entering this state.
   *
   * @param transition The state transition
   */
  void onEnter(GameStateTransition transition);

  /**
   * Called when exiting this state.
   *
   * @param transition The state transition
   */
  void onExit(GameStateTransition transition);

  /** Called for periodic updates while in this state. */
  default void onTick() {
    //
  }
}
