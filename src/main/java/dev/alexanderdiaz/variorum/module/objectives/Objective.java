package dev.alexanderdiaz.variorum.module.objectives;

import dev.alexanderdiaz.variorum.module.team.Team;

public interface Objective {
  /** Called when the objective should be enabled */
  void enable();

  /** Called when the objective should be disabled */
  void disable();

  /** Gets the name of this objective */
  String getName();

  /**
   * Check if a given team is able to complete this objective
   *
   * @param team The team to compare.
   * @return If the given team can complete this objective.
   */
  boolean canComplete(Team team);

  /** Check if the objective is completed */
  boolean isCompleted();
}
