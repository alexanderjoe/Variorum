package dev.alexanderdiaz.variorum.command;

import dev.alexanderdiaz.variorum.Variorum;
import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.chat.ChatModule;
import dev.alexanderdiaz.variorum.module.team.TeamsModule;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;

public class ChatCommands {
  private final Variorum plugin;

  public ChatCommands(Variorum plugin) {
    this.plugin = plugin;
  }

  @Command("chat channel <channel>")
  @CommandDescription("Switch to a specific chat channel")
  public void switchChannel(
      final Player player,
      final @Argument(value = "channel", suggestions = "channelSuggestions") String channelName) {
    Match match = Variorum.getMatch();
    if (match == null) {
      player.sendMessage(Component.text("No match is currently running!", NamedTextColor.RED));
      return;
    }

    match.getModule(ChatModule.class).ifPresent(chatModule -> {
      chatModule.getChannels().stream()
          .filter(channel -> channel.getName().equalsIgnoreCase(channelName))
          .findFirst()
          .ifPresentOrElse(
              channel -> chatModule.setPlayerChannel(player, channel),
              () -> player.sendMessage(Component.text("Channel not found!", NamedTextColor.RED)));
    });
  }

  @Command("chat global")
  @CommandDescription("Switch to global chat")
  public void globalChat(final Player player) {
    Match match = Variorum.getMatch();
    if (match == null) {
      player.sendMessage(Component.text("No match is currently running!", NamedTextColor.RED));
      return;
    }

    match.getModule(ChatModule.class).ifPresent(chatModule -> {
      chatModule.getChannels().stream()
          .filter(channel -> channel.getName().equalsIgnoreCase("Global"))
          .findFirst()
          .ifPresent(channel -> chatModule.setPlayerChannel(player, channel));
    });
  }

  @Command("chat team")
  @CommandDescription("Switch to team chat")
  public void teamChat(final Player player) {
    Match match = Variorum.getMatch();
    if (match == null) {
      player.sendMessage(Component.text("No match is currently running!", NamedTextColor.RED));
      return;
    }

    match.getModule(TeamsModule.class).ifPresent(teamsModule -> {
      teamsModule
          .getPlayerTeam(player)
          .ifPresentOrElse(
              team -> match.getModule(ChatModule.class).ifPresent(chatModule -> {
                chatModule.getChannels().stream()
                    .filter(channel -> channel.getName().equalsIgnoreCase("Team " + team.name()))
                    .findFirst()
                    .ifPresent(channel -> chatModule.setPlayerChannel(player, channel));
              }),
              () ->
                  player.sendMessage(Component.text("You are not in a team!", NamedTextColor.RED)));
    });
  }

  @Command("chat info")
  @CommandDescription("Display information about your current chat channel")
  public void chatInfo(final Player player) {
    Match match = Variorum.getMatch();
    if (match == null) {
      player.sendMessage(Component.text("No match is currently running!", NamedTextColor.RED));
      return;
    }

    match.getModule(ChatModule.class).ifPresent(chatModule -> {
      chatModule
          .getPlayerChannel(player)
          .ifPresentOrElse(
              channel -> {
                player.sendMessage(
                    Component.text("Current channel: " + channel.getName(), NamedTextColor.YELLOW));
                player.sendMessage(Component.text("Available channels:", NamedTextColor.YELLOW));
                chatModule
                    .getChannels()
                    .forEach(ch -> player.sendMessage(
                        Component.text("- " + ch.getName(), NamedTextColor.GRAY)));
              },
              () -> player.sendMessage(
                  Component.text("You are not in any channel!", NamedTextColor.RED)));
    });
  }

  @Suggestions("channelSuggestions")
  public List<String> suggestChannels(CommandContext<Player> context, String input) {
    Match match = Variorum.getMatch();
    if (match == null) {
      return new ArrayList<>();
    }

    return match
        .getModule(ChatModule.class)
        .map(chatModule -> chatModule.getChannels().stream()
            .map(channel -> channel.getName())
            .filter(name -> name.toLowerCase().startsWith(input.toLowerCase()))
            .toList())
        .orElse(new ArrayList<>());
  }
}
