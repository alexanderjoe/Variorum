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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;

public class GameStateModule implements Module {
    private final Match match;
    private GameListener listener;
    private BukkitTask countdownTask;
    private BukkitTask waitingMessageTask;

    private static final int MIN_PLAYERS = 2; // Configurable minimum

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
                60L, // Initial delay of 3 seconds
                600L // Repeat every 30 seconds (20 ticks * 30)
        );
    }

    @Override
    public void enable() {
        this.listener = new GameListener();
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
    }

    public void setState(GameState newState) {
        if (this.currentState == newState) return;

        GameState oldState = this.currentState;
        this.currentState = newState;

        // Handle state transitions
        switch (newState) {
            case WAITING:
                match.getWorld().getPlayers().forEach(player -> {
                    player.setGameMode(GameMode.ADVENTURE);
                    player.sendMessage(Component.text("Waiting for more players...", NamedTextColor.YELLOW));
                });
                startWaitingMessages();
                break;

            case COUNTDOWN:
                startCountdown(10); // Default 10 seconds
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
                // Schedule match end after showing end screen
                Variorum.get().getServer().getScheduler().runTaskLater(Variorum.get(),
                        () -> match.end(), 100L);
                break;
        }

        Events.call(new GameStateChangeEvent(match, oldState, newState));
    }

    public void startCountdown(int seconds) {
        // Cancel any existing countdown
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

                    // Show countdown to all players
                    // For longer countdowns, only show messages at specific intervals
                    boolean shouldAnnounce = secondsLeft[0] <= 5 || // Last 5 seconds
                            secondsLeft[0] <= 30 && secondsLeft[0] % 5 == 0 || // Every 5s under 30s
                            secondsLeft[0] <= 60 && secondsLeft[0] % 10 == 0 || // Every 10s under 60s
                            secondsLeft[0] % 30 == 0; // Every 30s otherwise

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

    private class GameListener implements Listener {
        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) {
            if (currentState == GameState.WAITING) {
                // Start countdown if enough players
                if (match.getWorld().getPlayers().size() >= MIN_PLAYERS) {
                    setState(GameState.COUNTDOWN);
                }
            }

            // Set appropriate gamemode
            switch (currentState) {
                case WAITING, COUNTDOWN, ENDED:
                    event.getPlayer().setGameMode(GameMode.ADVENTURE);
                    break;
                case PLAYING:
                    event.getPlayer().setGameMode(GameMode.SURVIVAL);
                    break;
            }
        }

        @EventHandler
        public void onBlockBreak(BlockBreakEvent event) {
            if (currentState != GameState.PLAYING) {
                event.setCancelled(true);
            }
        }

        @EventHandler
        public void onBlockPlace(BlockPlaceEvent event) {
            if (currentState != GameState.PLAYING) {
                event.setCancelled(true);
            }
        }

        @EventHandler
        public void onEntityDamage(EntityDamageEvent event) {
            if (currentState != GameState.PLAYING && event.getEntity() instanceof Player) {
                event.setCancelled(true);
            }
        }
    }
}