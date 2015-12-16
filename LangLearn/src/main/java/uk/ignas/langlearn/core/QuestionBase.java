package uk.ignas.langlearn.core;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class QuestionBase {
    private Map<Translation, Difficulty> questions = ImmutableMap.of();

    public QuestionBase(Map<Translation, Difficulty> questions) {
        this.questions = questions;
    }

    public QuestionBase() {

    }

    public Map<Translation, Difficulty> getQuestions() {
        return questions;
    }
}
