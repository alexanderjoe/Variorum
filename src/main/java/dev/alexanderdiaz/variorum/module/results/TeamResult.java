package dev.alexanderdiaz.variorum.module.results;

import lombok.Getter;

@Getter
public class TeamResult {
    private int objectivesCompleted;

    public TeamResult() {
        this.objectivesCompleted = 0;
    }

    public void incrementObjectives() {
        objectivesCompleted++;
    }
}
