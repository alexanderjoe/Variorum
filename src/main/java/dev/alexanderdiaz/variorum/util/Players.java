package dev.alexanderdiaz.variorum.util;

import dev.alexanderdiaz.variorum.Variorum;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Players {
    public static void reset(Player player) {
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setFlySpeed(0.1F);
        player.setWalkSpeed(0.2F);
        player.setExp(0);
        player.setTotalExperience(0);
        player.setLevel(0);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setSaturation(5);
        player.setCanPickupItems(true);
        player.setFireTicks(0);
        Variorum.get().getLogger().info(player.getRemainingAir() + " air remaining");
        player.setFallDistance(0.0F);
        player.setVelocity(new Vector());
        player.resetPlayerWeather();
        player.resetPlayerTime();
        player.resetCooldown();
        player.setItemOnCursor(null);

        player.eject();
        if (player.getVehicle() != null) {
            player.getVehicle().eject();
        }

        player.closeInventory();
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.updateInventory();

        player.clearActivePotionEffects();
    }
}
