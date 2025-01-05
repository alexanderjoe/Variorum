package dev.alexanderdiaz.variorum.module.objectives;

import dev.alexanderdiaz.variorum.module.objectives.monument.MonumentDestroyedEvent;
import dev.alexanderdiaz.variorum.module.state.GameState;
import dev.alexanderdiaz.variorum.module.state.GameStateChangeEvent;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@RequiredArgsConstructor
public class ObjectivesListener implements Listener {
    private final ObjectivesModule module;

    @EventHandler
    public void onGameStateChange(GameStateChangeEvent event) {
        if (event.getNewState() != GameState.PLAYING) {
            // Prevent objective interaction when match isn't in progress
            module.getObjectives().forEach(Objective::disable);
        } else {
            // Re-enable objectives when match starts
            module.getObjectives().forEach(Objective::enable);
        }
    }

    @EventHandler
    public void onMonumentDestroyed(MonumentDestroyedEvent event) {
        // Check if all objectives are completed
        boolean allCompleted = module.getObjectives().stream()
                .allMatch(Objective::isCompleted);

        if (allCompleted) {
            // Here you could fire a match completion event or handle win conditions
            // This would be expanded based on your game rules
            event.getMatch().getRequiredModule(dev.alexanderdiaz.variorum.module.state.GameStateModule.class)
                    .setState(GameState.ENDED);
        }
    }
}