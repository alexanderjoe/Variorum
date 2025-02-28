package dev.alexanderdiaz.variorum.module.state.handlers;

import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.state.GameState;
import dev.alexanderdiaz.variorum.module.state.GameStateHandler;
import dev.alexanderdiaz.variorum.module.state.GameStateTransition;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.GameMode;

@RequiredArgsConstructor
public class PlayingStateHandler implements GameStateHandler {
    private final Match match;

    @Override
    public GameState getState() {
        return GameState.PLAYING;
    }

    @Override
    public void onEnter(GameStateTransition transition) {
        match.getWorld().getPlayers().forEach(player -> {
            player.setGameMode(GameMode.SURVIVAL);
            player.showTitle(Title.title(
                    Component.text("Match Started!", NamedTextColor.GREEN),
                    Component.text("Good luck!", NamedTextColor.YELLOW),
                    Title.Times.times(Duration.ofSeconds(1), Duration.ofSeconds(3), Duration.ofSeconds(1))));
        });
    }

    @Override
    public void onExit(GameStateTransition transition) {}

    /**
     * Check if the match should end due to player count.
     *
     * @return True if the match should end
     */
    public boolean shouldEndDueToPlayerCount() {
        return match.getWorld().getPlayers().size() < 2;
    }
}
