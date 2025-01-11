package dev.alexanderdiaz.variorum.module.objectives;

import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.Module;
import dev.alexanderdiaz.variorum.module.objectives.monument.MonumentListener;
import dev.alexanderdiaz.variorum.module.objectives.monument.MonumentObjective;
import dev.alexanderdiaz.variorum.module.objectives.wool.WoolListener;
import dev.alexanderdiaz.variorum.module.objectives.wool.WoolObjective;
import dev.alexanderdiaz.variorum.util.Events;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.bukkit.event.Listener;

@Getter
public class ObjectivesModule implements Module {
    private final Match match;
    private final List<Objective> objectives;
    private final List<Listener> listeners;

    public ObjectivesModule(Match match) {
        this.match = match;
        this.objectives = new ArrayList<>();
        this.listeners = new ArrayList<>();
    }

    public void addObjective(Objective objective) {
        objectives.add(objective);
    }

    @Override
    public void enable() {
        this.listeners.add(new ObjectivesListener(this));

        if (objectives.stream().anyMatch(objective -> objective instanceof MonumentObjective)) {
            this.listeners.add(new MonumentListener(this));
        }

        if (objectives.stream().anyMatch(objective -> objective instanceof WoolObjective)) {
            this.listeners.add(new WoolListener(this));
        }

        objectives.forEach(Objective::enable);
        this.listeners.forEach(Events::register);
    }

    @Override
    public void disable() {
        this.listeners.forEach(Events::unregister);

        objectives.forEach(Objective::disable);
        objectives.clear();
    }
}
