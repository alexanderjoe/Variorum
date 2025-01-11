package dev.alexanderdiaz.variorum.region;

import java.util.Collections;
import java.util.Set;
import lombok.Getter;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

public class PointRegion extends AbstractRegion {
    @Getter
    private final Vector point;

    public PointRegion(Vector point) {
        this.point = point.clone();
    }

    @Override
    public boolean contains(Vector vector) {
        return point.equals(vector);
    }

    @Override
    public Set<Block> getBlocks(World world) {
        return Collections.singleton(world.getBlockAt(point.getBlockX(), point.getBlockY(), point.getBlockZ()));
    }

    @Override
    public Vector getMin() {
        return point.clone();
    }

    @Override
    public Vector getMax() {
        return point.clone();
    }

    @Override
    public Vector getCenter() {
        return point.clone();
    }
}
