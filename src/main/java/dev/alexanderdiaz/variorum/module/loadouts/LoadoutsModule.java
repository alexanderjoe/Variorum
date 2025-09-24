package dev.alexanderdiaz.variorum.module.loadouts;

import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.Module;
import dev.alexanderdiaz.variorum.module.loadouts.types.LoadoutArmor;
import dev.alexanderdiaz.variorum.module.loadouts.types.LoadoutEffect;
import dev.alexanderdiaz.variorum.module.loadouts.types.LoadoutItem;
import dev.alexanderdiaz.variorum.module.team.Team;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@Getter
public class LoadoutsModule implements Module {
  private final Match match;

  public LoadoutsModule(Match match) {
    this.match = match;
  }

  @Override
  public void enable() {}

  @Override
  public void disable() {}

  public void applyLoadout(Player player, String loadoutId, Team team) {
    Loadout loadout = match.getRegistry().get(Loadout.class, loadoutId, true).orElse(null);

    if (loadout == null) return;

    player.getInventory().clear();

    if (loadout.getHelmet() != null) {
      player.getInventory().setHelmet(createArmorPiece(loadout.getHelmet(), team));
    }
    if (loadout.getChestplate() != null) {
      player.getInventory().setChestplate(createArmorPiece(loadout.getChestplate(), team));
    }
    if (loadout.getLeggings() != null) {
      player.getInventory().setLeggings(createArmorPiece(loadout.getLeggings(), team));
    }
    if (loadout.getBoots() != null) {
      player.getInventory().setBoots(createArmorPiece(loadout.getBoots(), team));
    }

    for (LoadoutItem item : loadout.getItems()) {
      player.getInventory().setItem(item.slot(), createItem(item));
    }

    for (LoadoutEffect effect : loadout.getEffects()) {
      player.addPotionEffect(createEffect(effect));
    }
  }

  private ItemStack createArmorPiece(LoadoutArmor armor, Team team) {
    ItemStack item = new ItemStack(Material.valueOf(armor.material().toUpperCase()));
    ItemMeta meta = item.getItemMeta();

    if (meta instanceof LeatherArmorMeta leatherMeta && armor.teamColor() && team != null) {
      leatherMeta.setColor(getTeamColor(team));
    }

    if (armor.unbreakable()) {
      meta.setUnbreakable(true);
    }

    item.setItemMeta(meta);
    return item;
  }

  private ItemStack createItem(LoadoutItem item) {
    ItemStack itemStack =
        new ItemStack(Material.valueOf(item.material().toUpperCase()), item.amount());
    ItemMeta meta = itemStack.getItemMeta();

    if (item.unbreakable()) {
      meta.setUnbreakable(true);
    }

    for (String enchantStr : item.enchantments()) {
      Enchantment enchant = parseEnchantment(enchantStr);
      if (enchant != null) {
        meta.addEnchant(enchant, 1, true);
      }
    }

    itemStack.setItemMeta(meta);
    return itemStack;
  }

  private PotionEffect createEffect(LoadoutEffect effect) {
    var parsedEffect =
        switch (effect.type().toUpperCase()) {
          case "SPEED", "SWIFTNESS" -> PotionEffectType.SPEED;
          case "SLOWNESS" -> PotionEffectType.SLOWNESS;
          case "HASTE", "MINING_SPEED" -> PotionEffectType.HASTE;
          case "MINING_FATIGUE" -> PotionEffectType.MINING_FATIGUE;
          case "STRENGTH" -> PotionEffectType.STRENGTH;
          case "INSTANT_HEALTH" -> PotionEffectType.INSTANT_HEALTH;
          case "INSTANT_DAMAGE" -> PotionEffectType.INSTANT_DAMAGE;
          case "JUMP_BOOST" -> PotionEffectType.JUMP_BOOST;
          case "NAUSEA" -> PotionEffectType.NAUSEA;
          case "REGENERATION" -> PotionEffectType.REGENERATION;
          case "RESISTANCE", "DAMAGE_RESISTANCE" -> PotionEffectType.RESISTANCE;
          case "FIRE_RESISTANCE" -> PotionEffectType.FIRE_RESISTANCE;
          case "WATER_BREATHING" -> PotionEffectType.WATER_BREATHING;
          case "INVISIBILITY" -> PotionEffectType.INVISIBILITY;
          case "BLINDNESS" -> PotionEffectType.BLINDNESS;
          case "NIGHT_VISION" -> PotionEffectType.NIGHT_VISION;
          case "HUNGER" -> PotionEffectType.HUNGER;
          case "WEAKNESS" -> PotionEffectType.WEAKNESS;
          case "POISON" -> PotionEffectType.POISON;
          case "WITHER" -> PotionEffectType.WITHER;
          case "HEALTH_BOOST" -> PotionEffectType.HEALTH_BOOST;
          case "ABSORPTION" -> PotionEffectType.ABSORPTION;
          case "SATURATION" -> PotionEffectType.SATURATION;
          case "GLOWING" -> PotionEffectType.GLOWING;
          case "LEVITATION" -> PotionEffectType.LEVITATION;
          case "LUCK" -> PotionEffectType.LUCK;
          case "UNLUCK" -> PotionEffectType.UNLUCK;
          case "SLOW_FALLING" -> PotionEffectType.SLOW_FALLING;
          case "CONDUIT_POWER" -> PotionEffectType.CONDUIT_POWER;
          case "DOLPHINS_GRACE" -> PotionEffectType.DOLPHINS_GRACE;
          case "BAD_OMEN" -> PotionEffectType.BAD_OMEN;
          case "HERO_OF_THE_VILLAGE" -> PotionEffectType.HERO_OF_THE_VILLAGE;
          case "DARKNESS" -> PotionEffectType.DARKNESS;
          case "TRIAL_OMEN" -> PotionEffectType.TRIAL_OMEN;
          case "RAID_OMEN" -> PotionEffectType.RAID_OMEN;
          case "WIND_CHARGED" -> PotionEffectType.WIND_CHARGED;
          case "WEAVING" -> PotionEffectType.WEAVING;
          case "OOZING" -> PotionEffectType.OOZING;
          case "INFESTED" -> PotionEffectType.INFESTED;
          default -> null;
        };
    if (parsedEffect == null) return null;

    return new PotionEffect(parsedEffect, parseTime(effect.duration()), 1);
  }

  private Color getTeamColor(Team team) {
    return switch (team.color().toLowerCase()) {
      case "red", "dark_red" -> Color.RED;
      case "blue", "dark_blue" -> Color.BLUE;
      case "green", "dark_green" -> Color.GREEN;
      case "yellow" -> Color.YELLOW;
      case "purple", "dark_purple" -> Color.PURPLE;
      case "orange", "gold" -> Color.ORANGE;
      case "aqua", "dark_aqua", "cyan" -> Color.AQUA;
      default -> Color.WHITE;
    };
  }

  private Enchantment parseEnchantment(String name) {
    return switch (name.toLowerCase().replace(" ", "_")) {
      case "arrow_infinite", "infinity" -> Enchantment.INFINITY;
      case "unbreaking", "durability" -> Enchantment.UNBREAKING;
      case "fire_protection" -> Enchantment.FIRE_PROTECTION;
      case "protection" -> Enchantment.PROTECTION;
      case "projectile_protection" -> Enchantment.PROJECTILE_PROTECTION;
      case "blast_protection" -> Enchantment.BLAST_PROTECTION;
      case "feather_falling" -> Enchantment.FEATHER_FALLING;
      case "aqua_affinity" -> Enchantment.AQUA_AFFINITY;
      case "thorns" -> Enchantment.THORNS;
      case "depth_strider" -> Enchantment.DEPTH_STRIDER;
      case "frost_walker" -> Enchantment.FROST_WALKER;
      case "sharpness" -> Enchantment.SHARPNESS;
      case "smite" -> Enchantment.SMITE;
      case "bane_of_arthropods" -> Enchantment.BANE_OF_ARTHROPODS;
      case "knockback" -> Enchantment.KNOCKBACK;
      case "fire_aspect" -> Enchantment.FIRE_ASPECT;
      case "looting" -> Enchantment.LOOTING;
      case "efficiency" -> Enchantment.EFFICIENCY;
      case "silk_touch" -> Enchantment.SILK_TOUCH;
      case "fortune" -> Enchantment.FORTUNE;
      case "power" -> Enchantment.POWER;
      case "punch" -> Enchantment.PUNCH;
      case "flame" -> Enchantment.FLAME;
      case "mending" -> Enchantment.MENDING;
      default -> null;
    };
  }

  private int parseTime(String time) {
    Pattern pattern = Pattern.compile("(\\d+)(s|m|h)");
    Matcher matcher = pattern.matcher(time.toLowerCase());

    if (!matcher.matches()) {
      return 20; // Default 1 second if invalid format
    }

    int value = Integer.parseInt(matcher.group(1));
    String unit = matcher.group(2);

    return switch (unit) {
      case "s" -> value * 20; // seconds to ticks
      case "m" -> value * 20 * 60; // minutes to ticks
      case "h" -> value * 20 * 60 * 60; // hours to ticks
      default -> 20;
    };
  }
}
