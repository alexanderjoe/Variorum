package dev.alexanderdiaz.variorum.module.state;

import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.Module;
import dev.alexanderdiaz.variorum.module.state.countdown.CountdownManager;
import dev.alexanderdiaz.variorum.module.state.handlers.*;
import dev.alexanderdiaz.variorum.util.Events;
import lombok.Getter;

public class GameStateModule implements Module {
    private final Match match;

    @Getter
    private GameStateManager stateManager;

    @Getter
    private CountdownManager countdownManager;

    @Getter
    private WaitingStateHandler waitingHandler;

    @Getter
    private CountdownStateHandler countdownHandler;

    @Getter
    private PlayingStateHandler playingHandler;

    @Getter
    private EndedStateHandler endedHandler;

    private GameListener gameListener;

    public GameStateModule(Match match) {
        this.match = match;
    }

    @Override
    public void enable() {
        this.countdownManager = new CountdownManager(match);
        this.stateManager = new GameStateManager(match, GameState.WAITING);
        this.waitingHandler = new WaitingStateHandler(match, countdownManager);
        this.countdownHandler = new CountdownStateHandler(match, countdownManager);
        this.playingHandler = new PlayingStateHandler(match);
        this.endedHandler = new EndedStateHandler(match, countdownManager);

        stateManager.registerHandler(waitingHandler);
        stateManager.registerHandler(countdownHandler);
        stateManager.registerHandler(playingHandler);
        stateManager.registerHandler(endedHandler);

        this.gameListener = new GameListener(match, this);
        Events.register(gameListener);

        // Initial state setup - force a transition to WAITING to run the onEnter logic
        stateManager.transitionTo(GameState.WAITING);
    }

    @Override
    public void disable() {
        if (stateManager != null) {
            stateManager.cleanup();
            stateManager = null;
        }

        if (countdownManager != null) {
            countdownManager.cleanup();
            countdownManager = null;
        }

        if (gameListener != null) {
            Events.unregister(gameListener);
            gameListener = null;
        }
    }

    /**
     * Gets the current game state.
     *
     * @return The current game state
     */
    public GameState getCurrentState() {
        return stateManager.getCurrentState();
    }

    /**
     * Sets the game state.
     *
     * @param newState The new game state to transition to
     */
    public void setState(GameState newState) {
        stateManager.transitionTo(newState);
    }

    /**
     * Starts a countdown with the specified duration.
     *
     * @param seconds The duration of the countdown in seconds
     */
    public void startCountdown(int seconds) {
        if (getCurrentState() == GameState.WAITING || getCurrentState() == GameState.COUNTDOWN) {
            setState(GameState.COUNTDOWN);
            countdownHandler.setCountdownDuration(seconds);
        }
    }
}
