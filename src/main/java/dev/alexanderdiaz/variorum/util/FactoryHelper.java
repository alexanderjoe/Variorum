package dev.alexanderdiaz.variorum.util;

import dev.alexanderdiaz.variorum.map.MapParseException;
import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.regions.RegionsFactory;
import dev.alexanderdiaz.variorum.util.region.Region;
import org.w3c.dom.Element;

import java.util.Optional;

import static dev.alexanderdiaz.variorum.map.VariorumMapFactory.getElementContext;

public class FactoryHelper {

    public static <T extends Region> T resolveRequiredRegion() {
        return null;
    }

    public static <T extends Region> Optional<T> resolveRegion(Match match, Class<T> type, String regionId, Optional<Element> inline) {
        Optional<Region> region = Optional.empty();

        if (regionId != null) {
            region = match.getRegistry().get(Region.class, regionId, true);
        } else if (inline.isPresent()) {
            region = Optional.of(match.getFactory().getFactory(RegionsFactory.class).parseRegion(match, inline.get()));
        }

        if (region.isPresent()) {
            if (!type.isAssignableFrom(region.get().getClass())) {
                String error = "Region type mismatch. Expected \"" + type.getSimpleName() + "\" but got \"" + region.get().getClass().getSimpleName() + "\".";
                throw new MapParseException(error, "regions", getElementContext(inline.orElse(null)));
            }
        }

        return (Optional<T>) region;
    }
}
