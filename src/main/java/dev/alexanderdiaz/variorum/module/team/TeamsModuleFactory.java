package dev.alexanderdiaz.variorum.module.team;

import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.ModuleFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TeamsModuleFactory implements ModuleFactory<TeamsModule> {
    @Override
    public Optional<TeamsModule> build(Match match, Element root) {
        NodeList teamNodes = root.getElementsByTagName("team");
        if (teamNodes.getLength() == 0) {
            return Optional.empty();
        }

        List<TeamsModule.Team> teams = new ArrayList<>();
        for (int i = 0; i < teamNodes.getLength(); i++) {
            Element teamElement = (Element) teamNodes.item(i);

            String id = teamElement.getAttribute("id");
            String name = teamElement.getTextContent();
            String color = teamElement.getAttribute("color");

            teams.add(new TeamsModule.Team(id, name, color));
        }

        return Optional.of(new TeamsModule(match, teams));
    }
}
