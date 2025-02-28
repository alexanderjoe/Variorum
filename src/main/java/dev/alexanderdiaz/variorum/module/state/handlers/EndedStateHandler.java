package dev.alexanderdiaz.variorum.module.state.handlers;

import dev.alexanderdiaz.variorum.Variorum;
import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.state.GameState;
import dev.alexanderdiaz.variorum.module.state.GameStateHandler;
import dev.alexanderdiaz.variorum.module.state.GameStateTransition;
import dev.alexanderdiaz.variorum.module.state.countdown.CountdownManager;
import dev.alexanderdiaz.variorum.module.state.countdown.CountdownOptions;
import dev.alexanderdiaz.variorum.util.Players;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.GameMode;

@RequiredArgsConstructor
public class EndedStateHandler implements GameStateHandler {
    private static final int CYCLE_SECONDS = 10;

    private final Match match;
    private final CountdownManager countdownManager;

    @Override
    public GameState getState() {
        return GameState.ENDED;
    }

    @Override
    public void onEnter(GameStateTransition transition) {
        countdownManager.stopActiveCountdown();

        match.getPlayers().forEach(player -> {
            Players.reset(player);
            player.setGameMode(GameMode.ADVENTURE);
            player.showTitle(Title.title(
                    Component.text("Game Over!", NamedTextColor.GOLD),
                    Component.empty(),
                    Title.Times.times(Duration.ofSeconds(1), Duration.ofSeconds(3), Duration.ofSeconds(1))));
        });

        countdownManager.createCountdown(
                "cycle_countdown",
                CYCLE_SECONDS,
                CountdownOptions.builder()
                        .prefix(Component.text("Next map in ", NamedTextColor.YELLOW))
                        .build(),
                countdown -> {
                    Variorum.get().getMatchManager().getRotation().cycle();
                });

        countdownManager.startCountdown("cycle_countdown");
    }

    @Override
    public void onExit(GameStateTransition transition) {}
}
