package dev.alexanderdiaz.variorum.module.objectives;

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
            module.getObjectives().forEach(Objective::disable);
        } else {
            module.getObjectives().forEach(Objective::enable);
        }
    }
}