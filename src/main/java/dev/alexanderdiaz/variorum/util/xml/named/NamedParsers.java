package dev.alexanderdiaz.variorum.util.xml.named;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.alexanderdiaz.variorum.map.MapParseException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import javax.annotation.Nullable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.w3c.dom.Element;

/**
 * Utility class for handling named XML parsers using reflection. This class helps manage methods annotated
 * with @NamedParser and provides functionality to invoke them based on XML element names.
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
                    throw new IllegalStateException(
                            String.format("Duplicate parser name '%s' found in %s", name, clazz.getName()));
                }
                parsers.put(method, name);
            }
        }

        return parsers.asMap();
    }

    /**
     * Invokes the appropriate parser method for a given XML element.
     *
     * @param instance The instance that contains the parser methods
     * @param element The XML element to parse
     * @param errorMessage Error message to use if no parser is found
     * @param args Additional arguments to pass to the parser method
     * @param <T> Expected return type of the parser
     * @return The parsed result
     * @throws IllegalArgumentException if no parser is found or parsing fails
     */
    @SuppressWarnings("unchecked")
    public static <T> T invoke(
            Object instance,
            Map<Method, Collection<String>> parsers,
            Element element,
            String errorMessage,
            Object... args) {

        Object[] fullArgs = new Object[args.length + 1];
        fullArgs[0] = element;
        System.arraycopy(args, 0, fullArgs, 1, args.length);

        for (Map.Entry<Method, Collection<String>> entry : parsers.entrySet()) {
            if (!entry.getValue().contains(element.getTagName())) {
                continue;
            }

            try {
                return (T) entry.getKey().invoke(instance, fullArgs);
            } catch (Exception e) {
                throw new MapParseException(
                        "Failed to parse element: " + element.getTagName(),
                        element.getTagName(),
                        element.getTextContent(),
                        e);
            }
        }

        throw new MapParseException(errorMessage, element.getTagName(), element.getTextContent());
    }
}
