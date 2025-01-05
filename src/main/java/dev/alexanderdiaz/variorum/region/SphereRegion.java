package dev.alexanderdiaz.variorum.region;

import lombok.Getter;
import org.bukkit.util.Vector;

public class SphereRegion extends AbstractRegion {
    private final Vector center;
    @Getter
    private final double radius;
    private final double radiusSquared;

    public SphereRegion(Vector center, double radius) {
        this.center = center.clone();
        this.radius = radius;
        this.radiusSquared = radius * radius;
    }

    @Override
    public boolean contains(Vector vector) {
        return distanceSq(vector, center) <= radiusSquared;
    }

    @Override
    public Vector getMin() {
        return center.clone().subtract(new Vector(radius, radius, radius));
    }

    @Override
    public Vector getMax() {
        return center.clone().add(new Vector(radius, radius, radius));
    }

    @Override
    public Vector getCenter() {
        return center.clone();
    }
}
