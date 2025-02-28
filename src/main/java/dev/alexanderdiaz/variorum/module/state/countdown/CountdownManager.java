package dev.alexanderdiaz.variorum.module.state.countdown;

import dev.alexanderdiaz.variorum.match.Match;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import lombok.Getter;

public class CountdownManager {
    private final Match match;
    private final Map<String, Countdown> countdowns = new HashMap<>();

    @Getter
    private Countdown activeCountdown;

    public CountdownManager(Match match) {
        this.match = match;
    }

    /**
     * Creates a new countdown with the given ID.
     *
     * @param id The unique identifier for this countdown
     * @param seconds The number of seconds to count down from
     * @param options Configuration options for the countdown
     * @param onComplete A callback to run when the countdown completes
     * @return The created countdown
     */
    public Countdown createCountdown(String id, int seconds, CountdownOptions options, Consumer<Countdown> onComplete) {
        Countdown countdown = new Countdown(match, seconds, options, cd -> {
            activeCountdown = null;
            if (onComplete != null) {
                onComplete.accept(cd);
            }
        });

        countdowns.put(id, countdown);
        return countdown;
    }

    /**
     * Gets a countdown by its ID.
     *
     * @param id The ID of the countdown to get
     * @return The countdown, or null if not found
     */
    public Countdown getCountdown(String id) {
        return countdowns.get(id);
    }

    /**
     * Starts a countdown with the given ID. If another countdown is running, it will be stopped.
     *
     * @param id The ID of the countdown to start
     * @return The started countdown, or null if not found
     */
    public Countdown startCountdown(String id) {
        Countdown countdown = countdowns.get(id);
        if (countdown == null) {
            return null;
        }

        if (activeCountdown != null && activeCountdown.isRunning()) {
            activeCountdown.stop();
        }

        activeCountdown = countdown;
        countdown.start();

        return countdown;
    }

    public void stopActiveCountdown() {
        if (activeCountdown != null && activeCountdown.isRunning()) {
            activeCountdown.stop();
            activeCountdown = null;
        }
    }

    public void cleanup() {
        countdowns.values().forEach(Countdown::stop);
        countdowns.clear();
        activeCountdown = null;
    }
}
