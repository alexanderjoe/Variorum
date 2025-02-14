package dev.alexanderdiaz.variorum.module.zones;

import static dev.alexanderdiaz.variorum.map.VariorumMapFactory.getElementContext;

import dev.alexanderdiaz.variorum.map.MapParseException;
import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.FactoryUtil;
import dev.alexanderdiaz.variorum.module.ModuleFactory;
import dev.alexanderdiaz.variorum.region.Region;
import dev.alexanderdiaz.variorum.util.xml.XmlElement;
import dev.alexanderdiaz.variorum.util.xml.named.NamedParser;
import dev.alexanderdiaz.variorum.util.xml.named.NamedParsers;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.w3c.dom.Element;

public class ZoneFactory implements ModuleFactory<ZoneModule> {
    private static final Map<Method, Collection<String>> PARSERS = NamedParsers.getMethods(ZoneFactory.class);

    @Override
    public Optional<ZoneModule> build(Match match, XmlElement root) {
        List<XmlElement> zones = root.getChildren("zones");
        if (zones.isEmpty()) {
            return Optional.empty();
        }

        ZoneModule module = new ZoneModule(match);

        for (XmlElement zone : zones) {
            Zone parsedZone = parseZone(match, zone);
            module.addZone(parsedZone);
        }

        return Optional.of(module);
    }

    private Zone parseZone(Match match, XmlElement element) {
        String id = element.getRequiredAttribute("id");
        XmlElement regionElement = element.getRequiredChild("region");

        Region region;
        Optional<String> regionRef = regionElement.getAttribute("ref");
        if (regionRef.isPresent()) {
            // TODO: Implement region reference lookup
            throw new MapParseException(
                    "Region references not yet implemented", "zones", getElementContext(regionElement.getElement()));
        } else {
            XmlElement inline = regionElement.getChildren().getFirst();
            if (inline == null) {
                throw new MapParseException(
                        "Region element must contain a region definition",
                        "zones",
                        getElementContext(regionElement.getElement()));
            }
            region = FactoryUtil.resolveRequiredRegionAs(match, Region.class, Optional.empty(), Optional.of(inline));
        }

        Zone zone = new Zone(id, region);

        // Parse checks
        XmlElement checksElement = element.getRequiredChild("checks");
        if (checksElement.getElement() != null) {
            parseChecks(match, zone, checksElement);
        }

        return zone;
    }

    private void parseChecks(Match match, Zone zone, XmlElement checksElement) {
        List<XmlElement> checkNodes = checksElement.getChildren();
        for (XmlElement checkElement : checkNodes) {
            try {
                NamedParsers.invoke(
                        this, PARSERS, checkElement, "Unknown check type: " + checkElement.getName(), match, zone);
            } catch (Exception e) {
                throw new MapParseException(
                        "Failed to parse check: " + checkElement.getName(),
                        "zones",
                        getElementContext(checkElement.getElement()),
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
