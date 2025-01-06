package dev.alexanderdiaz.variorum.command;

import dev.alexanderdiaz.variorum.Variorum;
import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.state.GameState;
import dev.alexanderdiaz.variorum.module.state.GameStateModule;
import dev.alexanderdiaz.variorum.module.team.Team;
import dev.alexanderdiaz.variorum.module.team.TeamsModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;

import java.util.List;

public class TeamCommands {
    private final Variorum plugin;

    public TeamCommands(Variorum plugin) {
        this.plugin = plugin;
    }

    @Command("join <team>")
    @CommandDescription("Join a specific team")
    public void joinTeam(
            final Player player,
            final @Argument(value = "team", suggestions = "availableTeams") String teamId
    ) {
        Match match = Variorum.getMatch();
        if (match == null) {
            player.sendMessage(Component.text("No match is currently running!", NamedTextColor.RED));
            return;
        }

        // Check match state
        GameStateModule stateModule = match.getRequiredModule(GameStateModule.class);
        if (stateModule.getCurrentState() == GameState.ENDED) {
            player.sendMessage(Component.text("Cannot join teams while the match is cycling!", NamedTextColor.RED));
            return;
        }

        TeamsModule teamsModule = match.getRequiredModule(TeamsModule.class);
        Team from = teamsModule.getPlayerTeam(player).orElse(null);
        Team to = teamsModule.getTeamById(teamId).orElse(null);

        if (to == null) {
            player.sendMessage(Component.text("That team doesn't exist!", NamedTextColor.RED));
            return;
        }

        // Check if player is already on this team
        if (from != null && from.equals(to)) {
            player.sendMessage(Component.text("You are already on that team!", NamedTextColor.RED));
            return;
        }

        // Check if teams would become unbalanced
        if (!teamsModule.canJoinTeam(to)) {
            player.sendMessage(Component.text("That team is full!", NamedTextColor.RED));
            return;
        }

        teamsModule.setPlayerTeam(player, to);
    }

    @Command("auto")
    @CommandDescription("Join a balanced team automatically")
    public void autoJoin(final Player player) {
        Match match = Variorum.getMatch();
        if (match == null) {
            player.sendMessage(Component.text("No match is currently running!", NamedTextColor.RED));
            return;
        }

        // Check match state
        GameStateModule stateModule = match.getRequiredModule(GameStateModule.class);
        if (stateModule.getCurrentState() == GameState.ENDED) {
            player.sendMessage(Component.text("Cannot join teams while the match is cycling!", NamedTextColor.RED));
            return;
        }

        TeamsModule teamsModule = match.getRequiredModule(TeamsModule.class);
        Team autoTeam = teamsModule.autoAssignTeam(player);

        if (autoTeam == null) {
            player.sendMessage(Component.text("Failed to find a team to join!", NamedTextColor.RED));
            return;
        }

        // Check if player is already on this team
        Team currentTeam = teamsModule.getPlayerTeam(player).orElse(null);
        if (currentTeam != null && currentTeam.equals(autoTeam)) {
            player.sendMessage(Component.text("You are already on that team!", NamedTextColor.RED));
            return;
        }

        teamsModule.setPlayerTeam(player, autoTeam);
    }

    @Command("leave")
    @CommandDescription("Leave your current team")
    public void leaveTeam(final Player player) {
        Match match = Variorum.getMatch();
        if (match == null) {
            player.sendMessage(Component.text("No match is currently running!", NamedTextColor.RED));
            return;
        }

        // Check match state
        GameStateModule stateModule = match.getRequiredModule(GameStateModule.class);
        if (GameState.ENDED.equals(stateModule.getCurrentState())) {
            player.sendMessage(Component.text("Cannot leave teams while the match is cycling!", NamedTextColor.RED));
            return;
        }

        TeamsModule teamsModule = match.getRequiredModule(TeamsModule.class);
        teamsModule.removePlayerFromTeam(player);
        player.sendMessage(Component.text("You left your team", NamedTextColor.YELLOW));
    }

    @Suggestions("availableTeams")
    public List<String> suggestTeams(CommandContext<Player> context, String input) {
        Match match = Variorum.getMatch();
        if (match == null) {
            return List.of();
        }

        TeamsModule teamsModule = match.getRequiredModule(TeamsModule.class);
        return teamsModule.getTeams().stream()
                .map(Team::id)
                .filter(id -> id.toLowerCase().startsWith(input.toLowerCase()))
                .toList();
    }
}