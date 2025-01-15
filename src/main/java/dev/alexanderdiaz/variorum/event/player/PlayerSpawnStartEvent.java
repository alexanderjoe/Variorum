package dev.alexanderdiaz.variorum.event.player;

import dev.alexanderdiaz.variorum.module.team.Team;
import java.util.Optional;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerSpawnStartEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final Optional<Team> team;

    @Getter
    private final boolean giveLoadout;

    @Getter
    private final boolean teleportPlayer;

    public PlayerSpawnStartEvent(@NotNull Player who, Team team, boolean giveLoadout, boolean teleportPlayer) {
        super(who);
        this.team = Optional.ofNullable(team);
        this.giveLoadout = giveLoadout;
        this.teleportPlayer = teleportPlayer;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}
