package dev.alexanderdiaz.variorum.map.rotation;

import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.match.MatchFactory;

import java.util.List;

public abstract class AbstractRotationProvider implements RotationProvider {
    protected final MatchFactory factory;

    AbstractRotationProvider(MatchFactory factory) {
        this.factory = factory;
    }

//    Rotation defaultRotation(List<Match> maps) {
//
//    }
}
