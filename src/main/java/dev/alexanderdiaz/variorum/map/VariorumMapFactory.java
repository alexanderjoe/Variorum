package dev.alexanderdiaz.variorum.map;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VariorumMapFactory {

    public static VariorumMap load(File file) {
        try {
            Document doc = createSecureDocumentBuilder().parse(file);
            return createMapFromDocument(doc);
        } catch (Exception e) {
            throw new MapLoadException("Failed to load map from file", e);
        }
    }

    public static VariorumMap load(InputStream inputStream) {
        try {
            Document doc = createSecureDocumentBuilder().parse(inputStream);
            return createMapFromDocument(doc);
        } catch (Exception e) {
            throw new MapLoadException("Failed to load map from input stream", e);
        }
    }

    private static DocumentBuilder createSecureDocumentBuilder() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // Security features
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        return factory.newDocumentBuilder();
    }

    private static VariorumMap createMapFromDocument(Document doc) {
        doc.getDocumentElement().normalize();
        Element mapElement = doc.getDocumentElement();

        return VariorumMap.builder()
                .name(mapElement.getAttribute("name"))
                .authors(createAuthors(doc))
                .teams(createTeams(doc))
                .spawns(createSpawns(doc))
                .objectives(createObjectives(doc))
                .build();
    }

    private static List<String> createAuthors(Document doc) {
        List<String> authors = new ArrayList<>();
        NodeList authorNodes = doc.getElementsByTagName("author");
        for (int i = 0; i < authorNodes.getLength(); i++) {
            authors.add(authorNodes.item(i).getTextContent());
        }
        return authors;
    }

    private static List<VariorumMap.Team> createTeams(Document doc) {
        List<VariorumMap.Team> teams = new ArrayList<>();
        NodeList teamNodes = doc.getElementsByTagName("team");
        for (int i = 0; i < teamNodes.getLength(); i++) {
            Element teamElement = (Element) teamNodes.item(i);
            teams.add(VariorumMap.Team.builder()
                    .id(teamElement.getAttribute("id"))
                    .color(teamElement.getAttribute("color"))
                    .name(teamElement.getTextContent())
                    .build());
        }
        return teams;
    }

    private static VariorumMap.Spawns createSpawns(Document doc) {
        Element spawnsElement = (Element) doc.getElementsByTagName("spawns").item(0);

        return VariorumMap.Spawns.builder()
                .defaultSpawn(createSpawnRegion((Element) spawnsElement.getElementsByTagName("default").item(0)))
                .teamSpawns(createTeamSpawns(spawnsElement))
                .build();
    }

    private static VariorumMap.Spawns.SpawnRegion createSpawnRegion(Element element) {
        Element regionsElement = (Element) element.getElementsByTagName("regions").item(0);
        String loadout = element.getAttribute("loadout");

        return VariorumMap.Spawns.SpawnRegion.builder()
                .yaw(Double.parseDouble(regionsElement.getAttribute("yaw")))
                .point(VariorumMap.Point.fromString(
                        regionsElement.getElementsByTagName("point").item(0).getTextContent()))
                .loadout(loadout.isEmpty() ? null : loadout)
                .build();
    }

    private static List<VariorumMap.Spawns.TeamSpawn> createTeamSpawns(Element spawnsElement) {
        List<VariorumMap.Spawns.TeamSpawn> teamSpawns = new ArrayList<>();
        NodeList spawnNodes = spawnsElement.getElementsByTagName("spawn");

        for (int i = 0; i < spawnNodes.getLength(); i++) {
            Element spawnElement = (Element) spawnNodes.item(i);
            teamSpawns.add(VariorumMap.Spawns.TeamSpawn.builder()
                    .team(spawnElement.getAttribute("team"))
                    .region(createSpawnRegion(spawnElement))
                    .build());
        }

        return teamSpawns;
    }

    private static VariorumMap.Objectives createObjectives(Document doc) {
        Element objectivesElement = (Element) doc.getElementsByTagName("objectives").item(0);
        Element monumentsRoot = (Element) objectivesElement.getElementsByTagName("monuments").item(0);

        return VariorumMap.Objectives.builder()
                .monuments(VariorumMap.Objectives.Monuments.builder()
                        .materials(monumentsRoot.getAttribute("materials"))
                        .teamMonuments(createTeamMonuments(monumentsRoot))
                        .build())
                .build();
    }

    private static List<VariorumMap.Objectives.Monuments.TeamMonuments> createTeamMonuments(Element monumentsRoot) {
        List<VariorumMap.Objectives.Monuments.TeamMonuments> teamMonuments = new ArrayList<>();
        NodeList teamMonumentNodes = monumentsRoot.getElementsByTagName("monuments");

        for (int i = 0; i < teamMonumentNodes.getLength(); i++) {
            Element teamMonumentsElement = (Element) teamMonumentNodes.item(i);
            if (teamMonumentsElement.hasAttribute("owner")) {
                teamMonuments.add(createTeamMonument(teamMonumentsElement));
            }
        }

        return teamMonuments;
    }

    private static VariorumMap.Objectives.Monuments.TeamMonuments createTeamMonument(Element teamMonumentsElement) {
        List<VariorumMap.Objectives.Monuments.TeamMonuments.Monument> monuments = new ArrayList<>();
        NodeList monumentNodes = teamMonumentsElement.getElementsByTagName("monument");

        for (int i = 0; i < monumentNodes.getLength(); i++) {
            Element monumentElement = (Element) monumentNodes.item(i);
            Element regionElement = (Element) monumentElement.getElementsByTagName("region").item(0);

            monuments.add(VariorumMap.Objectives.Monuments.TeamMonuments.Monument.builder()
                    .name(monumentElement.getAttribute("name"))
                    .region(VariorumMap.Objectives.Monuments.TeamMonuments.Monument.Region.builder()
                            .block(VariorumMap.Point.fromString(
                                    regionElement.getElementsByTagName("block").item(0).getTextContent()))
                            .build())
                    .build());
        }

        return VariorumMap.Objectives.Monuments.TeamMonuments.builder()
                .owner(teamMonumentsElement.getAttribute("owner"))
                .monuments(monuments)
                .build();
    }

    public static class MapLoadException extends RuntimeException {
        public MapLoadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}