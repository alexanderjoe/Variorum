package dev.alexanderdiaz.variorum.module.stats;

import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.Module;
import dev.alexanderdiaz.variorum.module.team.Team;
import dev.alexanderdiaz.variorum.module.team.TeamsModule;
import dev.alexanderdiaz.variorum.util.Events;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.Getter;
import org.bukkit.entity.Player;

public class StatsModule implements Module {
    private final Match match;
    private StatsListener listener;

    @Getter
    private final Map<UUID, PlayerStats> playerStats;

    public StatsModule(Match match) {
        this.match = match;
        this.playerStats = new HashMap<>();
    }

    @Override
    public void enable() {
        this.listener = new StatsListener(this);
        Events.register(listener);
    }

    @Override
    public void disable() {
        if (listener != null) {
            Events.unregister(listener);
        }
        playerStats.clear();
    }

    public PlayerStats getPlayerStats(Player player) {
        return playerStats.computeIfAbsent(player.getUniqueId(), k -> new PlayerStats());
    }

    @Nullable public Team getPlayerTeam(Player player) {
        return match.getRequiredModule(TeamsModule.class).getPlayerTeam(player).orElse(null);
    }
}
