package dev.alexanderdiaz.variorum.module.zones;

/** Base class for zone checks that need to reference the zone. */
public abstract class AbstractZoneCheck implements ZoneCheck {
  protected final Zone zone;

  protected AbstractZoneCheck(Zone zone) {
    this.zone = zone;
  }
}
