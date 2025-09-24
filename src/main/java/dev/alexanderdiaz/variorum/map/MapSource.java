package dev.alexanderdiaz.variorum.map;

import java.io.File;
import lombok.Getter;

public class MapSource {
  @Getter
  private final File folder;

  @Getter
  private final File xmlFile;

  public MapSource(File folder, File xmlFile) {
    this.folder = folder;
    this.xmlFile = xmlFile;
  }
}
