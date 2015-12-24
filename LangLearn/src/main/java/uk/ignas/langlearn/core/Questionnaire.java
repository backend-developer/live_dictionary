package uk.ignas.langlearn.core;

import java.util.*;

import static java.util.Collections.singleton;

public class Questionnaire {
    public static final int UNKNOWN_QUESTION_LIMIT = 20;
    public static final int NEWEST_100_QUESTIONS = 100;
    public static final int PROBABILITY_OF_80_PERCENT = 80;
    private List<Translation> questions;
    private final Set<Translation> unknownQuestions = new HashSet<>();
    private final Random random = new Random();
    private TranslationDao dao;

    public Questionnaire(TranslationDao dao) {
        this.dao = dao;
        //making sure data structure preserves insertion order even the code is changed
        reloadTranslations();
    }

    private void reloadTranslations() {
        LinkedHashMap<Translation, Difficulty> q = getTranslationsFromDb();
        if (!(q instanceof LinkedHashMap)) {
            throw new RuntimeException("words containing data structure does not preserve order: " + q.getClass().getName());
        }
        this.questions = new ArrayList<>(q.keySet());
        for (Translation t : q.keySet()) {
            if (q.get(t) == Difficulty.HARD) {
                markUnknown(t);
            }
        }
    }

    private LinkedHashMap<Translation, Difficulty> getTranslationsFromDb() {
        LinkedHashMap<Translation, Difficulty> allTranslations = dao.getAllTranslations();
        return reverseInsertionOrder(allTranslations);
    }

    private LinkedHashMap<Translation, Difficulty> reverseInsertionOrder(LinkedHashMap<Translation, Difficulty> allTranslations) {
        List<Translation> translations = new ArrayList<>(allTranslations.keySet());
        Collections.reverse(translations);
        LinkedHashMap<Translation, Difficulty> allTranslationsReversed = new LinkedHashMap<>();
        for (Translation translation : translations) {
            allTranslationsReversed.put(translation, allTranslations.get(translation));
        }
        return allTranslationsReversed;
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

    public boolean markUnknown(Translation translation) {
        if (translation.getId() == null) {
            return false;
        }
        questions.remove(translation);
        unknownQuestions.add(translation);
        int recordsUpdated = dao.update(translation.getId(), translation.getForeignWord(), translation.getNativeWord(), Difficulty.HARD);
        return recordsUpdated > 0;
    }

    public void insert(Translation translation) {
        dao.insertSingle(translation);
        reloadTranslations();
    }

    public void delete(Translation currentWord) {
        dao.delete(singleton(currentWord));
        reloadTranslations();
    }

    public boolean update(Translation translation) {
        if (translation.getId() == null) {
            return false;
        }

        boolean updatedWithHard = updateIfIsAnIdOfAnyOfWords(translation, unknownQuestions, Difficulty.HARD);
        boolean updatedWithEasy = updateIfIsAnIdOfAnyOfWords(translation, questions, Difficulty.EASY);
        reloadTranslations();
        return updatedWithHard || updatedWithEasy;
    }

    private boolean updateIfIsAnIdOfAnyOfWords(Translation translation, Collection<Translation> questions, Difficulty difficulty) {
        for (Translation t : questions) {
            if (Objects.equals(t.getId(), translation.getId())) {
                dao.update(translation.getId(), translation.getForeignWord(), translation.getNativeWord(), difficulty);
                return true;
            }
        }
        return false;
    }
}
