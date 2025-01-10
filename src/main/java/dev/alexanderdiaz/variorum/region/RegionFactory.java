package dev.alexanderdiaz.variorum.region;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import dev.alexanderdiaz.variorum.map.VariorumMap;
import dev.alexanderdiaz.variorum.util.xml.NamedParser;
import dev.alexanderdiaz.variorum.util.xml.NamedParsers;
import org.bukkit.util.Vector;
import org.w3c.dom.Element;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

public class RegionFactory {
    private static final Table<Object, Method, Collection<String>> PARSERS = HashBasedTable.create();

    static {
        // Register all parser methods from this class
        Map<Method, Collection<String>> methods = NamedParsers.getMethods(RegionFactory.class);
        methods.forEach((method, names) -> PARSERS.put(RegionFactory.class, method, names));
    }

    /**
     * Parses a region from an XML element if it exists.
     *
     * @param element The XML element to parse
     * @return The parsed Region
     * @throws IllegalArgumentException if the region type is unknown
     */
    public static Region parse(Element element) {
        // Special case for nested region elements
        if (element.getTagName().equals("region")) {
            Element blockElement = (Element) element.getElementsByTagName("block").item(0);
            if (blockElement != null) {
                return parseBlock(blockElement);
            }
        }

        return NamedParsers.invoke(
                PARSERS,
                element,
                "Unknown region type: " + element.getTagName()
        );
    }

    /**
     * Parses a region from an XML element, throwing an exception if the element doesn't exist.
     *
     * @param parent The parent element containing the region
     * @param name The name of the region element to look for
     * @return The parsed Region
     * @throws IllegalArgumentException if the region element is missing or invalid
     */
    public static Region parseRequired(Element parent, String name) {
        Element element = (Element) parent.getElementsByTagName(name).item(0);
        if (element == null) {
            throw new IllegalArgumentException("Required region element '" + name + "' not found");
        }
        return parse(element);
    }

    @NamedParser("block")
    private static Region parseBlock(Element element) {
        VariorumMap.Point point = VariorumMap.Point.fromString(element.getTextContent().trim());
        return new BlockRegion(new Vector(point.getX(), point.getY(), point.getZ()));
    }

    @NamedParser("point")
    private static Region parsePoint(Element element) {
        VariorumMap.Point point = VariorumMap.Point.fromString(element.getTextContent().trim());
        return new PointRegion(new Vector(point.getX(), point.getY(), point.getZ()));
    }

    @NamedParser("box")
    private static Region parseBox(Element element) {
        VariorumMap.Point center = VariorumMap.Point.fromString(element.getAttribute("center"));
        double xSize = Double.parseDouble(element.getAttribute("x"));
        double ySize = Double.parseDouble(element.getAttribute("y"));
        double zSize = Double.parseDouble(element.getAttribute("z"));

        return new BoxRegion(
                new Vector(center.getX(), center.getY(), center.getZ()),
                xSize, ySize, zSize
        );
    }

    @NamedParser("cuboid")
    private static Region parseCuboid(Element element) {
        VariorumMap.Point min = VariorumMap.Point.fromString(element.getAttribute("min"));
        VariorumMap.Point max = VariorumMap.Point.fromString(element.getAttribute("max"));

        return new CuboidRegion(
                new Vector(min.getX(), min.getY(), min.getZ()),
                new Vector(max.getX(), max.getY(), max.getZ())
        );
    }

    @NamedParser("circle")
    private static Region parseCircle(Element element) {
        VariorumMap.Point center = VariorumMap.Point.fromString(element.getAttribute("center"));
        double radius = Double.parseDouble(element.getAttribute("radius"));

        return new CircleRegion(
                new Vector(center.getX(), center.getY(), center.getZ()),
                radius
        );
    }

    @NamedParser("cylinder")
    private static Region parseCylinder(Element element) {
        VariorumMap.Point center = VariorumMap.Point.fromString(element.getAttribute("center"));
        double radius = Double.parseDouble(element.getAttribute("radius"));
        double height = Double.parseDouble(element.getAttribute("height"));

        return new CylinderRegion(
                new Vector(center.getX(), center.getY(), center.getZ()),
                radius, height
        );
    }

    @NamedParser("sphere")
    private static Region parseSphere(Element element) {
        VariorumMap.Point center = VariorumMap.Point.fromString(element.getAttribute("center"));
        double radius = Double.parseDouble(element.getAttribute("radius"));

        return new SphereRegion(
                new Vector(center.getX(), center.getY(), center.getZ()),
                radius
        );
    }
}