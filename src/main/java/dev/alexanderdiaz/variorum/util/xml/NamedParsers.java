package dev.alexanderdiaz.variorum.util.xml;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.w3c.dom.Element;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

/**
 * Utility class for handling named XML parsers using reflection.
 * This class helps manage methods annotated with @NamedParser and provides
 * functionality to invoke them based on XML element names.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NamedParsers {

    /**
     * Collects all methods annotated with @NamedParser in a given class.
     *
     * @param clazz The class to scan for parser methods
     * @return A map of methods to their associated parser names
     * @throws IllegalStateException if a parser name is registered multiple times
     */
    public static Map<Method, Collection<String>> getMethods(final Class<?> clazz) {
        final Multimap<Method, String> parsers = HashMultimap.create();

        for (final Method method : clazz.getDeclaredMethods()) {
            @Nullable final NamedParser parser = method.getAnnotation(NamedParser.class);
            if (parser == null) {
                continue;
            }

            method.setAccessible(true);
            for (String name : parser.value()) {
                if (parsers.containsValue(name)) {
                    throw new IllegalStateException(String.format(
                            "Duplicate parser name '%s' found in %s",
                            name,
                            clazz.getName()
                    ));
                }
                parsers.put(method, name);
            }
        }

        return parsers.asMap();
    }

    /**
     * Invokes the appropriate parser method for a given XML element.
     *
     * @param parsers Map of parser objects, methods, and their associated names
     * @param element The XML element to parse
     * @param errorMessage Error message to use if no parser is found
     * @param args Arguments to pass to the parser method
     * @param <T> Expected return type of the parser
     * @return The parsed result
     * @throws IllegalArgumentException if no parser is found or parsing fails
     */
    @SuppressWarnings("unchecked")
    public static <T> T invoke(
            Table<Object, Method, Collection<String>> parsers,
            Element element,
            String errorMessage,
            Object... args) {

        for (Table.Cell<Object, Method, Collection<String>> cell : parsers.cellSet()) {
            if (!cell.getValue().contains(element.getTagName())) {
                continue;
            }

            try {
                return (T) cell.getColumnKey().invoke(cell.getRowKey(), args);
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to parse element: " + element.getTagName(), e);
            }
        }

        throw new IllegalArgumentException(errorMessage);
    }
}