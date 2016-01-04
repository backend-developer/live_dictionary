package uk.ignas.langlearn.core;

import java.util.Date;

public class DifficultyAtTime {
    private Date timepoint;
    private Answer answer;

    public DifficultyAtTime(Answer answer, Date timepoint) {
        this.timepoint = timepoint;
        this.answer = answer;
    }

    public Date getTimepoint() {
        return timepoint;
    }

    public Answer getAnswer() {
        return answer;
    }
}
