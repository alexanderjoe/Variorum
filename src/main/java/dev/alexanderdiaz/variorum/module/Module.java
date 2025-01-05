package dev.alexanderdiaz.variorum.module;

public interface Module {
    /**
     * Called when the module should be enabled.
     * This is typically when the match starts.
     */
    void enable();

    /**
     * Called when the module should be disabled.
     * This is typically when the match ends.
     */
    void disable();
}
