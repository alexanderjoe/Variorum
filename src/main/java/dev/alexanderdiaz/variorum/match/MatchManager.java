package dev.alexanderdiaz.variorum.match;

import dev.alexanderdiaz.variorum.Variorum;
import dev.alexanderdiaz.variorum.map.rotation.Rotation;
import java.io.IOException;
import java.util.logging.Level;
import lombok.Getter;

public class MatchManager {
    @Getter
    private final MatchFactory matchFactory;

    @Getter
    private final Rotation rotation;

    public MatchManager(MatchFactory factory, Rotation rotation) {
        this.matchFactory = factory;
        this.rotation = rotation;
    }

    public void start() throws IOException {
        this.rotation.start();
    }

    public void shutdown() {
        for (final Match match : this.rotation.getMapQueue()) {
            try {
                match.unload();
            } catch (Exception e) {
                Variorum.get().getLogger().log(Level.WARNING, "Failed to delete match: " + match, e.getMessage());
            }
        }
    }
}
