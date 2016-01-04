package uk.ignas.langlearn.core;

import java.util.ArrayList;
import java.util.List;

public class TranslationMetadata {
    private List<DifficultyAtTime> recentDifficulty = new ArrayList<>();

    public TranslationMetadata(List<DifficultyAtTime> recentDifficulty) {
        this.recentDifficulty = recentDifficulty;
    }

    public static TranslationMetadata copy(TranslationMetadata from) {
        return new TranslationMetadata(
                new ArrayList<>(from.getRecentDifficulty())
        );
    }

    public List<DifficultyAtTime> getRecentDifficulty() {
        return recentDifficulty;
    }
}
