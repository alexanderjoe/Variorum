package dev.alexanderdiaz.variorum.region;

import lombok.Getter;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import java.util.Collections;
import java.util.Set;

public class BlockRegion extends AbstractRegion {
    @Getter
    private final Vector position;

    public BlockRegion(Vector position) {
        this.position = position.clone();
    }

    @Override
    public boolean contains(Vector vector) {
        return vector.getBlockX() == position.getBlockX()
                && vector.getBlockY() == position.getBlockY()
                && vector.getBlockZ() == position.getBlockZ();
    }

    @Override
    public Set<Block> getBlocks(World world) {
        return Collections.singleton(world.getBlockAt(
                position.getBlockX(),
                position.getBlockY(),
                position.getBlockZ()
        ));
    }

    @Override
    public Vector getMin() {
        return position.clone();
    }

    @Override
    public Vector getMax() {
        return position.clone();
    }

    @Override
    public Vector getCenter() {
        return position.clone().add(new Vector(0.5, 0.5, 0.5));
    }
}