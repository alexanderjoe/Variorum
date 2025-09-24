package dev.alexanderdiaz.variorum.region;

import java.util.Objects;
import lombok.Getter;
import org.bukkit.util.Vector;

public class SphereRegion extends AbstractRegion {
  private final Vector center;

  @Getter
  private final double radius;

  private final double radiusSquared;

  public SphereRegion(Vector center, double radius) {
    if (radius <= 0) {
      throw new IllegalArgumentException("Radius must be positive");
    }
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

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    SphereRegion that = (SphereRegion) o;
    return Double.compare(radius, that.radius) == 0
        && Double.compare(radiusSquared, that.radiusSquared) == 0
        && Objects.equals(center, that.center);
  }

  @Override
  public int hashCode() {
    return Objects.hash(center, radius, radiusSquared);
  }
}
