package dev.alexanderdiaz.variorum.module.objectives.monument;

import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.objectives.Objective;
import dev.alexanderdiaz.variorum.module.team.Team;
import dev.alexanderdiaz.variorum.region.Region;
import dev.alexanderdiaz.variorum.util.Events;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.Set;

public class MonumentObjective implements Objective {
    private final Match match;
    private final String name;
    @Getter
    private final Team owner;
    @Getter
    private final Region region;
    private final Set<Material> materials;
    private boolean broken = false;

    public MonumentObjective(Match match, String name, Team owner, Region region, Set<Material> materials) {
        this.match = match;
        this.name = name;
        this.owner = owner;
        this.region = region;
        this.materials = new HashSet<>(materials);
    }

    @Override
    public void enable() {
        // No longer needs to register a listener
    }

    @Override
    public void disable() {
        // No longer needs to unregister a listener
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isCompleted() {
        return broken;
    }

    public boolean isMonumentBlock(Block block) {
        return region.contains(block.getLocation());
    }

    public boolean isValidMaterial(Material material) {
        return materials.contains(material);
    }

    public void markBroken() {
        // Only fire the event if the monument wasn't already broken
        if (!broken) {
            this.broken = true;
            Events.call(new MonumentDestroyedEvent(match, this));
        }
    }
}