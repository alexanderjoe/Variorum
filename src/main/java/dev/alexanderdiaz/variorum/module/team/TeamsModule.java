package dev.alexanderdiaz.variorum.module.team;

import dev.alexanderdiaz.variorum.Variorum;
import dev.alexanderdiaz.variorum.event.team.PlayerChangeTeamEvent;
import dev.alexanderdiaz.variorum.event.team.PlayerTeamScoreboardEvent;
import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.Module;
import dev.alexanderdiaz.variorum.util.Events;
import java.util.*;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team.OptionStatus;

public class TeamsModule implements Module {
    @Getter
    private final Match match;

    @Getter
    private final List<Team> teams;

    private final Map<UUID, Team> playerTeams = new HashMap<>();
    private final List<Listener> listeners = new ArrayList<>();
    private final Scoreboard scoreboard;

    public TeamsModule(Match match, List<Team> teams) {
        this.match = match;
        this.teams = Collections.unmodifiableList(new ArrayList<>(teams));
        this.scoreboard = Variorum.get().getServer().getScoreboardManager().getNewScoreboard();
    }

    @Override
    public void enable() {
        listeners.add(new TeamListener(this));
        listeners.add(new SpectatorListener(this));
        listeners.forEach(Events::register);
    }

    @Override
    public void disable() {
        listeners.forEach(Events::unregister);
        playerTeams.clear();
        teams.forEach(team -> {
            org.bukkit.scoreboard.Team scoreboardTeam = scoreboard.getTeam(team.id());
            if (scoreboardTeam != null) {
                scoreboardTeam.unregister();
            }
        });
    }

    public Optional<Team> getPlayerTeam(Player player) {
        return Optional.ofNullable(playerTeams.get(player.getUniqueId()));
    }

    public Optional<Team> getTeamById(String id) {
        return teams.stream().filter(team -> team.id().equals(id)).findFirst();
    }

    public boolean isSpectator(Player player) {
        return getPlayerTeam(player).isEmpty();
    }

    public void setPlayerTeam(Player player, Team team) {
        Team oldTeam = playerTeams.get(player.getUniqueId());

        if (oldTeam != null && oldTeam.equals(team)) {
            return;
        }

        var event = new PlayerChangeTeamEvent(player, oldTeam, team);

        if (oldTeam != null) {
            org.bukkit.scoreboard.Team scoreboardTeam = scoreboard.getTeam(oldTeam.id());
            if (scoreboardTeam != null) {
                scoreboardTeam.removeEntry(player.getName());
            }
        }

        playerTeams.put(player.getUniqueId(), team);

        org.bukkit.scoreboard.Team scoreboardTeam = scoreboard.getTeam(team.id());
        if (scoreboardTeam == null) {
            scoreboardTeam = scoreboard.registerNewTeam(team.id());
            scoreboardTeam.color(team.textColor());
            scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY, OptionStatus.ALWAYS);
            scoreboardTeam.prefix(Component.text("[" + team.name() + "] "));
        }
        scoreboardTeam.addEntry(player.getName());

        Events.call(event);
        Events.call(new PlayerTeamScoreboardEvent(player, scoreboard));
    }

    public void removePlayerFromTeam(Player player) {
        Team oldTeam = playerTeams.get(player.getUniqueId());
        if (oldTeam != null) {
            var event = new PlayerChangeTeamEvent(player, oldTeam, null);
            Events.call(event);
            if (event.isCancelled()) {
                return;
            }

            org.bukkit.scoreboard.Team scoreboardTeam = scoreboard.getTeam(oldTeam.id());
            if (scoreboardTeam != null) {
                scoreboardTeam.removeEntry(player.getName());
            }
            playerTeams.remove(player.getUniqueId());
        }
    }

    public boolean canJoinTeam(Team team) {
        long teamSize =
                playerTeams.values().stream().filter(t -> t.equals(team)).count();

        long minTeamSize = teams.stream()
                .mapToLong(t ->
                        playerTeams.values().stream().filter(pt -> pt.equals(t)).count())
                .min()
                .orElse(0);

        // Allow joining if this would not make the team more than 1 player larger than the smallest team
        return teamSize <= minTeamSize + 1;
    }

    public Team autoAssignTeam(Player player) {
        return teams.stream()
                .min(Comparator.comparingLong(team -> playerTeams.values().stream()
                        .filter(t -> t.equals(team))
                        .count()))
                .orElse(null);
    }
}
