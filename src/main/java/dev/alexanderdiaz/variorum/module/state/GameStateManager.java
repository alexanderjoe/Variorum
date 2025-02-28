package dev.alexanderdiaz.variorum.module.state;

import dev.alexanderdiaz.variorum.Variorum;
import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.util.Events;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import org.bukkit.scheduler.BukkitTask;

public class GameStateManager {
    private final Match match;
    private final Map<GameState, GameStateHandler> handlers = new EnumMap<>(GameState.class);

    @Getter
    private GameState currentState;

    private BukkitTask tickTask;

    /**
     * Creates a new game state manager.
     *
     * @param match The match this manager is for
     * @param initialState The initial game state
     */
    public GameStateManager(Match match, GameState initialState) {
        this.match = match;
        this.currentState = initialState;

        this.tickTask = Variorum.get().getServer().getScheduler().runTaskTimer(Variorum.get(), this::tick, 20L, 20L);
    }

    /**
     * Registers a handler for a specific game state.
     *
     * @param handler The handler to register
     */
    public void registerHandler(GameStateHandler handler) {
        handlers.put(handler.getState(), handler);
    }

    /**
     * Gets the handler for a specific game state.
     *
     * @param state The state to get a handler for
     * @return The handler, or empty if none is registered
     */
    public Optional<GameStateHandler> getHandler(GameState state) {
        return Optional.ofNullable(handlers.get(state));
    }

    /**
     * Transitions to a new game state.
     *
     * @param newState The state to transition to
     */
    public void transitionTo(GameState newState) {
        if (currentState == newState) {
            return;
        }

        GameState oldState = currentState;

        GameStateTransition transition = new GameStateTransition(match, oldState, newState);
        transition.execute(this);

        currentState = newState;

        Events.call(new GameStateChangeEvent(match, oldState, newState));
    }

    private void tick() {
        getHandler(currentState).ifPresent(GameStateHandler::onTick);
    }

    public void cleanup() {
        if (tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }

        handlers.clear();
    }
}
