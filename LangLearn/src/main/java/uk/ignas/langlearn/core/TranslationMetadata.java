package uk.ignas.langlearn.core;

/**
 * Created by ignas on 12/28/15.
 */
public class TranslationMetadata {
    private Difficulty difficulty;

    public TranslationMetadata(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }
}
