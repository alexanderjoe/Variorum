package dev.alexanderdiaz.variorum.module.team;

import dev.alexanderdiaz.variorum.match.registry.RegisterableObject;
import dev.alexanderdiaz.variorum.util.Colors;
import lombok.ToString;
import net.kyori.adventure.text.format.NamedTextColor;

@ToString
public class Team implements RegisterableObject<Team> {
  private final String id;
  private final String name;
  private final String color;

  public Team(String id, String name, String color) {
    this.id = id;
    this.name = name;
    this.color = color;
  }

  public NamedTextColor textColor() {
    return Colors.stringToTextColor(this.color);
  }

  public String id() {
    return id;
  }

  public String name() {
    return name;
  }

  public String color() {
    return color;
  }

  @Override
  public String getId() {
    return this.id;
  }

  @Override
  public Team getObject() {
    return this;
  }
}
