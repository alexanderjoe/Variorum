package dev.alexanderdiaz.variorum.module.objectives;

import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.Module;
import dev.alexanderdiaz.variorum.module.objectives.monument.MonumentListener;
import dev.alexanderdiaz.variorum.module.objectives.monument.MonumentObjective;
import dev.alexanderdiaz.variorum.util.Events;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class ObjectivesModule implements Module {
    private final Match match;
    @Getter
    private final List<Objective> objectives;
    private ObjectivesListener objectivesListener;
    private MonumentListener monumentListener;

    public ObjectivesModule(Match match) {
        this.match = match;
        this.objectives = new ArrayList<>();
    }

    public void addObjective(Objective objective) {
        objectives.add(objective);
    }

    @Override
    public void enable() {
        // Register listeners
        this.objectivesListener = new ObjectivesListener(this);
        this.monumentListener = new MonumentListener(this);
        Events.register(objectivesListener);
        Events.register(monumentListener);

        // Enable all objectives
        objectives.forEach(Objective::enable);
    }

    @Override
    public void disable() {
        if (objectivesListener != null) {
            Events.unregister(objectivesListener);
        }
        if (monumentListener != null) {
            Events.unregister(monumentListener);
        }

        // Disable all objectives
        objectives.forEach(Objective::disable);
        objectives.clear();
    }
}