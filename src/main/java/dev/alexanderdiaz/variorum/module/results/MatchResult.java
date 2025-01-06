package dev.alexanderdiaz.variorum.module.results;

import dev.alexanderdiaz.variorum.module.team.Team;
import lombok.Getter;

import java.util.Map;
import java.util.Optional;

@Getter
public class MatchResult {
    private final Optional<Team> winningTeam;
    private final Map<Team, TeamResult> teamResults;

    private MatchResult(Optional<Team> winningTeam, Map<Team, TeamResult> teamResults) {
        this.winningTeam = winningTeam;
        this.teamResults = Map.copyOf(teamResults);
    }

    public static MatchResult createWin(Team winner, Map<Team, TeamResult> results) {
        return new MatchResult(Optional.of(winner), results);
    }

    public static MatchResult createTie(Map<Team, TeamResult> results) {
        return new MatchResult(Optional.empty(), results);
    }
}