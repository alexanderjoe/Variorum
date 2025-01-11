package dev.alexanderdiaz.variorum.module.loadouts;

import dev.alexanderdiaz.variorum.module.loadouts.types.LoadoutArmor;
import dev.alexanderdiaz.variorum.module.loadouts.types.LoadoutEffect;
import dev.alexanderdiaz.variorum.module.loadouts.types.LoadoutItem;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

// Data classes for loadout configuration
@Getter
public class Loadout {
    private final String id;
    private final LoadoutArmor helmet;
    private final LoadoutArmor chestplate;
    private final LoadoutArmor leggings;
    private final LoadoutArmor boots;
    private final List<LoadoutItem> items;
    private final List<LoadoutEffect> effects;

    public Loadout(
            String id,
            LoadoutArmor helmet,
            LoadoutArmor chestplate,
            LoadoutArmor leggings,
            LoadoutArmor boots,
            List<LoadoutItem> items,
            List<LoadoutEffect> effects) {
        this.id = id;
        this.helmet = helmet;
        this.chestplate = chestplate;
        this.leggings = leggings;
        this.boots = boots;
        this.items = new ArrayList<>(items);
        this.effects = new ArrayList<>(effects);
    }
}
