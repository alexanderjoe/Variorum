package dev.alexanderdiaz.variorum.module;

import dev.alexanderdiaz.variorum.map.MapParseException;
import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.regions.RegionFactory;
import dev.alexanderdiaz.variorum.region.Region;
import dev.alexanderdiaz.variorum.util.xml.XmlElement;
import java.util.Optional;

public class FactoryUtil {

  @SuppressWarnings("unchecked")
  public static <T extends Region> Optional<T> resolveRegionAs(
      Match match, Class<T> type, Optional<String> regionId, Optional<XmlElement> inlineRegion) {
    Optional<Region> region = Optional.empty();

    if (regionId.isPresent()) {
      region = match.getRegistry().get(Region.class, regionId.get(), true);
    } else if (inlineRegion.isPresent()) {
      region = Optional.of(match
          .getFactory()
          .getFactory(RegionFactory.class)
          .parseRegionAs(match, inlineRegion.get(), type));
    }

    if (region.isPresent()) {
      if (!type.isAssignableFrom(region.get().getClass())) {
        String error = "Region type mismatch. Expected \"" + type.getSimpleName() + "\" but got \""
            + region.get().getClass().getSimpleName() + "\".";
        throw new MapParseException(error, "region");
      }
    }

    return (Optional<T>) region;
  }

  public static <T extends Region> T resolveRequiredRegionAs(
      Match match, Class<T> type, Optional<String> regionId, Optional<XmlElement> inlineRegion) {
    Optional<T> region = resolveRegionAs(match, type, regionId, inlineRegion);
    if (region.isPresent()) {
      return region.get();
    }
    throw new MapParseException("Missing required region.");
  }

  @SuppressWarnings("unchecked")
  public static <T extends Region> Optional<T> resolveRegion(
      Match match, Class<T> type, String regionId, Optional<XmlElement> inlineRegion) {
    Optional<Region> region = Optional.empty();

    if (!regionId.isEmpty()) {
      region = match.getRegistry().get(Region.class, regionId, true);
    } else if (inlineRegion.isPresent()) {
      region = Optional.of(match
          .getFactory()
          .getFactory(RegionFactory.class)
          .parseRegion(match, inlineRegion.get()));
    }

    if (region.isPresent()) {
      if (!type.isAssignableFrom(region.get().getClass())) {
        String error = "Region type mismatch. Expected \"" + type.getSimpleName() + "\" but got \""
            + region.get().getClass().getSimpleName() + "\".";
        throw new MapParseException(error, "region", regionId);
      }
    }

    return (Optional<T>) region;
  }
}
