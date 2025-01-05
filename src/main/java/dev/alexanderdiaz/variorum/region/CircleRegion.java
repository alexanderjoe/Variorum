package dev.alexanderdiaz.variorum.region;

import lombok.Getter;
import org.bukkit.util.Vector;

public class CircleRegion extends AbstractRegion {
    private final Vector center;
    @Getter
    private final double radius;
    private final double radiusSquared;

    public CircleRegion(Vector center, double radius) {
        this.center = center.clone();
        this.radius = radius;
        this.radiusSquared = radius * radius;
    }

    @Override
    public boolean contains(Vector vector) {
        if (vector.getY() != center.getY()) {
            return false;
        }

        double dx = vector.getX() - center.getX();
        double dz = vector.getZ() - center.getZ();
        return (dx * dx + dz * dz) <= radiusSquared;
    }

    @Override
    public Vector getMin() {
        return center.clone().subtract(new Vector(radius, 0, radius));
    }

    @Override
    public Vector getMax() {
        return center.clone().add(new Vector(radius, 0, radius));
    }

    @Override
    public Vector getCenter() {
        return center.clone();
    }
}

