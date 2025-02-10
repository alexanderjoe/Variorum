package dev.alexanderdiaz.variorum.module.zones;

import static dev.alexanderdiaz.variorum.map.VariorumMapFactory.getElementContext;

import dev.alexanderdiaz.variorum.map.MapParseException;
import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.ModuleFactory;
import dev.alexanderdiaz.variorum.region.Region;
import dev.alexanderdiaz.variorum.region.RegionFactoryDeprecated;
import dev.alexanderdiaz.variorum.util.xml.named.NamedParser;
import dev.alexanderdiaz.variorum.util.xml.named.NamedParsers;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ZoneFactory implements ModuleFactory<ZoneModule> {
    private static final Map<Method, Collection<String>> PARSERS = NamedParsers.getMethods(ZoneFactory.class);

    @Override
    public Optional<ZoneModule> build(Match match, Element root) {
        NodeList zonesElement = root.getElementsByTagName("zones");
        if (zonesElement.getLength() == 0) {
            return Optional.empty();
        }

        ZoneModule module = new ZoneModule(match);

        Element zones = (Element) zonesElement.item(0);
        NodeList zoneNodes = zones.getElementsByTagName("zone");
        for (int i = 0; i < zoneNodes.getLength(); i++) {
            Element zoneElement = (Element) zoneNodes.item(i);
            Zone zone = parseZone(match, zoneElement);
            module.addZone(zone);
        }

        return Optional.of(module);
    }

    private Zone parseZone(Match match, Element element) {
        String id = element.getAttribute("id");
        if (id.isEmpty()) {
            throw new MapParseException("Zone must have an id attribute", "zones", getElementContext(element));
        }

        Element regionElement = (Element) element.getElementsByTagName("region").item(0);
        if (regionElement == null) {
            throw new MapParseException("Zone must have a region element", "zones", getElementContext(element));
        }

        Region region;
        String regionRef = regionElement.getAttribute("ref");
        if (!regionRef.isEmpty()) {
            // TODO: Implement region reference lookup
            throw new MapParseException("Region references not yet implemented", "zones", getElementContext(element));
        } else {
            // Parse inline region
            Element inlineRegion =
                    (Element) regionElement.getElementsByTagName("*").item(0);
            if (inlineRegion == null) {
                throw new MapParseException(
                        "Region element must contain a region definition", "zones", getElementContext(regionElement));
            }
            region = RegionFactoryDeprecated.parse(inlineRegion);
        }

        Zone zone = new Zone(id, region);

        // Parse checks
        Element checksElement = (Element) element.getElementsByTagName("checks").item(0);
        if (checksElement != null) {
            parseChecks(match, zone, checksElement);
        }

        return zone;
    }

    private void parseChecks(Match match, Zone zone, Element checksElement) {
        NodeList checkNodes = checksElement.getChildNodes();
        for (int i = 0; i < checkNodes.getLength(); i++) {
            if (!(checkNodes.item(i) instanceof Element checkElement)) {
                continue;
            }

            try {
                NamedParsers.invoke(
                        this, PARSERS, checkElement, "Unknown check type: " + checkElement.getTagName(), match, zone);
            } catch (Exception e) {
                throw new MapParseException(
                        "Failed to parse check: " + checkElement.getTagName(),
                        "zones",
                        getElementContext(checkElement),
                        e);
            }
        }
    }

    // Example check parser - we'll add more as we implement different check types
    @NamedParser("entry-check")
    private void parseEntryCheck(Element element, Match match, Zone zone) {
        // Entry check parsing will be implemented when we add entry checks
        throw new UnsupportedOperationException("Entry checks not yet implemented");
    }
}
