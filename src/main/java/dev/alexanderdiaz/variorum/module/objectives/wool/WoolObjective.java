package dev.alexanderdiaz.variorum.module.objectives.wool;

import com.google.common.base.Preconditions;
import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.objectives.Objective;
import dev.alexanderdiaz.variorum.module.team.Team;
import dev.alexanderdiaz.variorum.module.team.TeamsModule;
import dev.alexanderdiaz.variorum.region.Region;
import dev.alexanderdiaz.variorum.util.Colors;
import java.util.Optional;
import lombok.Getter;
import lombok.ToString;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

@Getter
@ToString(exclude = "match")
public class WoolObjective implements Objective {
  private final Match match;
  private final String name;
  private final Optional<Team> team;
  private final DyeColor color;
  private final Region destination;
  private final Optional<Region> source;
  private boolean pickup;
  private boolean refill;
  private boolean placed = false;

  public WoolObjective(
      Match match,
      String name,
      Optional<Team> team,
      DyeColor color,
      Region destination,
      Optional<Region> source,
      boolean pickup,
      boolean refill) {
    this.match = match;
    this.name = name;
    this.team = team;
    this.color = color;
    this.destination = destination;
    this.source = source;
    this.pickup = pickup;
    this.refill = refill;
  }

  @Override
  public void enable() {}

  @Override
  public void disable() {}

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean isCompleted() {
    return placed;
  }

  public void place(Player player) {
    Preconditions.checkArgument(!this.placed, "wool already placed");
    this.placed = true;

    Team team =
        this.match.getRequiredModule(TeamsModule.class).getPlayerTeam(player).orElse(null);
    if (team == null) {
      throw new IllegalStateException("team not found");
    }

    var message = Component.text(this.name, Colors.dyeToTextColor(color), TextDecoration.BOLD)
        .append(Component.text(" was placed by ", NamedTextColor.GRAY, TextDecoration.BOLD))
        .append(Component.text(player.getName(), team.textColor(), TextDecoration.BOLD));

    this.match.broadcast(message);
  }

  public boolean canComplete(Team team) {
    return this.team.isEmpty() || this.team.get().equals(team);
  }

  public boolean isValidDestination(Block block) {
    return destination.contains(block.getLocation());
  }

  public boolean isValidSource(Block block) {
    if (source.isEmpty()) return false;

    return source.get().contains(block.getLocation());
  }

  public boolean isRefillEnabled() {
    return refill;
  }

  public boolean isValidBlock(Material material) {
    if (!material.isBlock()) return false;

    return switch (color) {
      case WHITE -> material == Material.WHITE_WOOL;
      case ORANGE -> material == Material.ORANGE_WOOL;
      case MAGENTA -> material == Material.MAGENTA_WOOL;
      case LIGHT_BLUE -> material == Material.LIGHT_BLUE_WOOL;
      case YELLOW -> material == Material.YELLOW_WOOL;
      case LIME -> material == Material.LIME_WOOL;
      case PINK -> material == Material.PINK_WOOL;
      case GRAY -> material == Material.GRAY_WOOL;
      case LIGHT_GRAY -> material == Material.LIGHT_GRAY_WOOL;
      case CYAN -> material == Material.CYAN_WOOL;
      case PURPLE -> material == Material.PURPLE_WOOL;
      case BLUE -> material == Material.BLUE_WOOL;
      case BROWN -> material == Material.BROWN_WOOL;
      case GREEN -> material == Material.GREEN_WOOL;
      case RED -> material == Material.RED_WOOL;
      case BLACK -> material == Material.BLACK_WOOL;
    };
  }
}
