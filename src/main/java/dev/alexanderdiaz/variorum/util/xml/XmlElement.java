package dev.alexanderdiaz.variorum.util.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlElement {
  private final Element element;

  public XmlElement(Element element) {
    if (element == null) {
      throw new IllegalArgumentException("Element cannot be null");
    }
    this.element = element;
  }

  public static XmlElement from(Element element) {
    return new XmlElement(element);
  }

  public static Optional<XmlElement> firstChild(Element parent, String tagName) {
    NodeList nodes = parent.getElementsByTagName(tagName);
    if (nodes.getLength() == 0) {
      return Optional.empty();
    }
    Element child = (Element) nodes.item(0);
    return child.getParentNode() == parent ? Optional.of(new XmlElement(child)) : Optional.empty();
  }

  public boolean hasChild(String tagName) {
    NodeList childNodes = element.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node node = childNodes.item(i);
      if (node instanceof Element childElement && childElement.getTagName().equals(tagName)) {
        return true;
      }
    }
    return false;
  }

  public Optional<XmlElement> getChild(String tagName) {
    NodeList childNodes = element.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node node = childNodes.item(i);
      if (node instanceof Element childElement && childElement.getTagName().equals(tagName)) {
        return Optional.of(new XmlElement(childElement));
      }
    }
    return Optional.empty();
  }

  public Optional<String> getAttribute(String name) {
    String value = element.getAttribute(name);
    return value.isEmpty() ? Optional.empty() : Optional.of(value);
  }

  public String getRequiredAttribute(String name) {
    return getAttribute(name)
        .orElseThrow(
            () -> new IllegalArgumentException("Required attribute '" + name + "' missing"));
  }

  public int getIntAttribute(String name, int defaultValue) {
    return getAttribute(name).map(Integer::parseInt).orElse(defaultValue);
  }

  public double getDoubleAttribute(String name, double defaultValue) {
    return getAttribute(name).map(Double::parseDouble).orElse(defaultValue);
  }

  public boolean getBooleanAttribute(String name, boolean defaultValue) {
    return getAttribute(name).map(Boolean::parseBoolean).orElse(defaultValue);
  }

  public boolean hasAttribute(String name) {
    return element.hasAttribute(name);
  }

  /**
   * Gets all direct child elements under the first element with the specified tag name.
   *
   * @param tagName The tag name of the container element to find children under
   * @return List of XmlElements representing the direct children of the first matching container
   *     element, or an empty list if no matching container is found
   */
  public List<XmlElement> getChildren(String tagName) {
    NodeList childNodes = element.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node node = childNodes.item(i);
      if (node instanceof Element childElement && childElement.getTagName().equals(tagName)) {
        List<XmlElement> children = new ArrayList<>();
        NodeList containerChildren = childElement.getChildNodes();

        for (int j = 0; j < containerChildren.getLength(); j++) {
          Node containerChild = containerChildren.item(j);
          if (containerChild instanceof Element) {
            children.add(new XmlElement((Element) containerChild));
          }
        }

        return children;
      }
    }

    return Collections.emptyList();
  }

  public List<XmlElement> getChildren() {
    List<XmlElement> children = new ArrayList<>();

    NodeList childNodes = element.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node node = childNodes.item(i);
      if (node instanceof Element childElement) {
        children.add(new XmlElement(childElement));
      }
    }

    return children;
  }

  public List<XmlElement> getChildrenByTag(String tag) {
    return getChildren().stream()
        .filter(xmlElement -> xmlElement.getName().equalsIgnoreCase(tag))
        .toList();
  }

  public Optional<XmlElement> getFirstChildByTag(String tag) {
    return Optional.of(getChildrenByTag(tag).getFirst());
  }

  public Optional<XmlElement> getFirstChild(String tagName) {
    return getChildren(tagName).stream().findFirst();
  }

  public XmlElement getRequiredChild(String tagName) {
    return getFirstChild(tagName)
        .orElseThrow(
            () -> new IllegalArgumentException("Required child '" + tagName + "' missing"));
  }

  public String getTextContent() {
    return element.getTextContent().trim();
  }

  public String getName() {
    return element.getTagName();
  }

  public Optional<XmlElement> getParent() {
    Node parent = element.getParentNode();
    return parent instanceof Element
        ? Optional.of(new XmlElement((Element) parent))
        : Optional.empty();
  }

  public Element getElement() {
    return element;
  }
}
