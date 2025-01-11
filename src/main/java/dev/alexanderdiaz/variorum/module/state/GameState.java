package dev.alexanderdiaz.variorum.module.state;

public enum GameState {
    WAITING,     // Waiting for players to join
    COUNTDOWN,   // Counting down to start
    PLAYING,     // Match is in progress
    ENDED        // Match has ended
}
