package uk.ignas.langlearn.core;

import java.util.Date;

public class AnswerAtTime {
    private Date timepoint;
    private Answer answer;

    public AnswerAtTime(Answer answer, Date timepoint) {
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
