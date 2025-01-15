package dev.alexanderdiaz.variorum.region;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

class RegionFactoryTest {

    private static Element root;

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        try {
            File file = new File("src/test/resources/region-test.xml");
            Document doc = createSecureDocumentBuilder().parse(file);
            doc.getDocumentElement().normalize();
            root = doc.getDocumentElement();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Test
    void testThatFactoryCanParseRegionGivenValidInputXml() throws Exception {
        NodeList regionNode = root.getElementsByTagName("region");
        List<Region> regions = new ArrayList<>();

        for (int i = 0; i < regionNode.getLength(); i++) {
            Element regionElement = (Element) regionNode.item(i);
            Region region = RegionFactory.parse(regionElement);
            regions.add(region);
        }

        assertEquals(7, regions.size(), "Wrong number of regions parsed.");
        var last = regions.getLast();
        assertEquals(SphereRegion.class, last.getClass(), "Wrong class type of last region.");
        assertEquals(new SphereRegion(new Vector(0, 64, 0), 4), last, "Region not equal.");
    }

    @Test
    void testThatFactoryFailsToParseRegionGivenInvalidInputXml() {
        Element invalidRegion = (Element) root.getElementsByTagName("invalid-region").item(0);
        assertThrows(IllegalArgumentException.class, () -> RegionFactory.parse(invalidRegion));
    }

    private static DocumentBuilder createSecureDocumentBuilder() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        return factory.newDocumentBuilder();
    }
}
