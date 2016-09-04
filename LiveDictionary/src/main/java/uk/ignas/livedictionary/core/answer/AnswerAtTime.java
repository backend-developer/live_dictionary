package uk.ignas.livedictionary.core.answer;

import java.util.Date;

public class AnswerAtTime {
    private Date timepoint;
    private Answer answer;
    private final Feedback feedback;

    public AnswerAtTime(Answer answer, Date timepoint) {
        this(answer, timepoint, null);
    }

    public AnswerAtTime(Answer answer, Date timepoint, Feedback feedback) {
        this.timepoint = timepoint;
        this.answer = answer;
        this.feedback = feedback;
    }

    public Date getTimepoint() {
        return timepoint;
    }

    public Answer getAnswer() {
        return answer;
    }

    public Feedback getFeedback() {
        return feedback;
    }
}
