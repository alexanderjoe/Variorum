package dev.alexanderdiaz.variorum.module.scoreboard;

import dev.alexanderdiaz.variorum.Variorum;
import dev.alexanderdiaz.variorum.event.match.MatchLoadEvent;
import dev.alexanderdiaz.variorum.event.match.MatchOpenEvent;
import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.Module;
import dev.alexanderdiaz.variorum.module.objectives.ObjectivesModule;
import dev.alexanderdiaz.variorum.module.objectives.monument.MonumentObjective;
import dev.alexanderdiaz.variorum.module.state.GameStateChangeEvent;
import dev.alexanderdiaz.variorum.module.team.Team;
import dev.alexanderdiaz.variorum.module.team.TeamsModule;
import dev.alexanderdiaz.variorum.util.Events;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.megavex.scoreboardlibrary.api.ScoreboardLibrary;
import net.megavex.scoreboardlibrary.api.sidebar.Sidebar;
import net.megavex.scoreboardlibrary.api.sidebar.component.ComponentSidebarLayout;
import net.megavex.scoreboardlibrary.api.sidebar.component.SidebarComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

@RequiredArgsConstructor
public class ScoreboardModule implements Module {
    private final Match match;
    private Sidebar sidebar;
    private ComponentSidebarLayout layout;
    private ScoreboardListener listener;

    @Override
    public void enable() {
        ScoreboardLibrary scoreboardLibrary = Variorum.get().getScoreboardLibrary();
        this.sidebar = scoreboardLibrary.createSidebar();

        // Create the initial layout
        updateLayout();

        // Register listener
        this.listener = new ScoreboardListener();
        Events.register(listener);

        // Add all current players
        match.getWorld().getPlayers().forEach(this::addPlayer);
    }

    @Override
    public void disable() {
        if (listener != null) {
            Events.unregister(listener);
            listener = null;
        }
        if (sidebar != null) {
            match.getWorld().getPlayers().forEach(this::removePlayer);
            sidebar.close();
            sidebar = null;
        }
        if (layout != null) {
            layout = null;
        }
    }

    private void addPlayer(Player player) {
        if (sidebar != null && match.isActive()) {
            sidebar.addPlayer(player);
        }
    }

    private void removePlayer(Player player) {
        if (sidebar != null) {
            sidebar.removePlayer(player);
        }
    }

    private void updateLayout() {
        TeamsModule teamsModule = match.getRequiredModule(TeamsModule.class);
        ObjectivesModule objectivesModule = match.getRequiredModule(ObjectivesModule.class);

        // Create team-based content
        var contentBuilder = SidebarComponent.builder();

        for (Team team : teamsModule.getTeams()) {
            // Add team name
            contentBuilder.addDynamicLine(() -> Component.text()
                    .append(Component.text(team.name()))
                    .color(team.textColor())
                    .build()
            );

            List<MonumentObjective> monumentObjectives = objectivesModule.getObjectives().stream()
                    .filter(obj -> obj instanceof MonumentObjective)
                    .map(obj -> (MonumentObjective) obj)
                    .filter(mon -> !mon.getOwner().equals(team))
                    .toList();

            for (MonumentObjective monument : monumentObjectives) {
                contentBuilder.addDynamicLine(() -> {
                    Component status = monument.isCompleted() ?
                            Component.text("✓", NamedTextColor.GREEN).decorate(TextDecoration.BOLD) :
                            Component.text("✗", NamedTextColor.RED).decorate(TextDecoration.BOLD);

                    return Component.text()
                            .append(Component.text("  " + monument.getName() + " - ", NamedTextColor.WHITE))
                            .append(status)
                            .build();
                });
            }

            // Add blank line between teams
            contentBuilder.addBlankLine();
        }

        contentBuilder.addDynamicLine(() -> Component.text()
                .append(Component.text("Map: ", NamedTextColor.GOLD))
                .append(Component.text(match.getMap().getName(), NamedTextColor.WHITE).decorate(TextDecoration.BOLD))
                .build());

        SidebarComponent content = contentBuilder.build();
        this.layout = new ComponentSidebarLayout(
                SidebarComponent.staticLine(Component.text("Monuments")),
                content
        );
        this.layout.apply(sidebar);
    }

    private class ScoreboardListener implements Listener {
        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) {
            addPlayer(event.getPlayer());
        }

        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event) {
            removePlayer(event.getPlayer());
        }

        @EventHandler
        public void onGameStateChange(MatchLoadEvent event) {
            updateLayout();
        }

        @EventHandler
        public void onGameStateChange(GameStateChangeEvent event) {
            updateLayout();
        }

        @EventHandler
        public void onMonumentDestroyed(dev.alexanderdiaz.variorum.module.objectives.monument.MonumentDestroyedEvent event) {
            updateLayout();
        }
    }
}