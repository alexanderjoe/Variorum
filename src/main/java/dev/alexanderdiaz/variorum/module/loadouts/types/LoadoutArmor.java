package dev.alexanderdiaz.variorum.module.loadouts.types;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

public record LoadoutArmor(String material, @Getter boolean teamColor, @Getter boolean unbreakable) {

    @Override
    public String material() {
        return StringUtils.replace(material.trim(), " ", "_");
    }
}
