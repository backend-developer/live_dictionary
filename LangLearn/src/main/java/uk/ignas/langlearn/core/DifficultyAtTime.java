package uk.ignas.langlearn.core;

import java.util.Date;

public class DifficultyAtTime {
    private Date timepoint;
    private Difficulty difficulty;

    public DifficultyAtTime(Difficulty difficulty, Date timepoint) {
        this.timepoint = timepoint;
        this.difficulty = difficulty;
    }

    public Date getTimepoint() {
        return timepoint;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }
}
