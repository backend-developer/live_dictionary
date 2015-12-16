package uk.ignas.langlearn.core;

import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Questionnaire {
    private final List<Translation> questions ;
    private final Random random;

    public Questionnaire(Map<Translation, Difficulty> q, Random random) {
        this.random = random;
        this.questions = new ArrayList<>(q.keySet());
    }

    public Questionnaire(Map<Translation, Difficulty> q) {
        this(q, new Random());
    }


    public Translation getRandomTranslation() {
        int size = questions.size();

        if (size < 100) {
            return questions.get(random.nextInt(size));
        } else {
            if (random.nextInt(100) < 80) {
                return questions.get(random.nextInt(100));
            } else {
                return questions.get(random.nextInt(size));
            }
        }
    }

    public String drawQuestion() {
        if (questions.size() == 0) {
            throw new QuestionnaireException("no questions found");
        }
        return questions.get(random.nextInt(questions.size())).getOriginalWord();
    }
}
