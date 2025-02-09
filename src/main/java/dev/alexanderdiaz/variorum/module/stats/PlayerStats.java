package dev.alexanderdiaz.variorum.module.stats;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

@Getter
public class PlayerStats {
    private int kills = 0;
    private int deaths = 0;
    private int objectives = 0;
    private double damageDealt = 0;
    private double damageTaken = 0;
    private double longestShot = 0;

    public void incrementKills() {
        kills++;
    }

    public void incrementDeaths() {
        deaths++;
    }

    public void incrementObjectives() {
        objectives++;
    }

    public void addDamageDealt(double damage) {
        damageDealt += damage;
    }

    public void addDamageTaken(double damage) {
        damageTaken += damage;
    }

    public void updateLongestShot(double distance) {
        if (distance > longestShot) {
            longestShot = distance;
        }
    }

    public @NotNull Component generateSummary() {
        return Component.text()
                .append(Component.text("Kills: ", NamedTextColor.GOLD))
                .append(Component.text(kills, NamedTextColor.WHITE))
                .append(Component.text(" | ", NamedTextColor.GRAY))
                .append(Component.text("Deaths: ", NamedTextColor.GOLD))
                .append(Component.text(deaths, NamedTextColor.WHITE))
                .append(Component.text(" | ", NamedTextColor.GRAY))
                .append(Component.text("Objectives: ", NamedTextColor.GOLD))
                .append(Component.text(objectives, NamedTextColor.WHITE))
                .append(Component.text(" | ", NamedTextColor.GRAY))
                .append(Component.text("Damage Dealt: ", NamedTextColor.GOLD))
                .append(Component.text(damageDealt, NamedTextColor.WHITE))
                .append(Component.text(" | ", NamedTextColor.GRAY))
                .append(Component.text("Damage Taken: ", NamedTextColor.GOLD))
                .append(Component.text(damageTaken, NamedTextColor.WHITE))
                .append(Component.text(" | ", NamedTextColor.GRAY))
                .append(Component.text("Longest Shot: ", NamedTextColor.GOLD))
                .append(Component.text(longestShot, NamedTextColor.WHITE))
                .build();
    }
}
