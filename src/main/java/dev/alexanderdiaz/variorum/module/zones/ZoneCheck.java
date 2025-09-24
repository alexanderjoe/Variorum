package dev.alexanderdiaz.variorum.module.zones;

/*
 * Base interface for all zone checks.
 */
public interface ZoneCheck {
  /** Called when this check is enabled. */
  void enable();

  /** Called when this check is disabled. */
  void disable();

  /** Get the type of check this is. */
  CheckType getType();
}
