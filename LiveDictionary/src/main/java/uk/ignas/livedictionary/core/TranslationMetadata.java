package uk.ignas.livedictionary.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TranslationMetadata {
    private List<AnswerAtTime> recentAnswers = new ArrayList<>();

    private Set<Label> labels = new HashSet<>();

    public TranslationMetadata(List<AnswerAtTime> recentAnswers) {
        this.recentAnswers = recentAnswers;
    }

    public static TranslationMetadata createEmpty() {
        return new TranslationMetadata(new ArrayList<AnswerAtTime>());
    }

    public List<AnswerAtTime> getRecentAnswers() {
        return recentAnswers;
    }

    public Set<Label> getLabels() {
        return labels;
    }
}
