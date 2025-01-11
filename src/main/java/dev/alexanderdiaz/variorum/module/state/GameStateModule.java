package dev.alexanderdiaz.variorum.module.state;

import dev.alexanderdiaz.variorum.Variorum;
import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.Module;
import dev.alexanderdiaz.variorum.util.Events;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.GameMode;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;

public class GameStateModule implements Module {
    private final Match match;
    private GameListener listener;
    private BukkitTask countdownTask;
    private BukkitTask waitingMessageTask;
    private BukkitTask cycleTask;

    protected static final int MIN_PLAYERS = 2;
    protected static final int CYCLE_DELAY_TICKS = 200; // 10 seconds

    @Getter
    private GameState currentState;

    public GameStateModule(Match match) {
        this.match = match;
        this.currentState = GameState.WAITING;
    }

    private void startWaitingMessages() {
        if (waitingMessageTask != null) {
            waitingMessageTask.cancel();
        }

        waitingMessageTask = Variorum.get().getServer().getScheduler().runTaskTimer(
                Variorum.get(),
                () -> {
                    if (currentState == GameState.WAITING) {
                        int currentPlayers = match.getWorld().getPlayers().size();
                        int neededPlayers = Math.max(0, MIN_PLAYERS - currentPlayers);

                        if (neededPlayers > 0) {
                            Component message = Component.text()
                                    .append(Component.text("Waiting for ", NamedTextColor.YELLOW))
                                    .append(Component.text(neededPlayers, NamedTextColor.RED))
                                    .append(Component.text(" more player" + (neededPlayers != 1 ? "s" : ""), NamedTextColor.YELLOW))
                                    .append(Component.text(" to start!", NamedTextColor.YELLOW))
                                    .build();

                            match.getWorld().getPlayers().forEach(player ->
                                    player.sendMessage(message));
                        }
                    } else {
                        waitingMessageTask.cancel();
                    }
                },
                60L,
                1200L
        );
    }

    @Override
    public void enable() {
        this.listener = new GameListener(match, this);
        Events.register(listener);
        setState(GameState.WAITING);
        startWaitingMessages();
    }

    @Override
    public void disable() {
        if (listener != null) {
            Events.unregister(listener);
        }
        if (countdownTask != null) {
            countdownTask.cancel();
        }
        if (waitingMessageTask != null) {
            waitingMessageTask.cancel();
        }
        if (cycleTask != null) {
            cycleTask.cancel();
        }
    }

    public void setState(GameState newState) {
        if (this.currentState == newState) return;

        GameState oldState = this.currentState;
        this.currentState = newState;

        switch (newState) {
            case WAITING:
                match.getWorld().getPlayers().forEach(player -> {
                    player.setGameMode(GameMode.ADVENTURE);
                    player.sendMessage(Component.text("Waiting for more players...", NamedTextColor.YELLOW));
                });
                startWaitingMessages();
                break;

            case COUNTDOWN:
                startCountdown(10);
                break;

            case PLAYING:
                match.getWorld().getPlayers().forEach(player -> {
                    player.setGameMode(GameMode.SURVIVAL);
                    player.showTitle(Title.title(
                            Component.text("Match Started!", NamedTextColor.GREEN),
                            Component.text("Good luck!", NamedTextColor.YELLOW),
                            Title.Times.times(Duration.ofSeconds(1), Duration.ofSeconds(3), Duration.ofSeconds(1))
                    ));
                });
                break;

            case ENDED:
                if (countdownTask != null) {
                    countdownTask.cancel();
                }
                match.getWorld().getPlayers().forEach(player -> {
                    player.setGameMode(GameMode.ADVENTURE);
                    player.showTitle(Title.title(
                            Component.text("Game Over!", NamedTextColor.GOLD),
                            Component.empty(),
                            Title.Times.times(Duration.ofSeconds(1), Duration.ofSeconds(3), Duration.ofSeconds(1))
                    ));
                });

                // Schedule map cycling
                cycleTask = Variorum.get().getServer().getScheduler().runTaskLater(
                        Variorum.get(),
                        () -> Variorum.get().getMatchManager().cycleToNextMatch(),
                        CYCLE_DELAY_TICKS
                );
                break;
        }

        Events.call(new GameStateChangeEvent(match, oldState, newState));
    }

    public void startCountdown(int seconds) {
        if (countdownTask != null) {
            countdownTask.cancel();
        }

        final Component COUNTDOWN_PREFIX = Component.text("Match starting in ", NamedTextColor.YELLOW);

        match.getWorld().getPlayers().forEach(player -> {
            player.setGameMode(GameMode.ADVENTURE);
            player.sendMessage(Component.text("Match starting soon!", NamedTextColor.GREEN));
        });

        final int[] secondsLeft = {seconds};

        countdownTask = Variorum.get().getServer().getScheduler().runTaskTimer(
                Variorum.get(),
                () -> {
                    if (secondsLeft[0] <= 0) {
                        countdownTask.cancel();
                        setState(GameState.PLAYING);
                        return;
                    }

                    boolean shouldAnnounce = secondsLeft[0] <= 5 ||
                            secondsLeft[0] <= 30 && secondsLeft[0] % 5 == 0 ||
                            secondsLeft[0] <= 60 && secondsLeft[0] % 10 == 0 ||
                            secondsLeft[0] % 30 == 0;

                    if (shouldAnnounce) {
                        match.getWorld().getPlayers().forEach(player -> {
                            player.sendMessage(COUNTDOWN_PREFIX.append(Component.text(secondsLeft[0], NamedTextColor.RED)));
                            if (secondsLeft[0] <= 5) {
                                player.showTitle(Title.title(
                                        Component.text(secondsLeft[0], NamedTextColor.RED),
                                        Component.empty(),
                                        Title.Times.times(Duration.ofMillis(0), Duration.ofMillis(1000), Duration.ofMillis(0))
                                ));
                            }
                        });
                    }

                    secondsLeft[0]--;
                },
                0L, 20L
        );
    }
}
