package dev.alexanderdiaz.variorum.module.team;

import dev.alexanderdiaz.variorum.Variorum;
import dev.alexanderdiaz.variorum.event.team.PlayerChangeTeamEvent;
import dev.alexanderdiaz.variorum.event.team.PlayerTeamScoreboardEvent;
import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.Module;
import dev.alexanderdiaz.variorum.util.Events;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team.OptionStatus;

import java.util.*;

public class TeamsModule implements Module {
    private final Match match;
    @Getter
    private final List<Team> teams;
    private final Map<UUID, Team> playerTeams = new HashMap<>();
    private TeamListener listener;
    private Scoreboard scoreboard;

    public Optional<Team> getPlayerTeam(Player player) {
        return Optional.ofNullable(playerTeams.get(player.getUniqueId()));
    }

    public Optional<Team> getTeamById(String id) {
        return teams.stream()
                .filter(team -> team.id().equals(id))
                .findFirst();
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
        long teamSize = playerTeams.values().stream()
                .filter(t -> t.equals(team))
                .count();

        long minTeamSize = teams.stream()
                .mapToLong(t -> playerTeams.values().stream()
                        .filter(pt -> pt.equals(t))
                        .count())
                .min()
                .orElse(0);

        // Allow joining if this would not make the team more than 1 player larger than the smallest team
        return teamSize <= minTeamSize + 1;
    }

    public Team autoAssignTeam(Player player) {
        return teams.stream()
                .min(Comparator.comparingLong(team ->
                        playerTeams.values().stream()
                                .filter(t -> t.equals(team))
                                .count()))
                .orElse(null);
    }

    public TeamsModule(Match match, List<Team> teams) {
        this.match = match;
        this.teams = Collections.unmodifiableList(new ArrayList<>(teams));
        this.scoreboard = Variorum.get().getServer().getScoreboardManager().getNewScoreboard();
    }

    @Override
    public void enable() {
        this.listener = new TeamListener();
        Events.register(listener);
    }

    @Override
    public void disable() {
        if (listener != null) {
            Events.unregister(listener);
        }
        playerTeams.clear();
        teams.forEach(team -> {
            org.bukkit.scoreboard.Team scoreboardTeam = scoreboard.getTeam(team.id());
            if (scoreboardTeam != null) {
                scoreboardTeam.unregister();
            }
        });
    }

    private class TeamListener implements Listener {
        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
            if (!(event.getEntity() instanceof Player victim) ||
                    !(event.getDamager() instanceof Player attacker)) {
                return;
            }

            Optional<Team> victimTeam = getPlayerTeam(victim);
            Optional<Team> attackerTeam = getPlayerTeam(attacker);

            if (victimTeam.isPresent() && attackerTeam.isPresent() &&
                    victimTeam.get().equals(attackerTeam.get())) {
                event.setCancelled(true);
            }
        }

        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event) {
            removePlayerFromTeam(event.getPlayer());
        }
    }
}
