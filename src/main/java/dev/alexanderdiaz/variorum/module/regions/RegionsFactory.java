package dev.alexanderdiaz.variorum.module.regions;

import dev.alexanderdiaz.variorum.map.VariorumMap;
import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.match.registry.RegisteredObject;
import dev.alexanderdiaz.variorum.module.Module;
import dev.alexanderdiaz.variorum.module.ModuleFactory;
import dev.alexanderdiaz.variorum.util.region.Region;
import dev.alexanderdiaz.variorum.util.region.shapes.*;
import dev.alexanderdiaz.variorum.util.xml.named.NamedParser;
import dev.alexanderdiaz.variorum.util.xml.named.NamedParsers;
import org.bukkit.util.Vector;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class RegionsFactory implements ModuleFactory<Module> {
    private static final Map<Method, Collection<String>> PARSERS = NamedParsers.getMethods(RegionsFactory.class);

    @Override
    public Optional<Module> build(Match match, Element root) throws Exception {
        NodeList elements = root.getElementsByTagName("regions");

        if (elements.getLength() == 0) {
            return Optional.empty();
        }

        for (int i = 0; i < elements.getLength(); i++) {
            Element child = (Element) elements.item(i);
            String id = child.getAttribute("id");
            Region region = parseRegion(match, child);
            match.getRegistry().register(new RegisteredObject<>(id, region));
        }

        return Optional.empty();
    }

    public Region parseRegion(Match match, Element element) {
        return NamedParsers.invoke(this, PARSERS, element, "Unknown region type: " + element.getTagName());
    }

    @NamedParser("block")
    private static Region parseBlock(Element element) {
        VariorumMap.Point point =
                VariorumMap.Point.fromString(element.getTextContent().trim());
        return new BlockRegion(new Vector(point.getX(), point.getY(), point.getZ()));
    }

    @NamedParser("point")
    private static Region parsePoint(Element element) {
        VariorumMap.Point point =
                VariorumMap.Point.fromString(element.getTextContent().trim());
        return new PointRegion(new Vector(point.getX(), point.getY(), point.getZ()));
    }

    @NamedParser("box")
    private static Region parseBox(Element element) {
        VariorumMap.Point center = VariorumMap.Point.fromString(element.getAttribute("center"));
        double xSize = Double.parseDouble(element.getAttribute("x"));
        double ySize = Double.parseDouble(element.getAttribute("y"));
        double zSize = Double.parseDouble(element.getAttribute("z"));

        return new BoxRegion(new Vector(center.getX(), center.getY(), center.getZ()), xSize, ySize, zSize);
    }

    @NamedParser("cuboid")
    private static Region parseCuboid(Element element) {
        VariorumMap.Point min = VariorumMap.Point.fromString(element.getAttribute("min"));
        VariorumMap.Point max = VariorumMap.Point.fromString(element.getAttribute("max"));

        return new CuboidRegion(
                new Vector(min.getX(), min.getY(), min.getZ()), new Vector(max.getX(), max.getY(), max.getZ()));
    }

    @NamedParser("circle")
    private static Region parseCircle(Element element) {
        VariorumMap.Point center = VariorumMap.Point.fromString(element.getAttribute("center"));
        double radius = Double.parseDouble(element.getAttribute("radius"));

        return new CircleRegion(new Vector(center.getX(), center.getY(), center.getZ()), radius);
    }

    @NamedParser("cylinder")
    private static Region parseCylinder(Element element) {
        VariorumMap.Point center = VariorumMap.Point.fromString(element.getAttribute("center"));
        double radius = Double.parseDouble(element.getAttribute("radius"));
        double height = Double.parseDouble(element.getAttribute("height"));

        return new CylinderRegion(new Vector(center.getX(), center.getY(), center.getZ()), radius, height);
    }

    @NamedParser("sphere")
    private static Region parseSphere(Element element) {
        VariorumMap.Point center = VariorumMap.Point.fromString(element.getAttribute("center"));
        double radius = Double.parseDouble(element.getAttribute("radius"));

        return new SphereRegion(new Vector(center.getX(), center.getY(), center.getZ()), radius);
    }
}
