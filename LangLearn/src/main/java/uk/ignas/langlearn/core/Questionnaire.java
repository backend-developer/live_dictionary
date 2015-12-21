package uk.ignas.langlearn.core;

import java.util.*;

public class Questionnaire {
    public static final int UNKNOWN_QUESTION_LIMIT = 20;
    public static final int NEWEST_100_QUESTIONS = 100;
    public static final int PROBABILITY_OF_80_PERCENT = 80;
    private final List<Translation> questions ;
    private final Set<Translation> unknownQuestions = new HashSet<>();
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
        for (Translation t: q.keySet()) {
            if (q.get(t) == Difficulty.HARD) {
                markUnknown(t);
            }
        }
    }

    public Translation getRandomTranslation() {
        int size = questions.size();
        if (questions.size() == 0) {
            throw new QuestionnaireException("no questions found");
        }
        if (unknownQuestions.size() > 0) {
            if (unknownQuestions.size() > random.nextInt(UNKNOWN_QUESTION_LIMIT)) {
                return getRandomUnknownQuestion();
            }
        }
        if (size <= NEWEST_100_QUESTIONS) {
            return questions.get(random.nextInt(size));
        } else {
            if (is80PercentOfTimes()) {
                return getOneOfTheNewest100Questions();
            } else {
                return getQuestionNotOutOf100Newest();
            }
        }
    }

    private boolean is80PercentOfTimes() {
        return random.nextInt(100) < PROBABILITY_OF_80_PERCENT;
    }

    private Translation getOneOfTheNewest100Questions() {
        return questions.get(random.nextInt(NEWEST_100_QUESTIONS));
    }

    private Translation getQuestionNotOutOf100Newest() {
        int randomGreaterOrEqualsTo100 = random.nextInt(questions.size() - NEWEST_100_QUESTIONS) + NEWEST_100_QUESTIONS;
        return questions.get(randomGreaterOrEqualsTo100);
    }

    private Translation getRandomUnknownQuestion() {
        ArrayList<Translation> translations = new ArrayList<>(unknownQuestions);
        Collections.shuffle(translations);
        return translations.get(0);
    }

    public void markKnown(Translation translation) {

    }

    public void markUnknown(Translation translation) {
        questions.remove(translation);
        unknownQuestions.add(translation);
    }

    public Set<Translation> getUnknownQuestions() {
        return unknownQuestions;
    }
}
