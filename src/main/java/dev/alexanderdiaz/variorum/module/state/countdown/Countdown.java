package dev.alexanderdiaz.variorum.module.state.countdown;

import dev.alexanderdiaz.variorum.Variorum;
import dev.alexanderdiaz.variorum.event.countdown.CountdownCancelEvent;
import dev.alexanderdiaz.variorum.event.countdown.CountdownCompleteEvent;
import dev.alexanderdiaz.variorum.event.countdown.CountdownStartEvent;
import dev.alexanderdiaz.variorum.event.countdown.CountdownTickEvent;
import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.util.Events;
import java.time.Duration;
import java.util.function.Consumer;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.scheduler.BukkitTask;

public class Countdown {
    private final Match match;
    private final int initialSeconds;
    private final CountdownOptions options;
    private final Consumer<Countdown> onComplete;

    private BukkitTask task;

    @Getter
    private int secondsLeft;

    @Getter
    private boolean running = false;

    /**
     * Creates a new countdown.
     *
     * @param match The match this countdown is for
     * @param seconds The number of seconds to count down from
     * @param options Configuration options for the countdown
     * @param onComplete A callback to run when the countdown completes
     */
    public Countdown(Match match, int seconds, CountdownOptions options, Consumer<Countdown> onComplete) {
        this.match = match;
        this.initialSeconds = seconds;
        this.secondsLeft = seconds;
        this.options = options;
        this.onComplete = onComplete;
    }

    /** Starts the countdown. If already running, this has no effect. */
    public void start() {
        if (running) {
            return;
        }

        running = true;
        secondsLeft = initialSeconds;

        Events.call(new CountdownStartEvent(match, this, secondsLeft));

        task = Variorum.get().getServer().getScheduler().runTaskTimer(Variorum.get(), this::tick, 0L, 20L);
    }

    /** Stops the countdown if it's running. */
    public void stop() {
        if (!running) {
            return;
        }

        running = false;

        if (task != null) {
            task.cancel();
            task = null;
        }

        Events.call(new CountdownCancelEvent(match, this, secondsLeft));
    }

    /** Called every tick (1 second) to update the countdown. */
    private void tick() {
        if (secondsLeft <= 0) {
            running = false;
            task.cancel();
            task = null;

            Events.call(new CountdownCompleteEvent(match, this));

            if (onComplete != null) {
                onComplete.accept(this);
            }
            return;
        }

        boolean shouldAnnounce = shouldAnnounce(secondsLeft);
        CountdownTickEvent tickEvent = new CountdownTickEvent(match, this, secondsLeft, shouldAnnounce);
        Events.call(tickEvent);

        if (shouldAnnounce && !tickEvent.isCancelled() && options.isAnnouncementsEnabled()) {
            announce(secondsLeft);
        }

        secondsLeft--;
    }

    /** Determines if the current second should be announced. */
    private boolean shouldAnnounce(int seconds) {
        return seconds <= 5
                || seconds <= 30 && seconds % 5 == 0
                || seconds <= 60 && seconds % 10 == 0
                || seconds % 30 == 0;
    }

    /** Announces the current countdown time to all players. */
    private void announce(int seconds) {
        if (options.isChatsEnabled()) {
            Component message = options.getPrefix().append(Component.text(seconds, NamedTextColor.RED));

            match.getWorld().getPlayers().forEach(player -> {
                player.sendMessage(message);
            });
        }

        if (options.isTitlesEnabled() && seconds <= 5) {
            match.getWorld().getPlayers().forEach(player -> {
                player.showTitle(Title.title(
                        Component.text(seconds, NamedTextColor.RED),
                        Component.empty(),
                        Title.Times.times(Duration.ofMillis(0), Duration.ofMillis(1000), Duration.ofMillis(0))));
            });
        }
    }
}
