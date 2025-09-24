package dev.alexanderdiaz.variorum.match.registry;

import lombok.Getter;

@Getter
public class RegisteredObject<T> implements RegisterableObject<T> {
  private final String id;
  private final T object;

  public RegisteredObject(String id, T object) {
    this.id = id;
    this.object = object;
  }
}
