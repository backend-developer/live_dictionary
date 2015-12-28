package uk.ignas.langlearn.core;

import java.util.*;

import static java.util.Collections.singleton;

public class Questionnaire {
    public static final int DIFFICULT_TRANSLATIONS_LIMIT = 20;
    public static final int NEWEST_100_QUESTIONS = 100;
    public static final int PROBABILITY_OF_80_PERCENT = 80;
    private List<Translation> questions;
    private final Set<Translation> difficultTranslations = new HashSet<>();
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
            throw new RuntimeException("translations containing data structure does not preserve order: " + q.getClass().getName());
        }
        this.questions = new ArrayList<>(q.keySet());
        this.difficultTranslations.clear();
        for (Translation t : q.keySet()) {
            if (q.get(t) == Difficulty.DIFFICULT) {
                questions.remove(t);
                difficultTranslations.add(t);
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
        if (difficultTranslations.size() > 0) {
            if (difficultTranslations.size() > random.nextInt(DIFFICULT_TRANSLATIONS_LIMIT)) {
                return getRandomDifficultTranslation();
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

    private Translation getRandomDifficultTranslation() {
        ArrayList<Translation> translations = new ArrayList<>(difficultTranslations);
        Collections.shuffle(translations);
        return translations.get(0);
    }

    public boolean mark(Translation translation, Difficulty difficulty) {
        if (translation.getId() == null) {
            return false;
        }
        int recordsUpdated = dao.update(translation.getId(), translation.getForeignWord(), translation.getNativeWord(), difficulty);
        reloadData();
        return recordsUpdated > 0;
    }

    public void insert(Translation translation) {
        dao.insertSingle(translation);
        reloadTranslations();
    }

    public void delete(Translation translation) {
        dao.delete(singleton(translation));
        reloadTranslations();
    }

    public boolean update(Translation translation) {
        if (translation.getId() == null) {
            return false;
        }

        boolean updatedAsDifficultCount = updateIfIsAnIdOfAnyOfTranslations(translation, difficultTranslations, Difficulty.DIFFICULT);
        boolean updatedAsEasyCount = updateIfIsAnIdOfAnyOfTranslations(translation, questions, Difficulty.EASY);
        reloadTranslations();
        return updatedAsDifficultCount || updatedAsEasyCount;
    }

    private boolean updateIfIsAnIdOfAnyOfTranslations(Translation translation, Collection<Translation> questions, Difficulty difficulty) {
        for (Translation t : questions) {
            if (Objects.equals(t.getId(), translation.getId())) {
                dao.update(translation.getId(), translation.getForeignWord(), translation.getNativeWord(), difficulty);
                return true;
            }
        }
        return false;
    }

    public void reloadData() {
        reloadTranslations();
    }
}
