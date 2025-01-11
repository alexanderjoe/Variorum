package dev.alexanderdiaz.variorum.util;

import org.bukkit.entity.Player;

public class Players {
    public static void reset(Player player) {
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setSaturation(0);
        player.setLevel(0);
        player.setExp(0);
        player.closeInventory();
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
    }
}
