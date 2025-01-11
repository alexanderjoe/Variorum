package dev.alexanderdiaz.variorum.map;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VariorumMapFactory {

    public static VariorumMap load(File file) {
        try {
            Document doc = createSecureDocumentBuilder().parse(file);
            return createMapFromDocument(doc);
        } catch (MapParseException e) {
            throw e;
        } catch (Exception e) {
            throw new MapLoadException("Failed to parse map file: " + file.getName(), e);
        }
    }

    public static VariorumMap load(InputStream inputStream) {
        try {
            Document doc = createSecureDocumentBuilder().parse(inputStream);
            return createMapFromDocument(doc);
        } catch (MapParseException e) {
            throw e;
        } catch (Exception e) {
            throw new MapLoadException("Failed to parse map from input stream", e);
        }
    }

    private static DocumentBuilder createSecureDocumentBuilder() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        return factory.newDocumentBuilder();
    }

    private static VariorumMap createMapFromDocument(Document doc) {
        doc.getDocumentElement().normalize();
        Element mapElement = doc.getDocumentElement();

        if (!"map".equals(mapElement.getTagName())) {
            throw new MapParseException("Root element must be <map>", "document", getElementContext(mapElement));
        }

        VariorumMap.VariorumMapBuilder builder = VariorumMap.builder();

        try {
            builder.name(getRequiredAttribute(mapElement, "name", "document"));

            try {
                builder.authors(createAuthors(doc));
            } catch (Exception e) {
                throw enhanceException(e, "authors", mapElement);
            }

            try {
                builder.teams(createTeams(doc));
            } catch (Exception e) {
                throw enhanceException(e, "teams", mapElement);
            }

            try {
                builder.spawns(createSpawns(doc));
            } catch (Exception e) {
                throw enhanceException(e, "spawns", mapElement);
            }

            return builder.build();

        } catch (MapParseException e) {
            throw e;
        } catch (Exception e) {
            throw new MapParseException(
                    "Failed to create map from document", "document", getElementContext(mapElement), e);
        }
    }

    private static List<String> createAuthors(Document doc) {
        List<String> authors = new ArrayList<>();
        NodeList authorNodes = doc.getElementsByTagName("author");

        if (authorNodes.getLength() == 0) {
            throw new MapParseException("Map must have at least one author", "authors");
        }

        for (int i = 0; i < authorNodes.getLength(); i++) {
            authors.add(authorNodes.item(i).getTextContent());
        }
        return authors;
    }

    private static List<VariorumMap.Team> createTeams(Document doc) {
        List<VariorumMap.Team> teams = new ArrayList<>();
        NodeList teamNodes = doc.getElementsByTagName("team");

        if (teamNodes.getLength() == 0) {
            throw new MapParseException("Map must have at least one team", "teams");
        }

        for (int i = 0; i < teamNodes.getLength(); i++) {
            try {
                Element teamElement = (Element) teamNodes.item(i);
                teams.add(VariorumMap.Team.builder()
                        .id(getRequiredAttribute(teamElement, "id", "teams"))
                        .color(getRequiredAttribute(teamElement, "color", "teams"))
                        .name(teamElement.getTextContent())
                        .build());
            } catch (Exception e) {
                throw new MapParseException(
                        "Failed to parse team at index " + i,
                        "teams",
                        getElementContext((Element) teamNodes.item(i)),
                        e);
            }
        }
        return teams;
    }

    private static VariorumMap.Spawns createSpawns(Document doc) {
        Element spawnsElement = getRequiredElement(doc, "spawns", "spawns", "Map must have spawns defined");

        try {
            return VariorumMap.Spawns.builder()
                    .defaultSpawn(createSpawnRegion(
                            getRequiredElement(spawnsElement, "default", "spawns", "Default spawn must be defined")))
                    .teamSpawns(createTeamSpawns(spawnsElement))
                    .build();
        } catch (MapParseException e) {
            throw e;
        } catch (Exception e) {
            throw new MapParseException(
                    "Failed to parse spawns section", "spawns", getElementContext(spawnsElement), e);
        }
    }

    private static VariorumMap.Spawns.SpawnRegion createSpawnRegion(Element element) {
        try {
            Element regionsElement =
                    getRequiredElement(element, "regions", "spawns", "Spawn must have regions defined");
            String loadout = element.getAttribute("loadout");

            return VariorumMap.Spawns.SpawnRegion.builder()
                    .yaw(Double.parseDouble(regionsElement.getAttribute("yaw")))
                    .point(VariorumMap.Point.fromString(
                            getRequiredElement(regionsElement, "point", "spawns", "Spawn region must have a point")
                                    .getTextContent()))
                    .loadout(loadout.isEmpty() ? null : loadout)
                    .build();
        } catch (NumberFormatException e) {
            throw new MapParseException("Invalid yaw value", "spawns", getElementContext(element), e);
        } catch (IllegalArgumentException e) {
            throw new MapParseException("Invalid point format", "spawns", getElementContext(element), e);
        }
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

    public static String getElementContext(Element element) {
        try {
            StringBuilder context = new StringBuilder();
            context.append("<").append(element.getTagName());

            if (element.hasAttributes()) {
                for (int i = 0; i < element.getAttributes().getLength(); i++) {
                    var attr = element.getAttributes().item(i);
                    context.append(" ")
                            .append(attr.getNodeName())
                            .append("=\"")
                            .append(attr.getNodeValue())
                            .append("\"");
                }
            }

            if (element.hasChildNodes()) {
                context.append(">...</").append(element.getTagName()).append(">");
            } else {
                context.append("/>");
            }

            return context.toString();
        } catch (Exception e) {
            return "Unable to get XML context";
        }
    }

    private static MapParseException enhanceException(Exception e, String section, Element context) {
        if (e instanceof MapParseException) {
            return (MapParseException) e;
        }
        return new MapParseException(
                "Failed to parse " + section + " section: " + e.getMessage(), section, getElementContext(context), e);
    }

    private static String getRequiredAttribute(Element element, String attribute, String section) {
        String value = element.getAttribute(attribute);
        if (value == null || value.isEmpty()) {
            throw new MapParseException(
                    "Required attribute '" + attribute + "' missing from element: " + element.getTagName(),
                    section,
                    getElementContext(element));
        }
        return value;
    }

    private static Element getRequiredElement(Element parent, String tagName, String section, String message) {
        Element element = (Element) parent.getElementsByTagName(tagName).item(0);
        if (element == null) {
            throw new MapParseException(message, section, getElementContext(parent));
        }
        return element;
    }

    private static Element getRequiredElement(Document doc, String tagName, String section, String message) {
        Element element = (Element) doc.getElementsByTagName(tagName).item(0);
        if (element == null) {
            throw new MapParseException(message, section);
        }
        return element;
    }
}
