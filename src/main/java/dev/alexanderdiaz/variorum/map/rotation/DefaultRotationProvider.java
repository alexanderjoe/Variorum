package dev.alexanderdiaz.variorum.map.rotation;

import dev.alexanderdiaz.variorum.Variorum;
import dev.alexanderdiaz.variorum.match.MatchFactory;

public class DefaultRotationProvider extends AbstractRotationProvider {
    private Rotation rotation;

    public DefaultRotationProvider(MatchFactory factory, Variorum plugin) {
        super(factory, plugin);
        this.rotation = defaultRotation();
    }

    @Override
    public Rotation provideRotation() {
        if (rotation == null || rotation.getMapQueue().isEmpty()) {
            rotation = defaultRotation();
        }
        return rotation;
    }
}
