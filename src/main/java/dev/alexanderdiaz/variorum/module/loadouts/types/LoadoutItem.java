package dev.alexanderdiaz.variorum.module.loadouts.types;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

public record LoadoutItem(
        String material,
        @Getter int amount,
        @Getter int slot,
        @Getter boolean unbreakable,
        @Getter List<String> enchantments) {
    public LoadoutItem(String material, int amount, int slot, boolean unbreakable, List<String> enchantments) {
        this.material = material;
        this.amount = amount;
        this.slot = slot;
        this.unbreakable = unbreakable;
        this.enchantments = new ArrayList<>(enchantments);
    }

    @Override
    public String material() {
        return StringUtils.replace(material.trim(), " ", "_");
    }
}
