package dev.alexanderdiaz.variorum.module;

public class ModuleBuildException extends Exception {
  public ModuleBuildException(ModuleFactory factory, Exception e) {
    super(factory.getClass().getSimpleName() + " failed to build: " + e.getMessage(), e);
  }

  public ModuleBuildException(ModuleFactory factory, String message) {
    super(factory.getClass().getSimpleName() + " failed to build: " + message);
  }
}
