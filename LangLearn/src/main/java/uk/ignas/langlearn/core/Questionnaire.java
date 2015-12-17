package uk.ignas.langlearn.core;

import java.util.*;

public class Questionnaire {
    private final List<Translation> questions ;
    private final Random random;

    public Questionnaire(LinkedHashMap<Translation, Difficulty> q) {
        this(q, new Random());
    }

    public Questionnaire(Map<Translation, Difficulty> q, Random random) {
        if (!(q instanceof LinkedHashMap)){
            throw new RuntimeException("words containing data structure does not preserve order: " + q.getClass().getName());
        }
        this.random = random;
        this.questions = new ArrayList<>(q.keySet());
    }

    public Translation getRandomTranslation() {
        int size = questions.size();
        if (questions.size() == 0) {
            throw new QuestionnaireException("no questions found");
        }
        if (size < 100) {
            return questions.get(random.nextInt(size));
        } else {
            if (random.nextInt(100) < 80) {
                return questions.get(random.nextInt(100));
            } else {
                return questions.get(random.nextInt(size - 100) + 100);
            }
        }
    }

    public void markKnown(Translation translation) {

    }

    public void markUnknown(Translation translation) {

    }
}
