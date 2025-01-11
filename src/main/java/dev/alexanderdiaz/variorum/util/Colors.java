package dev.alexanderdiaz.variorum.util;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.DyeColor;

public class Colors {
    public static NamedTextColor dyeToTextColor(DyeColor color) {
        return switch (color) {
            case ORANGE, BROWN -> NamedTextColor.GOLD;
            case MAGENTA, PINK -> NamedTextColor.LIGHT_PURPLE;
            case LIGHT_BLUE, BLUE -> NamedTextColor.BLUE;
            case YELLOW -> NamedTextColor.YELLOW;
            case LIME -> NamedTextColor.GREEN;
            case GRAY -> NamedTextColor.DARK_GRAY;
            case LIGHT_GRAY, BLACK -> NamedTextColor.GRAY;
            case CYAN -> NamedTextColor.DARK_AQUA;
            case PURPLE -> NamedTextColor.DARK_PURPLE;
            case GREEN -> NamedTextColor.DARK_GREEN;
            case RED -> NamedTextColor.DARK_RED;
            default -> NamedTextColor.WHITE;
        };
    }
}
