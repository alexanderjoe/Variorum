package dev.alexanderdiaz.variorum.util.xml;

import dev.alexanderdiaz.variorum.Variorum;
import java.io.StringWriter;
import java.util.logging.Level;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Helper class to print a xml element to the console for debugging. */
public class XmlPrettyPrint {

    public static void prettyPrint(Element element) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(element), new StreamResult(writer));

            Variorum.get().getLogger().log(Level.INFO, "\n" + writer);

        } catch (Exception e) {
            Variorum.get().getLogger().log(Level.SEVERE, "Error pretty printing XML: " + e.getMessage());
            printElementRecursively(element, 0);
        }
    }

    public static void prettyPrintOut(XmlElement element) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(element.getElement()), new StreamResult(writer));

            System.out.println(writer);

        } catch (Exception e) {
            Variorum.get().getLogger().log(Level.SEVERE, "Error pretty printing XML: " + e.getMessage());
            printElementRecursively(element.getElement(), 0);
        }
    }

    private static void printElementRecursively(Element element, int depth) {
        String indent = "  ".repeat(depth);

        Variorum.get().getLogger().log(Level.INFO, indent + "<" + element.getTagName());
        printAttributes(element);

        NodeList children = element.getChildNodes();
        if (children.getLength() == 0) {
            Variorum.get().getLogger().log(Level.INFO, "/>");
            return;
        }

        Variorum.get().getLogger().log(Level.INFO, ">");

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                printElementRecursively((Element) child, depth + 1);
            } else if (child.getNodeType() == Node.TEXT_NODE) {
                String text = child.getTextContent().trim();
                if (!text.isEmpty()) {
                    Variorum.get().getLogger().log(Level.INFO, indent + "  " + text);
                }
            }
        }

        Variorum.get().getLogger().log(Level.INFO, indent + "</" + element.getTagName() + ">");
    }

    private static void printAttributes(Element element) {
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attr = attributes.item(i);
            Variorum.get().getLogger().log(Level.INFO, " " + attr.getNodeName() + "=\"" + attr.getNodeValue() + "\"");
        }
    }
}
