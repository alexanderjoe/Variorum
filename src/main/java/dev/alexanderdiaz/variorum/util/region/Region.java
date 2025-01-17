package dev.alexanderdiaz.variorum.util.region;

import java.util.Set;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

public interface Region {
    /** Checks if a location is contained within this region */
    boolean contains(Location location);

    /** Checks if a vector is contained within this region */
    boolean contains(Vector vector);

    /** Gets all blocks in this region */
    Set<Block> getBlocks(World world);

    /** Gets the minimum point of this region */
    Vector getMin();

    /** Gets the maximum point of this region */
    Vector getMax();

    /** Gets the center point of this region */
    Vector getCenter();
}
