package dev.alexanderdiaz.variorum.module.loadouts.types;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

public record LoadoutEffect(String type, @Getter String duration) {

  @Override
  public String type() {
    return StringUtils.replace(type.trim(), " ", "_");
  }
}
