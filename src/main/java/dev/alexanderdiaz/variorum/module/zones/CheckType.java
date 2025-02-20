package dev.alexanderdiaz.variorum.module.zones;

/** Enum defining all possible types of zone checks. */
public enum CheckType {
    ENTRY, // Controls who can enter/exit the zone
    BUILD, // Controls block breaking/placing
    MOVEMENT, // Modifies movement physics
    DAMAGE, // Modifies damage dealing/taking
    EFFECT, // Applies potion/status effects
    ITEM, // Controls item interactions
    ENVIRONMENT, // Modifies environmental factors
    TRIGGER, // Handles events/commands
    DISPLAY // Controls visual elements
}
