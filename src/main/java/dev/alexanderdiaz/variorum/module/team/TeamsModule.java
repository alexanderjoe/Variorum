package dev.alexanderdiaz.variorum.module.team;

import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.Module;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.*;

public class TeamsModule implements Module {
    private final Match match;
    @Getter
    private final List<Team> teams;

    private final Map<UUID, Team> playerTeams = new HashMap<>();

    public Optional<Team> getPlayerTeam(Player player) {
        return Optional.ofNullable(playerTeams.get(player.getUniqueId()));
    }

    public void setPlayerTeam(Player player, Team team) {
        playerTeams.put(player.getUniqueId(), team);
    }

    public TeamsModule(Match match, List<Team> teams) {
        this.match = match;
        this.teams = Collections.unmodifiableList(new ArrayList<>(teams));
    }

    @Override
    public void enable() {
        // Initialize team-related listeners and mechanics
    }

    @Override
    public void disable() {
        // Cleanup team-related resources
        playerTeams.clear();
    }

    public static class Team {
        @Getter
        private final String id;
        @Getter
        private final String name;
        @Getter
        private final String color;

        public Team(String id, String name, String color) {
            this.id = id;
            this.name = name;
            this.color = color;
        }
    }
}