package dev.alexanderdiaz.variorum.module.state.handlers;

import dev.alexanderdiaz.variorum.Variorum;
import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.state.GameState;
import dev.alexanderdiaz.variorum.module.state.GameStateHandler;
import dev.alexanderdiaz.variorum.module.state.GameStateModule;
import dev.alexanderdiaz.variorum.module.state.GameStateTransition;
import dev.alexanderdiaz.variorum.module.state.countdown.CountdownManager;
import dev.alexanderdiaz.variorum.module.state.countdown.CountdownOptions;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.scheduler.BukkitTask;

@RequiredArgsConstructor
public class WaitingStateHandler implements GameStateHandler {
  private static final int MIN_PLAYERS = 2;
  private static final long MESSAGE_INTERVAL = 1200L; // 60 seconds in ticks

  private final Match match;
  private final CountdownManager countdownManager;

  private BukkitTask waitingMessageTask;

  @Override
  public GameState getState() {
    return GameState.WAITING;
  }

  @Override
  public void onEnter(GameStateTransition transition) {
    match.getWorld().getPlayers().forEach(player -> {
      player.setGameMode(GameMode.ADVENTURE);
      player.sendMessage(Component.text("Waiting for more players...", NamedTextColor.YELLOW));
    });

    startWaitingMessages();

    countdownManager.createCountdown(
        "waiting_ready", 10, CountdownOptions.matchStart(), countdown -> {
          transition.execute(match.getRequiredModule(GameStateModule.class).getStateManager());
        });
  }

  @Override
  public void onExit(GameStateTransition transition) {
    if (waitingMessageTask != null) {
      waitingMessageTask.cancel();
      waitingMessageTask = null;
    }
  }

  @Override
  public void onTick() {
    int playerCount = match.getWorld().getPlayers().size();
    if (playerCount >= MIN_PLAYERS) {
      countdownManager.startCountdown("waiting_ready");
    } else {
      countdownManager.stopActiveCountdown();
    }
  }

  private void startWaitingMessages() {
    if (waitingMessageTask != null) {
      waitingMessageTask.cancel();
    }

    waitingMessageTask = Variorum.get()
        .getServer()
        .getScheduler()
        .runTaskTimer(
            Variorum.get(),
            () -> {
              int currentPlayers = match.getWorld().getPlayers().size();
              int neededPlayers = Math.max(0, MIN_PLAYERS - currentPlayers);

              if (neededPlayers > 0) {
                Component message = Component.text()
                    .append(Component.text("Waiting for ", NamedTextColor.YELLOW))
                    .append(Component.text(neededPlayers, NamedTextColor.RED))
                    .append(Component.text(
                        " more player" + (neededPlayers != 1 ? "s" : ""), NamedTextColor.YELLOW))
                    .append(Component.text(" to start!", NamedTextColor.YELLOW))
                    .build();

                match.getWorld().getPlayers().forEach(player -> player.sendMessage(message));
              }
            },
            60L,
            MESSAGE_INTERVAL);
  }
}
