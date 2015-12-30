package uk.ignas.langlearn.core;

import java.util.Date;

/**
 * Created by ignas on 12/30/15.
 */
public class DifficultyAtTime {
    private Date timepoint;
    private Difficulty difficulty;

    public DifficultyAtTime(Date timepoint, Difficulty difficulty) {
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
