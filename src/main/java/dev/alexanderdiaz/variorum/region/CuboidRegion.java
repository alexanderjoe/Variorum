package dev.alexanderdiaz.variorum.region;

import lombok.Getter;
import org.bukkit.util.Vector;

public class CuboidRegion extends AbstractRegion {
  @Getter
  private final Vector min;

  @Getter
  private final Vector max;

  public CuboidRegion(Vector pos1, Vector pos2) {
    this.min = new Vector(
        Math.min(pos1.getX(), pos2.getX()),
        Math.min(pos1.getY(), pos2.getY()),
        Math.min(pos1.getZ(), pos2.getZ()));
    this.max = new Vector(
        Math.max(pos1.getX(), pos2.getX()),
        Math.max(pos1.getY(), pos2.getY()),
        Math.max(pos1.getZ(), pos2.getZ()));
  }

  @Override
  public boolean contains(Vector vector) {
    return vector.getX() >= min.getX()
        && vector.getX() <= max.getX()
        && vector.getY() >= min.getY()
        && vector.getY() <= max.getY()
        && vector.getZ() >= min.getZ()
        && vector.getZ() <= max.getZ();
  }

  @Override
  public Vector getCenter() {
    return min.clone().add(max).multiply(0.5);
  }
}
