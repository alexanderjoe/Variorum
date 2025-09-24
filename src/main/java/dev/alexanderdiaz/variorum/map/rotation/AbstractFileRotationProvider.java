package dev.alexanderdiaz.variorum.map.rotation;

import dev.alexanderdiaz.variorum.map.MapManager;
import dev.alexanderdiaz.variorum.match.MatchFactory;
import java.io.File;

public abstract class AbstractFileRotationProvider extends AbstractRotationProvider {
  protected final File file;

  public AbstractFileRotationProvider(File file, MapManager mm, MatchFactory factory) {
    super(mm, factory);
    this.file = file;
  }

  @Override
  public Rotation provideRotation() {
    return this.createRotation();
  }

  protected abstract Rotation createRotation();
}
