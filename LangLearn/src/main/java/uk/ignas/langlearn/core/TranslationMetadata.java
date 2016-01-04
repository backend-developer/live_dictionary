package uk.ignas.langlearn.core;

import java.util.ArrayList;
import java.util.List;

public class TranslationMetadata {
    private List<AnswerAtTime> recentAnswers = new ArrayList<>();

    public TranslationMetadata(List<AnswerAtTime> recentAnswers) {
        this.recentAnswers = recentAnswers;
    }

    public static TranslationMetadata copy(TranslationMetadata from) {
        return new TranslationMetadata(
                new ArrayList<>(from.getRecentAnswers())
        );
    }

    public List<AnswerAtTime> getRecentAnswers() {
        return recentAnswers;
    }
}
