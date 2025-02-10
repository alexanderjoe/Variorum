package dev.alexanderdiaz.variorum.util.xml;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A wrapper around {@link NodeList} that provides more convenient iteration methods and type safety. Filters
 * non-Element nodes automatically.
 */
public class XmlList implements Iterable<Element> {
    private final NodeList nodeList;

    public XmlList(NodeList nodeList) {
        this.nodeList = nodeList;
    }

    /**
     * Creates a stream of Elements from the NodeList.
     *
     * @return A stream containing only Element nodes
     */
    public Stream<Element> stream() {
        return StreamSupport.stream(
                Spliterators.spliterator(iterator(), nodeList.getLength(), Spliterator.ORDERED | Spliterator.SIZED),
                false);
    }

    /**
     * Gets the number of Element nodes in the list. Note: This may be less than nodeList.getLength() as it only counts
     * Element nodes.
     *
     * @return The number of Element nodes
     */
    public int size() {
        int count = 0;
        for (int i = 0; i < nodeList.getLength(); i++) {
            if (nodeList.item(i) instanceof Element) {
                count++;
            }
        }
        return count;
    }

    /**
     * Gets an Element at the specified index, skipping non-Element nodes.
     *
     * @param index The index of the Element to retrieve
     * @return The Element at that index
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public Element get(int index) {
        int count = 0;
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element) {
                if (count == index) {
                    return (Element) node;
                }
                count++;
            }
        }
        throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
    }

    /**
     * Creates a new wrapper around a NodeList.
     *
     * @param nodeList The NodeList to wrap
     * @return A new NodeListWrapper instance
     */
    public static XmlList of(NodeList nodeList) {
        return new XmlList(nodeList);
    }

    @Override
    public Iterator<Element> iterator() {
        return new Iterator<>() {
            private int index = 0;
            private int count = 0;

            @Override
            public boolean hasNext() {
                while (index < nodeList.getLength()) {
                    if (nodeList.item(index) instanceof Element) {
                        return true;
                    }
                    index++;
                }
                return false;
            }

            @Override
            public Element next() {
                while (index < nodeList.getLength()) {
                    Node node = nodeList.item(index++);
                    if (node instanceof Element element) {
                        count++;
                        return element;
                    }
                }
                throw new NoSuchElementException();
            }
        };
    }
}
