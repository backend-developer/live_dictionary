package uk.ignas.langlearn.core;

public enum Answer {
    INCORRECT(false),
    CORRECT(true);

    private boolean correct;

    Answer(boolean isCorrect) {
        this.correct = isCorrect;
    }

    public boolean isCorrect() {
        return correct;
    }
}
