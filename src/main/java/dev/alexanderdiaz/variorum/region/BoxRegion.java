package dev.alexanderdiaz.variorum.region;

import org.bukkit.util.Vector;

public class BoxRegion extends CuboidRegion {
  public BoxRegion(Vector center, double xSize, double ySize, double zSize) {
    super(
        center.clone().subtract(new Vector(xSize / 2, ySize / 2, zSize / 2)),
        center.clone().add(new Vector(xSize / 2, ySize / 2, zSize / 2)));
  }

  public BoxRegion(Vector center, Vector size) {
    this(center, size.getX(), size.getY(), size.getZ());
  }
}
