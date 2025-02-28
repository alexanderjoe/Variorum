package dev.alexanderdiaz.variorum.module.state.handlers;

import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.state.GameState;
import dev.alexanderdiaz.variorum.module.state.GameStateHandler;
import dev.alexanderdiaz.variorum.module.state.GameStateModule;
import dev.alexanderdiaz.variorum.module.state.GameStateTransition;
import dev.alexanderdiaz.variorum.module.state.countdown.CountdownManager;
import dev.alexanderdiaz.variorum.module.state.countdown.CountdownOptions;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CountdownStateHandler implements GameStateHandler {
    private final Match match;
    private final CountdownManager countdownManager;

    @Override
    public GameState getState() {
        return GameState.COUNTDOWN;
    }

    @Override
    public void onEnter(GameStateTransition transition) {
        if (countdownManager.getCountdown("main_countdown") == null) {
            countdownManager.createCountdown("main_countdown", 30, CountdownOptions.matchStart(), countdown -> {
                match.getRequiredModule(GameStateModule.class).getStateManager().transitionTo(GameState.PLAYING);
            });
        }

        countdownManager.startCountdown("main_countdown");
    }

    @Override
    public void onExit(GameStateTransition transition) {}

    /**
     * Sets the countdown duration.
     *
     * @param seconds The number of seconds for the countdown
     */
    public void setCountdownDuration(int seconds) {
        countdownManager.stopActiveCountdown();

        countdownManager.createCountdown("main_countdown", seconds, CountdownOptions.matchStart(), countdown -> {
            match.getRequiredModule(GameStateModule.class).getStateManager().transitionTo(GameState.PLAYING);
        });

        countdownManager.startCountdown("main_countdown");
    }
}
