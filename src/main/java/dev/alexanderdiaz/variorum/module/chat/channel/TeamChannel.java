package dev.alexanderdiaz.variorum.module.chat.channel;

import dev.alexanderdiaz.variorum.Variorum;
import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.team.Team;
import dev.alexanderdiaz.variorum.module.team.TeamsModule;
import java.util.Collection;
import java.util.Collections;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public record TeamChannel(Team team) implements ChatChannel {

  @Override
  public String getName() {
    return "Team " + team.name();
  }

  @Override
  public void sendMessage(Player sender, String message) {
    Component formatted = Component.text()
        .append(Component.text("[Team] ", team.textColor()))
        .append(sender.displayName())
        .append(Component.text(": "))
        .append(Component.text(message))
        .build();

    getRecipients(sender).forEach(player -> player.sendMessage(formatted));
    Variorum.get().getServer().getConsoleSender().sendMessage(formatted);
  }

  @Override
  public Collection<? extends Player> getRecipients(Player sender) {
    Match match = Variorum.getMatch();
    if (match == null) return Collections.emptyList();

    return match
        .getModule(TeamsModule.class)
        .map(teamsModule -> match.getWorld().getPlayers().stream()
            .filter(player -> teamsModule
                .getPlayerTeam(player)
                .map(playerTeam -> playerTeam.equals(team))
                .orElse(false))
            .toList())
        .orElse(Collections.emptyList());
  }
}
