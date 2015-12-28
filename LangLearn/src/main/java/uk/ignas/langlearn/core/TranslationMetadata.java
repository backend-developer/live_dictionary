package uk.ignas.langlearn.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TranslationMetadata {
    private Difficulty difficulty;
    private List<Date> recentMarkingAsEasy = new ArrayList<>();

    public TranslationMetadata(Difficulty difficulty, List<Date> recentMarkingAsEasy) {
        this.difficulty = difficulty;
        this.recentMarkingAsEasy = recentMarkingAsEasy;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public List<Date> getRecentMarkingAsEasy() {
        return recentMarkingAsEasy;
    }
}
