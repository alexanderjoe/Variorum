package dev.alexanderdiaz.variorum.module.objectives;

public interface Objective {
    /**
     * Called when the objective should be enabled
     */
    void enable();

    /**
     * Called when the objective should be disabled
     */
    void disable();

    /**
     * Gets the name of this objective
     */
    String getName();

    /**
     * Check if the objective is completed
     */
    boolean isCompleted();
}