package dev.alexanderdiaz.variorum.module.results;

import dev.alexanderdiaz.variorum.event.match.MatchCompleteEvent;
import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.Module;
import dev.alexanderdiaz.variorum.module.objectives.ObjectivesModule;
import dev.alexanderdiaz.variorum.module.objectives.monument.MonumentDestroyedEvent;
import dev.alexanderdiaz.variorum.module.objectives.wool.WoolPlaceEvent;
import dev.alexanderdiaz.variorum.module.state.GameState;
import dev.alexanderdiaz.variorum.module.state.GameStateChangeEvent;
import dev.alexanderdiaz.variorum.module.state.GameStateModule;
import dev.alexanderdiaz.variorum.module.team.Team;
import dev.alexanderdiaz.variorum.module.team.TeamsModule;
import dev.alexanderdiaz.variorum.util.Events;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ResultsModule implements Module {
  private final Match match;
  private ResultListener listener;

  @Getter
  private final Map<Team, TeamResult> teamResults;

  private boolean resultDeclared = false;

  public ResultsModule(Match match) {
    this.match = match;
    this.teamResults = new HashMap<>();

    // Initialize results for each team
    TeamsModule teamsModule = match.getRequiredModule(TeamsModule.class);
    teamsModule.getTeams().forEach(team -> teamResults.put(team, new TeamResult()));
  }

  @Override
  public void enable() {
    this.listener = new ResultListener();
    Events.register(listener);
  }

  @Override
  public void disable() {
    if (listener != null) {
      Events.unregister(listener);
    }
    teamResults.clear();
    resultDeclared = false;
  }

  private void checkResult() {
    if (resultDeclared) return;

    TeamsModule teamsModule = match.getRequiredModule(TeamsModule.class);
    ObjectivesModule objectivesModule = match.getRequiredModule(ObjectivesModule.class);

    // Get total number of objectives each team needs to complete
    Map<Team, Integer> requiredObjectives = new HashMap<>();
    teamsModule.getTeams().forEach(team -> {
      long count = objectivesModule.getObjectives().stream()
          .filter(obj -> obj.canComplete(team))
          .count();
      requiredObjectives.put(team, (int) count);
    });

    // Check if any team has completed ALL their required objectives
    for (Team team : teamsModule.getTeams()) {
      int completed = teamResults.get(team).getObjectivesCompleted();
      int required = requiredObjectives.get(team);

      if (completed >= required) {
        declareResult(MatchResult.createWin(team, teamResults));
        return;
      }
    }
  }

  private void checkFinalScore() {
    if (resultDeclared) return;

    TeamsModule teamsModule = match.getRequiredModule(TeamsModule.class);
    List<Team> winningTeams = new ArrayList<>();
    int highestScore = -1;

    // Find highest score
    for (Map.Entry<Team, TeamResult> entry : teamResults.entrySet()) {
      int score = entry.getValue().getObjectivesCompleted();
      if (score > highestScore) {
        winningTeams.clear();
        winningTeams.add(entry.getKey());
        highestScore = score;
      } else if (score == highestScore) {
        winningTeams.add(entry.getKey());
      }
    }

    // Declare result based on winning teams
    if (winningTeams.isEmpty() || winningTeams.size() > 1) {
      declareResult(MatchResult.createTie(teamResults));
    } else {
      declareResult(MatchResult.createWin(winningTeams.get(0), teamResults));
    }
  }

  private void declareResult(MatchResult result) {
    if (resultDeclared) return;
    resultDeclared = true;

    GameStateModule stateModule = match.getRequiredModule(GameStateModule.class);

    if (stateModule.getCurrentState() == GameState.PLAYING) {
      match.getWorld().getPlayers().forEach(player -> {
        // Main result message
        if (result.getWinningTeam().isPresent()) {
          Team winner = result.getWinningTeam().get();
          player.sendMessage(Component.text()
              .append(Component.text("Team ", NamedTextColor.GRAY))
              .append(Component.text(winner.name(), winner.textColor()))
              .append(Component.text(" has won the match!", NamedTextColor.GRAY))
              .build());
        } else {
          player.sendMessage(Component.text("The match has ended in a tie!", NamedTextColor.GRAY));
        }

        // Score breakdown
        player.sendMessage(Component.text("Final Results:", NamedTextColor.GRAY));
        result.getTeamResults().forEach((team, teamResult) -> {
          player.sendMessage(Component.text()
              .append(Component.text(team.name(), team.textColor()))
              .append(Component.text(": ", NamedTextColor.GRAY))
              .append(Component.text(teamResult.getObjectivesCompleted(), NamedTextColor.WHITE))
              .append(Component.text(" objectives completed", NamedTextColor.GRAY))
              .build());
        });
      });

      MatchCompleteEvent mce = new MatchCompleteEvent(match, List.of(), List.of());
      Events.call(mce);
      stateModule.setState(GameState.ENDED);
    }
  }

  private class ResultListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMonumentDestroyed(MonumentDestroyedEvent event) {
      if (resultDeclared) return;

      TeamsModule teamsModule = match.getRequiredModule(TeamsModule.class);
      teamsModule.getTeams().stream()
          .filter(team -> team.equals(event.getMonument().getOwner()))
          .forEach(team -> {
            teamResults.get(team).incrementObjectives();
          });

      checkResult();
    }

    @EventHandler
    public void onWoolPlaced(WoolPlaceEvent event) {
      if (resultDeclared) return;

      TeamsModule teamsModule = match.getRequiredModule(TeamsModule.class);
      teamsModule.getTeams().stream()
          .filter(team -> team.equals(event.getObjective().getTeam().orElse(null)))
          .forEach(team -> {
            teamResults.get(team).incrementObjectives();
          });

      checkResult();
    }

    @EventHandler
    public void onGameStateChange(GameStateChangeEvent event) {
      if (event.getNewState() == GameState.ENDED && event.getOldState() == GameState.PLAYING) {
        checkFinalScore();
      }
    }
  }
}
