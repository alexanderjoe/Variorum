package dev.alexanderdiaz.variorum.module.team;

import lombok.ToString;
import net.kyori.adventure.text.format.NamedTextColor;

@ToString
public class Team {
    private final String id;
    private final String name;
    private final String color;

    public Team(String id, String name, String color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    public NamedTextColor textColor() {
        return switch (this.color) {
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
            case "light_purple" -> NamedTextColor.LIGHT_PURPLE;
            case "yellow" -> NamedTextColor.YELLOW;
            case "white" -> NamedTextColor.WHITE;
            default -> NamedTextColor.AQUA;
        };
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String color() {
        return color;
    }
}