package dev.alexanderdiaz.variorum.module.scoreboard;

import dev.alexanderdiaz.variorum.event.team.PlayerTeamScoreboardEvent;
import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.Module;
import dev.alexanderdiaz.variorum.module.objectives.monument.MonumentDestroyedEvent;
import dev.alexanderdiaz.variorum.module.objectives.monument.MonumentObjective;
import dev.alexanderdiaz.variorum.module.objectives.ObjectivesModule;
import dev.alexanderdiaz.variorum.module.team.Team;
import dev.alexanderdiaz.variorum.module.team.TeamsModule;
import dev.alexanderdiaz.variorum.util.Events;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.*;

import java.util.*;

public class ScoreboardModule implements Module {
    private final Match match;
    private final org.bukkit.scoreboard.Scoreboard scoreboard;
    private final Objective objective;
    private ScoreboardListener listener;
    private int taskId = -1;

    // Unicode symbols for objective status
    private static final String CHECK_MARK = "✔";
    private static final String X_MARK = "✘";

    public ScoreboardModule(Match match) {
        this.match = match;
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        this.scoreboard = manager.getNewScoreboard();
        this.objective = scoreboard.registerNewObjective(
                "objectives",
                Criteria.DUMMY,
                Component.text("Objectives")
                        .color(NamedTextColor.GOLD)
        );
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    @Override
    public void enable() {
        this.listener = new ScoreboardListener();
        Events.register(listener);
        updateScoreboard();

        // Set scoreboard for all online players
        match.getWorld().getPlayers().forEach(this::setPlayerScoreboard);

        // Start tick update task
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                dev.alexanderdiaz.variorum.Variorum.get(),
                this::updateScoreboard,
                0L, // Start immediately
                3L  // Run every tick
        );
    }

    @Override
    public void disable() {
        if (listener != null) {
            Events.unregister(listener);
        }
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
        scoreboard.getTeams().forEach(org.bukkit.scoreboard.Team::unregister);
        scoreboard.getObjectives().forEach(Objective::unregister);
    }

    private void setPlayerScoreboard(Player player) {
        player.setScoreboard(scoreboard);
    }

    private void updateScoreboard() {
        TeamsModule teamsModule = match.getRequiredModule(TeamsModule.class);
        ObjectivesModule objectivesModule = match.getRequiredModule(ObjectivesModule.class);

        // Clear existing scores
        scoreboard.getEntries().forEach(scoreboard::resetScores);

        int score = 15; // Start from top

        // Add a blank line at the top
        objective.getScore(" ").setScore(score--);

        // For each team
        for (Team team : teamsModule.getTeams()) {
            // Add team name
            String teamDisplay = team.name();
            objective.getScore(teamDisplay).setScore(score--);

            // Get monuments for this team
            List<MonumentObjective> teamMonuments = objectivesModule.getObjectives().stream()
                    .filter(obj -> obj instanceof MonumentObjective)
                    .map(obj -> (MonumentObjective) obj)
                    .filter(monument -> monument.getOwner().equals(team))
                    .toList();

            // Add monument status
            for (MonumentObjective monument : teamMonuments) {
                String status = monument.isCompleted() ? CHECK_MARK : X_MARK;
                String display = "  " + monument.getName() + " " + status;
                objective.getScore(display).setScore(score--);
            }

            // Add blank line between teams
            objective.getScore("  ").setScore(score--);
        }
    }

    private class ScoreboardListener implements Listener {
        @EventHandler
        public void onMonumentDestroyed(MonumentDestroyedEvent event) {
            updateScoreboard();
        }

        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) {
            setPlayerScoreboard(event.getPlayer());
        }

        @EventHandler
        public void onPlayerTeamScoreboard(PlayerTeamScoreboardEvent event) {
            // Copy team information from the team scoreboard to our scoreboard
            org.bukkit.scoreboard.Team teamScoreboardTeam = event.getTeamScoreboard().getEntryTeam(event.getPlayer().getName());
            if (teamScoreboardTeam != null) {
                org.bukkit.scoreboard.Team ourTeam = scoreboard.getTeam(teamScoreboardTeam.getName());
                if (ourTeam == null) {
                    ourTeam = scoreboard.registerNewTeam(teamScoreboardTeam.getName());
//                    ourTeam.color(teamScoreboardTeam.color());
                    ourTeam.prefix(teamScoreboardTeam.prefix());
                    ourTeam.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY,
                            teamScoreboardTeam.getOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY));
                }
                ourTeam.addEntry(event.getPlayer().getName());
            }

            // Make sure they see our scoreboard
            setPlayerScoreboard(event.getPlayer());
            updateScoreboard();
        }
    }
}