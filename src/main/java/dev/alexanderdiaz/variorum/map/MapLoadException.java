package dev.alexanderdiaz.variorum.map;

/** Exception class for when there is an error loading a {@link VariorumMap}. */
public class MapLoadException extends RuntimeException {
  public MapLoadException(String message, Throwable cause) {
    super(message, cause);
  }
}
