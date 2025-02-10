package dev.alexanderdiaz.variorum.module.regions;

import dev.alexanderdiaz.variorum.map.MapParseException;
import dev.alexanderdiaz.variorum.map.VariorumMap.Point;
import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.match.registry.RegisteredObject;
import dev.alexanderdiaz.variorum.module.Module;
import dev.alexanderdiaz.variorum.module.ModuleFactory;
import dev.alexanderdiaz.variorum.region.*;
import dev.alexanderdiaz.variorum.util.xml.XmlList;
import dev.alexanderdiaz.variorum.util.xml.named.NamedParser;
import dev.alexanderdiaz.variorum.util.xml.named.NamedParsers;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.bukkit.util.Vector;
import org.w3c.dom.Element;

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
    public Optional<Module> build(Match match, Element root) throws Exception {
        XmlList regions = XmlList.of(root.getElementsByTagName("regions"));

        if (regions.size() == 0) {
            return Optional.empty();
        }

        for (Element regionElement : regions) {
            String id = regionElement.getAttribute("id");
            if (id.isEmpty()) {
                throw new MapParseException("Region must have an id attribute");
            }

            Region region = parseRegion(match, regionElement);
            match.getRegistry().register(new RegisteredObject<>(id, region));
        }

        return Optional.empty();
    }

    public Region parseRegion(Match match, Element element) {
        return NamedParsers.invoke(this, PARSERS, element, "Unknown region type.", new Object[] {match});
    }

    @NamedParser("region")
    public Region parseRegionId(Element element, Match match) {
        String id = element.getAttribute("id");
        if (id.isEmpty()) {
            throw new MapParseException("Region must have an id attribute");
        }

        return match.getRegistry().get(Region.class, id, true).get();
    }

    @NamedParser("block")
    public Region parseBlock(Element element, Match match) {
        Vector vector = Point.getVector(element.getTextContent().trim());
        return new BlockRegion(vector);
    }

    @NamedParser("point")
    public Region parsePoint(Element element, Match match) {
        Vector vector = Point.getVector(element.getTextContent().trim());
        return new PointRegion(vector);
    }

    @NamedParser("box")
    public Region parseBox(Element element, Match match) {
        Vector center = Point.getVector(element.getAttribute("center"));
        int x = Integer.parseInt(element.getAttribute("x"));
        int y = Integer.parseInt(element.getAttribute("y"));
        int z = Integer.parseInt(element.getAttribute("z"));

        return new BoxRegion(center, x, y, z);
    }

    @NamedParser("cuboid")
    public Region parseCuboid(Element element, Match match) {
        Vector min = Point.getVector(element.getAttribute("min"));
        Vector max = Point.getVector(element.getAttribute("max"));

        return new CuboidRegion(min, max);
    }

    @NamedParser("circle")
    public Region parseCircle(Element element, Match match) {
        Vector center = Point.getVector(element.getAttribute("center"));
        int radius = Integer.parseInt(element.getAttribute("radius"));

        return new CircleRegion(center, radius);
    }

    @NamedParser("cylinder")
    public Region parseCylinder(Element element, Match match) {
        Vector center = Point.getVector(element.getAttribute("center"));
        int radius = Integer.parseInt(element.getAttribute("radius"));
        int height = Integer.parseInt(element.getAttribute("height"));

        return new CylinderRegion(center, radius, height);
    }

    @NamedParser("sphere")
    public Region parseSphere(Element element, Match match) {
        Vector center = Point.getVector(element.getAttribute("center"));
        int radius = Integer.parseInt(element.getAttribute("radius"));

        return new SphereRegion(center, radius);
    }
}
