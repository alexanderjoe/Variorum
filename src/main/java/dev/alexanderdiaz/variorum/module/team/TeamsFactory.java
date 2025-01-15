package dev.alexanderdiaz.variorum.module.team;

import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.ModuleFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class TeamsFactory implements ModuleFactory<TeamsModule> {
    @Override
    public Optional<TeamsModule> build(Match match, Element root) {
        NodeList teamNodes = root.getElementsByTagName("team");
        if (teamNodes.getLength() == 0) {
            return Optional.empty();
        }

        List<Team> teams = new ArrayList<>();
        for (int i = 0; i < teamNodes.getLength(); i++) {
            Element teamElement = (Element) teamNodes.item(i);

            String id = teamElement.getAttribute("id");
            String name = teamElement.getTextContent();
            String color = teamElement.getAttribute("color");

            Team team = new Team(id, name, color);

            match.getRegistry().register(team);
            teams.add(team);
        }

        return Optional.of(new TeamsModule(match, teams));
    }
}
