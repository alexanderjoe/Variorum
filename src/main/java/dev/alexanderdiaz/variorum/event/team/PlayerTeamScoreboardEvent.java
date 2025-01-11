package dev.alexanderdiaz.variorum.event.team;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.scoreboard.Scoreboard;

@Getter
public class PlayerTeamScoreboardEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final Scoreboard teamScoreboard;

    public PlayerTeamScoreboardEvent(Player player, Scoreboard teamScoreboard) {
        this.player = player;
        this.teamScoreboard = teamScoreboard;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
