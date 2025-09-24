package dev.alexanderdiaz.variorum.module.team;

import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.ModuleFactory;
import dev.alexanderdiaz.variorum.util.xml.XmlElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TeamsFactory implements ModuleFactory<TeamsModule> {
  @Override
  public Optional<TeamsModule> build(Match match, XmlElement root) {
    List<XmlElement> teams = root.getChildren("teams");

    if (teams.isEmpty()) {
      return Optional.empty();
    }

    List<Team> foundTeams = new ArrayList<>();
    for (XmlElement team : teams) {
      String id = team.getRequiredAttribute("id");
      String name = team.getTextContent();
      String color = team.getRequiredAttribute("color");

      Team newTeam = new Team(id, name, color);

      match.getRegistry().register(newTeam);
      foundTeams.add(newTeam);
    }

    return Optional.of(new TeamsModule(match, foundTeams));
  }
}
