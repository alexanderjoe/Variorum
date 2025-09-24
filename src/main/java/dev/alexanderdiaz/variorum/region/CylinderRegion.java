package dev.alexanderdiaz.variorum.region;

import lombok.Getter;
import org.bukkit.util.Vector;

public class CylinderRegion extends AbstractRegion {
  private final Vector center;

  @Getter
  private final double radius;

  private final double radiusSquared;

  @Getter
  private final double height;

  public CylinderRegion(Vector center, double radius, double height) {
    if (radius <= 0) {
      throw new IllegalArgumentException("Radius must be positive");
    }
    this.center = center.clone();
    this.radius = radius;
    this.radiusSquared = radius * radius;
    this.height = height;
  }

  @Override
  public boolean contains(Vector vector) {
    if (Math.abs(vector.getY() - center.getY()) > height / 2) {
      return false;
    }

    double dx = vector.getX() - center.getX();
    double dz = vector.getZ() - center.getZ();
    return (dx * dx + dz * dz) <= radiusSquared;
  }

  @Override
  public Vector getMin() {
    return center.clone().subtract(new Vector(radius, height / 2, radius));
  }

  @Override
  public Vector getMax() {
    return center.clone().add(new Vector(radius, height / 2, radius));
  }

  @Override
  public Vector getCenter() {
    return center.clone();
  }
}
