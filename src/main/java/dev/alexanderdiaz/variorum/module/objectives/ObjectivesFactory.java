package dev.alexanderdiaz.variorum.module.objectives;

import static dev.alexanderdiaz.variorum.map.VariorumMapFactory.getElementContext;

import dev.alexanderdiaz.variorum.map.MapParseException;
import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.FactoryUtil;
import dev.alexanderdiaz.variorum.module.ModuleFactory;
import dev.alexanderdiaz.variorum.module.objectives.monument.MonumentObjective;
import dev.alexanderdiaz.variorum.module.objectives.wool.WoolObjective;
import dev.alexanderdiaz.variorum.module.team.Team;
import dev.alexanderdiaz.variorum.module.team.TeamsModule;
import dev.alexanderdiaz.variorum.region.Region;
import dev.alexanderdiaz.variorum.util.xml.XmlElement;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.DyeColor;
import org.bukkit.Material;

public class ObjectivesFactory implements ModuleFactory<ObjectivesModule> {

  @Override
  public Optional<ObjectivesModule> build(Match match, XmlElement root) {
    List<XmlElement> objectives = root.getChildren("objectives");
    if (objectives.isEmpty()) {
      throw new MapParseException(
          "Failed to parse objectives", "objectives", getElementContext(root.getElement()));
    }

    ObjectivesModule module = new ObjectivesModule(match);

    for (XmlElement objRoot : objectives) {
      var tag = objRoot.getName();
      if ("monuments".equalsIgnoreCase(tag)) {
        parseMonuments(objRoot, match, module);
      } else if ("wools".equalsIgnoreCase(tag)) {
        parseWools(objRoot, match, module);
      }
    }

    return Optional.of(module);
  }

  protected void parseMonuments(XmlElement monumentsRoot, Match match, ObjectivesModule module) {
    Set<Material> allowedMaterials = parseAllowedMaterials(monumentsRoot);
    TeamsModule teamsModule = match.getRequiredModule(TeamsModule.class);

    List<XmlElement> teamMonuments = monumentsRoot.getChildren();
    for (XmlElement monument : teamMonuments) {
      Optional<String> ownerStr = monument.getAttribute("owner");
      if (ownerStr.isEmpty()) {
        continue;
      }

      Optional<Team> team = teamsModule.getTeamById(ownerStr.get());
      if (team.isEmpty()) {
        continue;
      }

      parseTeamMonuments(match, module, monument, team.get(), allowedMaterials);
    }
  }

  protected void parseWools(XmlElement element, Match match, ObjectivesModule module) {
    Optional<Team> team =
        match.getRegistry().get(Team.class, element.getRequiredAttribute("owner"), false);
    boolean pickup = element.getBooleanAttribute("pickup", true);
    boolean refill = element.getBooleanAttribute("refill", true);

    List<XmlElement> woolObjs = element.getChildren();

    for (XmlElement obj : woolObjs) {
      DyeColor color = DyeColor.valueOf(obj.getRequiredAttribute("color").toUpperCase());

      // source
      Optional<Region> source = FactoryUtil.resolveRegionAs(
          match, Region.class, obj.getAttribute("source"), obj.getFirstChild("source"));

      // destination
      Region destination = FactoryUtil.resolveRequiredRegionAs(
          match,
          Region.class,
          obj.getAttribute("destination"),
          Optional.of(obj.getRequiredChild("destination")));

      // name
      Optional<String> name = obj.getAttribute("name");
      if (name.isEmpty()) {
        name = Optional.of(StringUtils.capitalize(color.name().toLowerCase() + " Wool"));
      }

      try {
        WoolObjective wool =
            new WoolObjective(match, name.get(), team, color, destination, source, pickup, refill);
        module.addObjective(wool);
      } catch (IllegalArgumentException e) {
        throw new MapParseException(
            "Invalid wool color: " + color,
            "objectives.wools",
            getElementContext(obj.getElement()),
            e);
      }
    }
  }

  protected static Set<Material> parseAllowedMaterials(XmlElement monumentsRoot) {
    String materialsStr = monumentsRoot.getRequiredAttribute("materials");
    return Arrays.stream(materialsStr.split(","))
        .map(String::trim)
        .map(String::toUpperCase)
        .map(Material::valueOf)
        .collect(Collectors.toSet());
  }

  public static void parseTeamMonuments(
      Match match,
      ObjectivesModule module,
      XmlElement teamMonumentsElement,
      Team team,
      Set<Material> allowedMaterials) {
    List<XmlElement> monuments = teamMonumentsElement.getChildren();

    for (XmlElement monumentElement : monuments) {
      String name = monumentElement.getRequiredAttribute("name");

      XmlElement regionElement = monumentElement.getRequiredChild("region");
      Optional<String> refId = regionElement.getAttribute("ref");
      Region region = FactoryUtil.resolveRequiredRegionAs(
          match, Region.class, refId, Optional.of(regionElement));

      MonumentObjective monument =
          new MonumentObjective(match, name, team, region, allowedMaterials);
      module.addObjective(monument);
    }
  }
}
