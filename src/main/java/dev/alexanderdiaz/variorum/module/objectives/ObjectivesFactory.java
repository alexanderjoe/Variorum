package dev.alexanderdiaz.variorum.module.objectives;

import static dev.alexanderdiaz.variorum.map.VariorumMapFactory.getElementContext;

import dev.alexanderdiaz.variorum.map.MapParseException;
import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.ModuleFactory;
import dev.alexanderdiaz.variorum.module.objectives.monument.MonumentObjective;
import dev.alexanderdiaz.variorum.module.objectives.wool.WoolObjective;
import dev.alexanderdiaz.variorum.module.team.Team;
import dev.alexanderdiaz.variorum.module.team.TeamsModule;
import dev.alexanderdiaz.variorum.util.region.Region;
import dev.alexanderdiaz.variorum.util.xml.named.NamedParser;
import dev.alexanderdiaz.variorum.util.xml.named.NamedParsers;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ObjectivesFactory implements ModuleFactory<ObjectivesModule> {
    private static final Map<Method, Collection<String>> PARSERS = NamedParsers.getMethods(ObjectivesFactory.class);

    @Override
    public Optional<ObjectivesModule> build(Match match, Element root) {
        Element objectivesElement =
                (Element) root.getElementsByTagName("objectives").item(0);
        if (objectivesElement == null) {
            throw new MapParseException("Failed to parse objectives", "objectives", getElementContext(root));
        }

        ObjectivesModule module = new ObjectivesModule(match);

        for (Collection<String> types : PARSERS.values()) {
            for (String type : types) {
                NodeList elements = objectivesElement.getElementsByTagName(type);
                if (elements.getLength() > 0) {
                    parseObjective(match, module, (Element) elements.item(0), type);
                }
            }
        }

        return Optional.of(module);
    }

    private void parseObjective(Match match, ObjectivesModule module, Element element, String type) {
        try {
            NamedParsers.invoke(this, PARSERS, element, "Failed to parse objective type: " + type, match, module);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse objective: " + type, e);
        }
    }

    @NamedParser("monuments")
    private void parseMonuments(Element monumentsRoot, Match match, ObjectivesModule module) {
        Set<Material> allowedMaterials = parseAllowedMaterials(monumentsRoot);
        TeamsModule teamsModule = match.getRequiredModule(TeamsModule.class);

        NodeList teamMonuments = monumentsRoot.getElementsByTagName("monuments");
        for (int i = 0; i < teamMonuments.getLength(); i++) {
            Element teamMonumentsElement = (Element) teamMonuments.item(i);
            String ownerStr = teamMonumentsElement.getAttribute("owner");
            if (ownerStr.isEmpty()) {
                continue;
            }

            Optional<Team> team = teamsModule.getTeamById(ownerStr);
            if (team.isEmpty()) {
                continue;
            }

            parseTeamMonuments(match, module, teamMonumentsElement, team.get(), allowedMaterials);
        }
    }

    @NamedParser("wools")
    private void parseWools(Element woolsRoot, Match match, ObjectivesModule module) {
        NodeList teamWools = woolsRoot.getElementsByTagName("wools");
        for (int i = 0; i < teamWools.getLength(); i++) {
            Element teamWoolsElement = (Element) teamWools.item(i);

            String ownerStr = teamWoolsElement.getAttribute("owner");
            Optional<Team> team = Optional.empty();
            if (!ownerStr.isEmpty()) {
                var owner = match.getRequiredModule(TeamsModule.class)
                        .getTeamById(ownerStr)
                        .orElse(null);
                if (owner == null) {
                    continue;
                }
                team = Optional.of(owner);
            }

            NodeList wools = teamWoolsElement.getElementsByTagName("wool");
            for (int j = 0; j < wools.getLength(); j++) {
                Element woolElement = (Element) wools.item(j);
                try {
                    parseWool(woolElement, team, match, module);
                } catch (Exception e) {
                    throw new MapParseException(
                            "Failed to parse wool objective", "objectives.wools", getElementContext(woolElement), e);
                }
            }
        }
    }

    private void parseWool(Element woolElement, Optional<Team> team, Match match, ObjectivesModule module) {
        String color = woolElement.getAttribute("color");
        if (color.isEmpty()) {
            throw new MapParseException(
                    "Wool must have a color attribute", "objectives.wools", getElementContext(woolElement));
        }

        String name = woolElement.getAttribute("name");
        if (name.isEmpty()) {
            name = StringUtils.capitalize(color + " Wool");
        }

        boolean refill = true;
        if (woolElement.hasAttribute("refill")) {
            refill = Boolean.parseBoolean(woolElement.getAttribute("refill"));
        }

        boolean pickup = true;
        if (woolElement.hasAttribute("pickup")) {
            pickup = Boolean.parseBoolean(woolElement.getAttribute("pickup"));
        }

        Element destinationElement =
                (Element) woolElement.getElementsByTagName("destination").item(0);
        if (destinationElement == null) {
            throw new MapParseException(
                    "Wool must have a destination defined", "objectives.wools", getElementContext(woolElement));
        }

        Element regionElement =
                (Element) destinationElement.getElementsByTagName("*").item(0);
        if (regionElement == null) {
            throw new MapParseException(
                    "Wool destination must contain a region element",
                    "objectives.wools",
                    getElementContext(destinationElement));
        }
        Region destination = RegionFactory.parse(regionElement);

        Element sourceElement =
                (Element) woolElement.getElementsByTagName("source").item(0);
        Optional<Region> source = Optional.empty();
        if (sourceElement != null) {
            Element sourceRegionElement =
                    (Element) sourceElement.getElementsByTagName("*").item(0);
            if (sourceRegionElement != null) {
                source = Optional.of(RegionFactory.parse(sourceRegionElement));
            }
        }

        try {
            WoolObjective wool = new WoolObjective(
                    match, name, team, DyeColor.valueOf(color.toUpperCase()), destination, source, pickup, refill);
            module.addObjective(wool);
        } catch (IllegalArgumentException e) {
            throw new MapParseException(
                    "Invalid wool color: " + color, "objectives.wools", getElementContext(woolElement), e);
        }
    }

    private static Set<Material> parseAllowedMaterials(Element monumentsRoot) {
        String materialsStr = monumentsRoot.getAttribute("materials");
        return Arrays.stream(materialsStr.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .map(Material::valueOf)
                .collect(Collectors.toSet());
    }

    private static void parseTeamMonuments(
            Match match,
            ObjectivesModule module,
            Element teamMonumentsElement,
            Team team,
            Set<Material> allowedMaterials) {
        NodeList monuments = teamMonumentsElement.getElementsByTagName("monument");
        for (int j = 0; j < monuments.getLength(); j++) {
            Element monumentElement = (Element) monuments.item(j);
            String name = monumentElement.getAttribute("name");

            Region region = RegionFactory.parseRequired(monumentElement, "region");

            MonumentObjective monument = new MonumentObjective(match, name, team, region, allowedMaterials);
            module.addObjective(monument);
        }
    }
}
