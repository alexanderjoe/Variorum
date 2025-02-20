package dev.alexanderdiaz.variorum.util.xml;

import static org.junit.jupiter.api.Assertions.*;

import java.io.StringReader;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

class XmlElementTest {

    @Test
    void getChildren_shouldReturnDirectChildrenOfRegions() throws Exception {
        // Arrange
        String xml =
                """
            <?xml version="1.0" encoding="UTF-8"?>
            <map name="Test World">
                <spawns>
                    <spawn team="red" loadout="default">
                        <regions yaw="0">
                            <point>0.5,66.0,-7.5</point>
                        </regions>
                    </spawn>
                    <spawn team="blue" loadout="default">
                        <regions yaw="180">
                            <point>0.5,66.0,8.5</point>
                        </regions>
                    </spawn>
                </spawns>
                <regions>
                    <point id="region-1">0.5,66,8.5</point>
                    <cuboid min="0,0,0" max="50,50,50" id="region-2" />
                </regions>
            </map>
            """;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xml)));
        XmlElement root = XmlElement.from(document.getDocumentElement());

        // Act
        List<XmlElement> regions = root.getChildren("regions");

        // Assert
        assertEquals(2, regions.size());

        XmlElement point = regions.get(0);
        assertEquals("point", point.getName());
        assertEquals("region-1", point.getRequiredAttribute("id"));

        XmlElement cuboid = regions.get(1);
        assertEquals("cuboid", cuboid.getName());
        assertEquals("region-2", cuboid.getRequiredAttribute("id"));
    }
}
