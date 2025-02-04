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

    public static NamedTextColor stringToTextColor(String color) {
        return switch (color) {
            case "black" -> NamedTextColor.BLACK;
            case "dark_blue" -> NamedTextColor.DARK_BLUE;
            case "dark_green" -> NamedTextColor.DARK_GREEN;
            case "dark_aqua" -> NamedTextColor.DARK_AQUA;
            case "dark_red" -> NamedTextColor.DARK_RED;
            case "dark_purple" -> NamedTextColor.DARK_PURPLE;
            case "gold" -> NamedTextColor.GOLD;
            case "gray" -> NamedTextColor.GRAY;
            case "dark_gray" -> NamedTextColor.DARK_GRAY;
            case "blue" -> NamedTextColor.BLUE;
            case "green" -> NamedTextColor.GREEN;
            case "red" -> NamedTextColor.RED;
            case "light_purple", "purple" -> NamedTextColor.LIGHT_PURPLE;
            case "yellow" -> NamedTextColor.YELLOW;
            case "white" -> NamedTextColor.WHITE;
            default -> NamedTextColor.AQUA;
        };
    }
}
