package dev.alexanderdiaz.variorum.module.regions;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dev.alexanderdiaz.variorum.map.MapParseException;
import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.match.registry.MatchRegistry;
import dev.alexanderdiaz.variorum.region.BoxRegion;
import dev.alexanderdiaz.variorum.region.CylinderRegion;
import dev.alexanderdiaz.variorum.region.Region;
import dev.alexanderdiaz.variorum.region.SphereRegion;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@DisplayName("Module-RegionFactory")
class RegionFactoryTest {
    private static Element root;
    private RegionFactory regionFactory;
    private Match mockMatch;
    private MatchRegistry mockRegistry;

    @BeforeEach
    void setUp() throws Exception {
        if (root == null) {
            File file = new File("src/test/resources/region-test.xml");
            Document doc = createSecureDocumentBuilder().parse(file);
            doc.getDocumentElement().normalize();
            root = doc.getDocumentElement();
        }

        regionFactory = new RegionFactory();
        mockMatch = mock(Match.class);
        mockRegistry = mock(MatchRegistry.class);
        when(mockMatch.getRegistry()).thenReturn(mockRegistry);
    }

    @Nested
    @DisplayName("Module-RegionFactory-Parsing")
    class RegionParsingTests {
        @Test
        @DisplayName("Successfully parses all regions from valid XML")
        void testParseAllRegionsFromValidXml() throws Exception {
            when(mockRegistry.get(eq(Region.class), anyString(), anyBoolean()))
                    .thenAnswer(invocation -> Optional.of(new SphereRegion(new Vector(0, 64, 0), 4)));

            NodeList regionNode = root.getElementsByTagName("region");
            List<Region> regions = new ArrayList<>();

            for (int i = 0; i < regionNode.getLength(); i++) {
                Element regionElement = (Element) regionNode.item(i);
                Region region = regionFactory.parseRegion(mockMatch, regionElement);
                regions.add(region);
            }

            assertEquals(7, regions.size(), "Wrong number of regions parsed");
            var last = regions.getLast();
            assertEquals(SphereRegion.class, last.getClass(), "Wrong class type of last region");
            assertEquals(new SphereRegion(new Vector(0, 64, 0), 4), last, "Region not equal");
        }

        @Test
        @DisplayName("Successfully parses individual region types")
        void testParseIndividualRegionTypes() throws Exception {
            Element sphereElement = createTestElement("sphere", Map.of("center", "0,64,0", "radius", "4"));
            Region sphere = regionFactory.parseRegion(mockMatch, sphereElement);
            assertInstanceOf(SphereRegion.class, sphere, "Should parse sphere region");

            Element cylinderElement =
                    createTestElement("cylinder", Map.of("center", "0,64,0", "radius", "4", "height", "10"));
            Region cylinder = regionFactory.parseRegion(mockMatch, cylinderElement);
            assertInstanceOf(CylinderRegion.class, cylinder, "Should parse cylinder region");

            Element boxElement = createTestElement("box", Map.of("center", "0,64,0", "x", "10", "y", "20", "z", "30"));
            Region box = regionFactory.parseRegion(mockMatch, boxElement);
            assertInstanceOf(BoxRegion.class, box, "Should parse box region");
        }

        @Test
        @DisplayName("Throws exception for missing required attributes")
        void testThrowsExceptionForMissingAttributes() {
            Element invalidElement = createTestElement("sphere", Map.of());
            assertThrows(
                    MapParseException.class,
                    () -> regionFactory.parseRegion(mockMatch, invalidElement),
                    "Should throw exception for missing required attributes");
        }

        @Test
        @DisplayName("Throws exception for invalid region type")
        void testThrowsExceptionForInvalidRegionType() {
            Element invalidRegion =
                    (Element) root.getElementsByTagName("invalid-region").item(0);
            assertThrows(MapParseException.class, () -> regionFactory.parseRegion(mockMatch, invalidRegion));
        }
    }

    @Nested
    @DisplayName("Module-RegionFactory-Edge")
    class EdgeCaseTests {
        @Test
        @DisplayName("Handles negative values correctly")
        void testHandlesNegativeValues() throws Exception {
            Element element = createTestElement("sphere", Map.of("center", "-10,-20,-30", "radius", "5"));
            Region region = regionFactory.parseRegion(mockMatch, element);

            assertInstanceOf(SphereRegion.class, region);
            SphereRegion sphere = (SphereRegion) region;
            assertEquals(new Vector(-10, -20, -30), sphere.getCenter(), "Should handle negative coordinates");
        }

        @Test
        @DisplayName("Handles weirdly formatted values")
        void testHandlesWeirdFormattedValues() throws Exception {
            Element element = createTestElement("sphere", Map.of("center", " 1551   , -289,      0283  ", "radius", "5"));
            Region region = regionFactory.parseRegion(mockMatch, element);

            assertInstanceOf(SphereRegion.class, region);
            SphereRegion sphere = (SphereRegion) region;
            assertEquals(new Vector(1551, -289, 283), sphere.getCenter(), "Should handle negative coordinates");
        }

        @Test
        @DisplayName("Validates radius is positive")
        void testValidatesPositiveRadius() {
            Element element = createTestElement("sphere", Map.of("center", "0,0,0", "radius", "-5"));

            var ex = assertThrows(
                    MapParseException.class,
                    () -> regionFactory.parseRegion(mockMatch, element),
                    "Should reject negative radius");
            assertInstanceOf(
                    IllegalArgumentException.class,
                    ex.getCause().getCause(),
                    "Root cause should be IllegalArgumentException");
        }
    }

    private static DocumentBuilder createSecureDocumentBuilder() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        return factory.newDocumentBuilder();
    }

    private Element createTestElement(String type, Map<String, String> attributes) {
        Document doc = null;
        try {
            doc = createSecureDocumentBuilder().newDocument();
        } catch (Exception e) {
            fail("Failed to create test document: " + e.getMessage());
        }

        Element element = doc.createElement(type);
        attributes.forEach(element::setAttribute);
        return element;
    }
}
