package dev.alexanderdiaz.variorum.module.regions;

import dev.alexanderdiaz.variorum.map.VariorumMap.Point;
import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.match.registry.RegisteredObject;
import dev.alexanderdiaz.variorum.module.Module;
import dev.alexanderdiaz.variorum.module.ModuleFactory;
import dev.alexanderdiaz.variorum.region.*;
import dev.alexanderdiaz.variorum.util.xml.XmlElement;
import dev.alexanderdiaz.variorum.util.xml.named.NamedParser;
import dev.alexanderdiaz.variorum.util.xml.named.NamedParsers;
import java.lang.reflect.Method;
import java.util.*;
import org.bukkit.util.Vector;

/*
 * This factory does not itself create or load a module, but rather loads the regions
 * from the map xml file into the match registry where they can be referenced by other modules.
 */
public class RegionFactory implements ModuleFactory<Module> {
    private static final Map<Method, Collection<String>> PARSERS = new HashMap<>();

    public RegionFactory() {
        PARSERS.putAll(NamedParsers.getMethods(RegionFactory.class));
    }

    @Override
    public Optional<Module> build(Match match, XmlElement root) throws Exception {
        List<XmlElement> regions = root.getChildren("regions");

        if (regions.isEmpty()) {
            return Optional.empty();
        }

        for (XmlElement region : regions) {
            String id = region.getRequiredAttribute("id");
            Region parsedRegion = parseRegion(match, region);
            match.getRegistry().register(new RegisteredObject<>(id, parsedRegion));
        }

        return Optional.empty();
    }

    public Region parseRegion(Match match, XmlElement element) {
        return NamedParsers.invoke(this, PARSERS, element, "Unknown region type.", new Object[] {match});
    }

    public <T extends Region> Region parseRegionAs(Match match, XmlElement element, Class<T> type) {
        if (element.getName().equalsIgnoreCase("region")
                && element.getChildren().isEmpty()) {
            return parseRegionId(element, match);
        }
        // place to add additional region parsing logic in the future like joins

        return NamedParsers.invoke(this, PARSERS, element, "Unknown region type.", new Object[] {match});
    }

    @NamedParser("region")
    public Region parseRegionId(XmlElement element, Match match) {
        String id = element.getRequiredAttribute("id");
        return match.getRegistry().get(Region.class, id, true).get();
    }

    @NamedParser("block")
    public Region parseBlock(XmlElement element, Match match) {
        Vector vector = Point.getVector(element.getTextContent().trim());
        return new BlockRegion(vector);
    }

    @NamedParser("point")
    public Region parsePoint(XmlElement element, Match match) {
        Vector vector = Point.getVector(element.getTextContent().trim());
        return new PointRegion(vector);
    }

    @NamedParser("box")
    public Region parseBox(XmlElement element, Match match) {
        Vector center = Point.getVector(element.getRequiredAttribute("center"));
        int x = Integer.parseInt(element.getRequiredAttribute("x"));
        int y = Integer.parseInt(element.getRequiredAttribute("y"));
        int z = Integer.parseInt(element.getRequiredAttribute("z"));

        return new BoxRegion(center, x, y, z);
    }

    @NamedParser("cuboid")
    public Region parseCuboid(XmlElement element, Match match) {
        Vector min = Point.getVector(element.getRequiredAttribute("min"));
        Vector max = Point.getVector(element.getRequiredAttribute("max"));

        return new CuboidRegion(min, max);
    }

    @NamedParser("circle")
    public Region parseCircle(XmlElement element, Match match) {
        Vector center = Point.getVector(element.getRequiredAttribute("center"));
        int radius = Integer.parseInt(element.getRequiredAttribute("radius"));

        return new CircleRegion(center, radius);
    }

    @NamedParser("cylinder")
    public Region parseCylinder(XmlElement element, Match match) {
        Vector center = Point.getVector(element.getRequiredAttribute("center"));
        int radius = Integer.parseInt(element.getRequiredAttribute("radius"));
        int height = Integer.parseInt(element.getRequiredAttribute("height"));

        return new CylinderRegion(center, radius, height);
    }

    @NamedParser("sphere")
    public Region parseSphere(XmlElement element, Match match) {
        Vector center = Point.getVector(element.getRequiredAttribute("center"));
        int radius = Integer.parseInt(element.getRequiredAttribute("radius"));

        return new SphereRegion(center, radius);
    }
}
