package dev.alexanderdiaz.variorum.region;

import dev.alexanderdiaz.variorum.map.VariorumMap;
import org.bukkit.util.Vector;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class RegionFactory {
    public static Region parse(Element element) {
        if (element.getTagName().equals("region")) {
            // Handle the specific case where region contains a direct block element
            NodeList blockNodes = element.getElementsByTagName("block");
            if (blockNodes.getLength() > 0) {
                VariorumMap.Point point = VariorumMap.Point.fromString(blockNodes.item(0).getTextContent().trim());
                return new BlockRegion(new Vector(point.getX(), point.getY(), point.getZ()));
            }
        }

        String type = element.getTagName().toLowerCase();

        return switch (type) {
            case "block" -> {
                VariorumMap.Point point = VariorumMap.Point.fromString(element.getTextContent().trim());
                yield new BlockRegion(new Vector(point.getX(), point.getY(), point.getZ()));
            }
            case "point" -> {
                VariorumMap.Point point = VariorumMap.Point.fromString(element.getTextContent().trim());
                yield new PointRegion(new Vector(point.getX(), point.getY(), point.getZ()));
            }
            case "box" -> {
                VariorumMap.Point center = VariorumMap.Point.fromString(element.getAttribute("center"));
                double xSize = Double.parseDouble(element.getAttribute("x"));
                double ySize = Double.parseDouble(element.getAttribute("y"));
                double zSize = Double.parseDouble(element.getAttribute("z"));
                yield new BoxRegion(
                        new Vector(center.getX(), center.getY(), center.getZ()),
                        xSize, ySize, zSize
                );
            }
            case "cuboid" -> {
                VariorumMap.Point min = VariorumMap.Point.fromString(element.getAttribute("min"));
                VariorumMap.Point max = VariorumMap.Point.fromString(element.getAttribute("max"));
                yield new CuboidRegion(
                        new Vector(min.getX(), min.getY(), min.getZ()),
                        new Vector(max.getX(), max.getY(), max.getZ())
                );
            }
            case "circle" -> {
                VariorumMap.Point center = VariorumMap.Point.fromString(element.getAttribute("center"));
                double radius = Double.parseDouble(element.getAttribute("radius"));
                yield new CircleRegion(
                        new Vector(center.getX(), center.getY(), center.getZ()),
                        radius
                );
            }
            case "cylinder" -> {
                VariorumMap.Point center = VariorumMap.Point.fromString(element.getAttribute("center"));
                double radius = Double.parseDouble(element.getAttribute("radius"));
                double height = Double.parseDouble(element.getAttribute("height"));
                yield new CylinderRegion(
                        new Vector(center.getX(), center.getY(), center.getZ()),
                        radius, height
                );
            }
            case "sphere" -> {
                VariorumMap.Point center = VariorumMap.Point.fromString(element.getAttribute("center"));
                double radius = Double.parseDouble(element.getAttribute("radius"));
                yield new SphereRegion(
                        new Vector(center.getX(), center.getY(), center.getZ()),
                        radius
                );
            }
            default -> throw new IllegalArgumentException("Unknown region type: " + type);
        };
    }
}