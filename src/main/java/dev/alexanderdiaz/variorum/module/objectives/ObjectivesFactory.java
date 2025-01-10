package dev.alexanderdiaz.variorum.module.objectives;

import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.ModuleFactory;
import dev.alexanderdiaz.variorum.module.objectives.monument.MonumentObjective;
import dev.alexanderdiaz.variorum.module.team.Team;
import dev.alexanderdiaz.variorum.module.team.TeamsModule;
import dev.alexanderdiaz.variorum.region.Region;
import dev.alexanderdiaz.variorum.region.RegionFactory;
import org.bukkit.Material;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ObjectivesFactory implements ModuleFactory<ObjectivesModule> {
    @Override
    public Optional<ObjectivesModule> build(Match match, Element root) {
        Element objectivesElement = (Element) root.getElementsByTagName("objectives").item(0);
        if (objectivesElement == null) {
            return Optional.empty();
        }

        ObjectivesModule module = new ObjectivesModule(match);

        // Process monuments
        Element monumentsRoot = (Element) objectivesElement.getElementsByTagName("monuments").item(0);
        if (monumentsRoot != null) {
            // Parse allowed materials
            String materialsStr = monumentsRoot.getAttribute("materials");
            Set<Material> allowedMaterials = Arrays.stream(materialsStr.split(","))
                    .map(String::trim)
                    .map(String::toUpperCase)
                    .map(Material::valueOf)
                    .collect(Collectors.toSet());

            // Get teams module for reference
            TeamsModule teamsModule = match.getRequiredModule(TeamsModule.class);

            // Process team monuments
            NodeList teamMonuments = monumentsRoot.getElementsByTagName("monuments");
            for (int i = 0; i < teamMonuments.getLength(); i++) {
                Element teamMonumentsElement = (Element) teamMonuments.item(i);
                String ownerStr = teamMonumentsElement.getAttribute("owner");

                // Find team
                Optional<Team> team = teamsModule.getTeamById(ownerStr);
                if (team.isEmpty()) {
                    continue;
                }

                // Process individual monuments
                NodeList monuments = teamMonumentsElement.getElementsByTagName("monument");
                for (int j = 0; j < monuments.getLength(); j++) {
                    Element monumentElement = (Element) monuments.item(j);
                    String name = monumentElement.getAttribute("name");

                    Region region = RegionFactory.parseRequired(monumentElement, "region");

                    MonumentObjective monument = new MonumentObjective(
                            match,
                            name,
                            team.get(),
                            region,
                            allowedMaterials
                    );
                    module.addObjective(monument);
                }
            }
        }

        return Optional.of(module);
    }
}