package dev.alexanderdiaz.variorum.region;

import java.util.Set;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

abstract class AbstractRegion implements Region {
    @Override
    public boolean contains(Location location) {
        return contains(location.toVector());
    }

    @Override
    public Set<Block> getBlocks(World world) {
        Set<Block> blocks = new java.util.HashSet<>();
        Vector min = getMin();
        Vector max = getMax();

        for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
            for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                    Vector pos = new Vector(x, y, z);
                    if (contains(pos)) {
                        blocks.add(world.getBlockAt(x, y, z));
                    }
                }
            }
        }
        return blocks;
    }

    protected double lengthSq(Vector vec) {
        return vec.lengthSquared();
    }

    protected double distanceSq(Vector v1, Vector v2) {
        return v1.clone().subtract(v2).lengthSquared();
    }
}
