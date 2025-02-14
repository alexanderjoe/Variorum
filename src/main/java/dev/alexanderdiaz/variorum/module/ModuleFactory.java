package dev.alexanderdiaz.variorum.module;

import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.util.xml.XmlElement;
import java.util.Optional;

/**
 * Interface for factories that create modules from XML configuration.
 *
 * @param <T> The type of module this factory creates
 */
public interface ModuleFactory<T extends Module> {
    /**
     * Builds a module from the given XML configuration.
     *
     * @param match The match this module will be part of
     * @param root The root XML element containing the module's configuration
     * @return An Optional containing the built module, or empty if the module should not be created
     * @throws Exception if there is an error building the module
     */
    Optional<T> build(Match match, XmlElement root) throws Exception;
}
