package uk.ignas.langlearn.core;

import java.util.ArrayList;
import java.util.List;

public class TranslationMetadata {
    private Difficulty difficulty;
    private List<DifficultyAtTime> recentDifficulty = new ArrayList<>();

    public TranslationMetadata(Difficulty difficulty, List<DifficultyAtTime> recentDifficulty) {
        this.difficulty = difficulty;
        this.recentDifficulty = recentDifficulty;
    }

    public static TranslationMetadata copy(TranslationMetadata from) {
        return new TranslationMetadata(
                from.getDifficulty(),
                new ArrayList<>(from.getRecentDifficulty())
        );
    }

    public Difficulty getDifficulty() {
        if (recentDifficulty.size() == 0) {
            return Difficulty.EASY;
        } else {
            return recentDifficulty.get(recentDifficulty.size() - 1).getDifficulty();
        }
    }

    public List<DifficultyAtTime> getRecentDifficulty() {
        return recentDifficulty;
    }
}
