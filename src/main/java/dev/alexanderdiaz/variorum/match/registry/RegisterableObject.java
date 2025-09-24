package dev.alexanderdiaz.variorum.match.registry;

public interface RegisterableObject<T> {
  /**
   * Get the unique identifier of the object.
   *
   * @return The unique identifier of the object.
   */
  String getId();

  /**
   * Get the registered object.
   *
   * @return The registered object.
   */
  T getObject();
}
