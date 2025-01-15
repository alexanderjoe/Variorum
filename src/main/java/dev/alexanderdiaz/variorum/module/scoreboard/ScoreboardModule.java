package dev.alexanderdiaz.variorum.module.scoreboard;

import dev.alexanderdiaz.variorum.Variorum;
import dev.alexanderdiaz.variorum.event.match.MatchOpenEvent;
import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.Module;
import dev.alexanderdiaz.variorum.module.objectives.ObjectivesModule;
import dev.alexanderdiaz.variorum.module.objectives.monument.MonumentDestroyedEvent;
import dev.alexanderdiaz.variorum.module.objectives.monument.MonumentObjective;
import dev.alexanderdiaz.variorum.module.objectives.wool.WoolObjective;
import dev.alexanderdiaz.variorum.module.objectives.wool.WoolPlaceEvent;
import dev.alexanderdiaz.variorum.module.state.GameStateChangeEvent;
import dev.alexanderdiaz.variorum.module.team.Team;
import dev.alexanderdiaz.variorum.module.team.TeamsModule;
import dev.alexanderdiaz.variorum.util.Events;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.megavex.scoreboardlibrary.api.ScoreboardLibrary;
import net.megavex.scoreboardlibrary.api.sidebar.Sidebar;
import net.megavex.scoreboardlibrary.api.sidebar.component.ComponentSidebarLayout;
import net.megavex.scoreboardlibrary.api.sidebar.component.SidebarComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ScoreboardModule implements Module {
    private final Match match;

    @Getter
    private Sidebar sidebar;

    private ComponentSidebarLayout layout;
    private ScoreboardListener listener;

    public ScoreboardModule(Match match) {
        this.match = match;
    }

    @Override
    public void enable() {
        ScoreboardLibrary scoreboardLibrary = Variorum.get().getScoreboardLibrary();
        this.sidebar = scoreboardLibrary.createSidebar();

        updateLayout();

        this.listener = new ScoreboardListener();
        Events.register(listener);

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

        var contentBuilder = SidebarComponent.builder();

        for (Team team : teamsModule.getTeams()) {
            contentBuilder.addDynamicLine(() -> Component.text()
                    .append(Component.text(team.name()))
                    .color(team.textColor())
                    .build());

            List<MonumentObjective> monumentObjectives = objectivesModule.getObjectives().stream()
                    .filter(obj -> obj instanceof MonumentObjective)
                    .map(obj -> (MonumentObjective) obj)
                    .filter(mon -> mon.getOwner().equals(team))
                    .toList();

            if (!monumentObjectives.isEmpty()) {
                monumentObjectives.forEach(monument -> {
                    contentBuilder.addDynamicLine(() -> {
                        Component status = monument.isCompleted()
                                ? Component.text("✓", NamedTextColor.GREEN).decorate(TextDecoration.BOLD)
                                : Component.text("✗", NamedTextColor.RED).decorate(TextDecoration.BOLD);

                        return Component.text()
                                .append(Component.text("  " + monument.getName() + " - ", NamedTextColor.WHITE))
                                .append(status)
                                .build();
                    });
                });
            }

            List<WoolObjective> woolObjectives = objectivesModule.getObjectives().stream()
                    .filter(obj -> obj instanceof WoolObjective)
                    .map(obj -> (WoolObjective) obj)
                    .filter(wool ->
                            wool.getTeam().isPresent() && wool.getTeam().get().equals(team))
                    .toList();

            Variorum.get().getLogger().info("Loaded wool objectives: " + woolObjectives.size());
            woolObjectives.forEach(o -> Variorum.get().getLogger().info(o.getName()));

            if (!woolObjectives.isEmpty()) {
                woolObjectives.forEach(wool -> {
                    contentBuilder.addDynamicLine(() -> {
                        Component status = wool.isCompleted()
                                ? Component.text("✓", NamedTextColor.GREEN).decorate(TextDecoration.BOLD)
                                : Component.text("✗", NamedTextColor.RED).decorate(TextDecoration.BOLD);

                        return Component.text()
                                .append(Component.text("  " + wool.getName() + " - ", NamedTextColor.WHITE))
                                .append(status)
                                .build();
                    });
                });
            }

            contentBuilder.addBlankLine();
        }

        contentBuilder.addDynamicLine(() -> Component.text()
                .append(Component.text("Map: ", NamedTextColor.GOLD))
                .append(Component.text(match.getMap().getName(), NamedTextColor.WHITE)
                        .decorate(TextDecoration.BOLD))
                .build());

        SidebarComponent content = contentBuilder.build();
        this.layout = new ComponentSidebarLayout(SidebarComponent.staticLine(Component.text("Objectives")), content);
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
        public void onGameStateChange(MatchOpenEvent event) {
            Bukkit.getOnlinePlayers().forEach(player -> {
                Variorum.get().getServer().getAsyncScheduler().runNow(Variorum.get(), task -> {
                    addPlayer(player);
                });
            });
        }

        @EventHandler
        public void onGameStateChange(GameStateChangeEvent event) {
            updateLayout();
        }

        @EventHandler
        public void onMonumentDestroyed(MonumentDestroyedEvent event) {
            updateLayout();
        }

        @EventHandler
        public void onWoolPlace(WoolPlaceEvent event) {
            updateLayout();
        }
    }
}
