package dev.alexanderdiaz.variorum.command;

import dev.alexanderdiaz.variorum.Variorum;
import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.state.GameState;
import dev.alexanderdiaz.variorum.module.state.GameStateModule;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.*;

public class GameStateCommands {
  private final Variorum plugin;

  public GameStateCommands(Variorum plugin) {
    this.plugin = plugin;
  }

  @Command("start|s [time]")
  @CommandDescription("Force start the match with optional countdown time")
  @Permission("variorum.command.start")
  public void startMatch(
      final CommandSender sender, final @Default("3s") @Argument(value = "time") String timeStr) {
    Match match = Variorum.getMatch();
    if (match == null) {
      sender.sendMessage(Component.text("No match is currently running!", NamedTextColor.RED));
      return;
    }

    int seconds = parseTimeString(timeStr);
    if (seconds < 0) {
      sender.sendMessage(
          Component.text("Invalid time format! Use: 30s, 1m, 1h", NamedTextColor.RED));
      return;
    }

    GameStateModule stateModule = match.getRequiredModule(GameStateModule.class);

    switch (stateModule.getCurrentState()) {
      case WAITING:
        stateModule.startCountdown(seconds);
        sender.sendMessage(Component.text(
            "Starting match countdown with " + seconds + " seconds", NamedTextColor.GREEN));
        break;
      case COUNTDOWN:
        stateModule.startCountdown(seconds);
        sender.sendMessage(Component.text(
            "Restarting countdown with " + seconds + " seconds", NamedTextColor.GREEN));
        break;
      case PLAYING:
        sender.sendMessage(Component.text("Match is already in progress!", NamedTextColor.RED));
        break;
      case ENDED:
        sender.sendMessage(Component.text("Match has already ended!", NamedTextColor.RED));
        break;
    }
  }

  @Command("end")
  @CommandDescription("Force end the current match")
  @Permission("variorum.command.end")
  public void endMatch(final CommandSender sender) {
    Match match = Variorum.getMatch();
    if (match == null) {
      sender.sendMessage(Component.text("No match is currently running!", NamedTextColor.RED));
      return;
    }

    GameStateModule stateModule = match.getRequiredModule(GameStateModule.class);

    switch (stateModule.getCurrentState()) {
      case WAITING:
      case COUNTDOWN:
        sender.sendMessage(Component.text("Match hasn't started yet!", NamedTextColor.RED));
        break;
      case PLAYING:
        stateModule.setState(GameState.ENDED);
        sender.sendMessage(Component.text("Ending match...", NamedTextColor.GREEN));
        break;
      case ENDED:
        sender.sendMessage(Component.text("Match has already ended!", NamedTextColor.RED));
        break;
    }
  }

  @Command("state")
  @CommandDescription("Display the current game state")
  @Permission("variorum.command.state")
  public void showState(final CommandSender sender) {
    Match match = Variorum.getMatch();
    if (match == null) {
      sender.sendMessage(Component.text("No match is currently running!", NamedTextColor.RED));
      return;
    }

    GameStateModule stateModule = match.getRequiredModule(GameStateModule.class);
    GameState currentState = stateModule.getCurrentState();

    sender.sendMessage(Component.text("Current game state: ", NamedTextColor.YELLOW)
        .append(Component.text(currentState.name(), NamedTextColor.GREEN)));

    switch (currentState) {
      case WAITING:
        int playerCount = match.getWorld().getPlayers().size();
        sender.sendMessage(Component.text("Players: ", NamedTextColor.YELLOW)
            .append(Component.text(playerCount, NamedTextColor.WHITE)));
        break;

      case COUNTDOWN:
        // If we had access to the countdown object, we could show remaining time
        break;

      case PLAYING:
        // Could show match duration if we tracked it
        break;

      case ENDED:
        // Could show time until map cycle
        break;
    }
  }

  private int parseTimeString(String timeStr) {
    Pattern pattern = Pattern.compile("^(\\d+)(s|m|h)$");
    Matcher matcher = pattern.matcher(timeStr.toLowerCase());

    if (!matcher.matches()) {
      return -1;
    }

    int value = Integer.parseInt(matcher.group(1));
    String unit = matcher.group(2);

    return switch (unit) {
      case "s" -> value;
      case "m" -> value * 60;
      case "h" -> value * 3600;
      default -> -1;
    };
  }
}
