package dev.alexanderdiaz.variorum.command;

import static net.kyori.adventure.text.Component.text;

import dev.alexanderdiaz.variorum.Variorum;
import dev.alexanderdiaz.variorum.map.MapManager;
import dev.alexanderdiaz.variorum.map.VariorumMap;
import dev.alexanderdiaz.variorum.map.rotation.Rotation;
import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.match.MatchManager;
import dev.alexanderdiaz.variorum.util.Colors;
import dev.alexanderdiaz.variorum.util.Paginator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Default;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;

public class RotationCommands {
  private final Variorum plugin;

  public RotationCommands(Variorum plugin) {
    this.plugin = plugin;
  }

  @Command("rotation|rot [page]")
  @CommandDescription("View the current map rotation")
  public void rotation(
      final CommandSender sender,
      final @Default("1") @Argument(value = "page", suggestions = "rotationPageNumbers") int page) {
    MatchManager mm = plugin.getMatchManager();
    Rotation rotation = mm.getRotation();
    Match currentMatch = rotation.getMatch();
    List<Match> matches = rotation.getMapQueue();

    Paginator<Match> paginator = new Paginator<>(matches, 5);
    List<Match> pageMatches = paginator.getPage(page);

    var msg = text()
        .append(text("Current map rotation:").color(NamedTextColor.YELLOW))
        .append(text("\n"))
        .append(text("Page ").color(NamedTextColor.YELLOW))
        .append(text(page).color(NamedTextColor.GOLD))
        .append(text(" of ").color(NamedTextColor.YELLOW))
        .append(text(paginator.getTotalPages()).color(NamedTextColor.GOLD));

    for (Match match : pageMatches) {
      NamedTextColor color = match == currentMatch ? NamedTextColor.RED : NamedTextColor.GOLD;
      msg.append(text("\n - ").color(NamedTextColor.GRAY))
          .append(text(match.getMap().getName()).color(color));
    }

    sender.sendMessage(msg.build());
  }

  @Suggestions("rotationPageNumbers")
  public List<String> suggestRotationPageNumbers(CommandContext<Player> context, String input) {
    MatchManager mm = plugin.getMatchManager();
    Rotation rotation = mm.getRotation();
    List<Match> matches = rotation.getMapQueue();

    Paginator<Match> paginator = new Paginator<>(matches, 5);
    int totalPages = paginator.getTotalPages();

    return java.util.stream.IntStream.rangeClosed(1, totalPages)
        .mapToObj(String::valueOf)
        .filter(page -> page.startsWith(input))
        .toList();
  }

  @Command("maps [page]")
  @CommandDescription("View all of the available maps")
  public void maps(
      final CommandSender sender,
      final @Default("1") @Argument(value = "page", suggestions = "mapsPageNumbers") int page) {
    MapManager mm = plugin.getMapManager();
    List<VariorumMap> maps = mm.getAllMaps().values().stream().toList();

    Paginator<VariorumMap> paginator = new Paginator<>(maps, 5);
    List<VariorumMap> pageMaps = paginator.getPage(page);

    var msg = text()
        .append(text("Available maps:").color(NamedTextColor.YELLOW))
        .append(text("\n"))
        .append(text("Page ").color(NamedTextColor.YELLOW))
        .append(text(page).color(NamedTextColor.GOLD))
        .append(text(" of ").color(NamedTextColor.YELLOW))
        .append(text(paginator.getTotalPages()).color(NamedTextColor.GOLD));

    for (VariorumMap map : pageMaps) {
      msg.append(text("\n - ").color(NamedTextColor.GRAY))
          .append(text(map.getName()).color(NamedTextColor.GOLD));
    }

    sender.sendMessage(msg.build());
  }

  @Suggestions("mapsPageNumbers")
  public List<String> suggestMapsPageNumbers(CommandContext<Player> context, String input) {
    MapManager mm = plugin.getMapManager();
    List<VariorumMap> maps = mm.getAllMaps().values().stream().toList();

    Paginator<VariorumMap> paginator = new Paginator<>(maps, 5);
    int totalPages = paginator.getTotalPages();

    return java.util.stream.IntStream.rangeClosed(1, totalPages)
        .mapToObj(String::valueOf)
        .filter(page -> page.startsWith(input))
        .toList();
  }

  @Command("map <map>")
  @CommandDescription("View details of the specified map.")
  public void mapDetail(final CommandSender sender, final @Argument(value = "map") String mapName) {
    MapManager mm = plugin.getMapManager();
    Optional<VariorumMap> maybeMap = mm.getMapByName(mapName);

    if (maybeMap.isEmpty()) {
      sender.sendMessage(text("Could not find a map by the name of ", NamedTextColor.RED)
          .append(text(mapName, NamedTextColor.YELLOW))
          .append(text(".")));
      return;
    }
    VariorumMap map = maybeMap.get();

    var msg = text()
        .append(text("Map: ").color(NamedTextColor.YELLOW))
        .append(text(map.getName()).color(NamedTextColor.GOLD))
        .append(text("\n"))
        .append(text("Authors: ").color(NamedTextColor.YELLOW))
        .append(text(StringUtils.join(map.getAuthors(), ",")).color(NamedTextColor.GOLD))
        .append(text("\n"))
        .append(text("Teams: ").color(NamedTextColor.YELLOW))
        .append(text("\n"));

    map.getTeams().forEach(team -> {
      msg.append(text(" - ").color(NamedTextColor.GRAY))
          .append(text(team.getName()).color(Colors.stringToTextColor(team.getColor())))
          .append(text("\n"));
    });

    sender.sendMessage(msg.build());
  }

  @Command("setnext|sn <map>")
  @CommandDescription("Set the next map in the rotation")
  public void setNextMap(
      final CommandSender sender,
      final @Greedy @Argument(value = "map", suggestions = "availableMaps") String mapName) {
    MatchManager mm = plugin.getMatchManager();
    MapManager mapManager = plugin.getMapManager();
    Optional<VariorumMap> maybeMap = mapManager.getMapByName(mapName);

    if (maybeMap.isEmpty()) {
      sender.sendMessage(text("Could not find a map by the name of ", NamedTextColor.RED)
          .append(text(mapName, NamedTextColor.YELLOW))
          .append(text(".")));
      return;
    }
    VariorumMap map = maybeMap.get();

    Match newMatch;
    try {
      newMatch = mm.getMatchFactory().create(map);
    } catch (Exception e) {
      sender.sendMessage(text("An error occurred while attempting to set ", NamedTextColor.RED)
          .append(text(mapName, NamedTextColor.YELLOW))
          .append(text(" as the next map.", NamedTextColor.RED)));
      Variorum.get().getLogger().log(Level.SEVERE, "Error creating match for map: " + mapName, e);
      return;
    }

    mm.getRotation().next(newMatch);

    sender.sendMessage(text("Set next map to ", NamedTextColor.YELLOW)
        .append(text(map.getName(), NamedTextColor.GOLD)));
  }

  @Suggestions("availableMaps")
  public List<String> suggestMaps(CommandContext<Player> context, String input) {
    return plugin.getMapManager().getAllMaps().values().stream()
        .map(VariorumMap::getName)
        .filter(name -> name.toLowerCase().startsWith(input.toLowerCase()))
        .toList();
  }
}
