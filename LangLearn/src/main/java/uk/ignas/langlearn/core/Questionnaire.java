package uk.ignas.langlearn.core;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.singleton;

public class Questionnaire {
    public static final int DIFFICULT_TRANSLATIONS_LIMIT = 20;
    public static final int NEWEST_100_QUESTIONS = 100;
    public static final int PROBABILITY_OF_80_PERCENT = 80;
    private List<Translation> questions;
    private final Set<Translation> difficultTranslations = new HashSet<>();
    private List<Translation> veryEasyQuestions = new ArrayList<>();
    private final Random random = new Random();
    private TranslationDao dao;
    private Clock clock;

    public Questionnaire(TranslationDao dao) {
        this(dao, new Clock());
    }

    public Questionnaire(TranslationDao dao, Clock clock) {
        this.dao = dao;
        this.clock = clock;
        //making sure data structure preserves insertion order even the code is changed
        reloadTranslations();
    }

    private void reloadTranslations() {
        questions = dao.getAllTranslations();
        Collections.reverse(questions);
        this.difficultTranslations.clear();
        for (Translation t : new ArrayList<>(questions)) {
            if (t.getMetadata().getDifficulty() == Difficulty.DIFFICULT) {
                questions.remove(t);
                difficultTranslations.add(t);
            }
        }

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
        Translation translationToReturn;
        if (size <= NEWEST_100_QUESTIONS) {
            translationToReturn = questions.get(random.nextInt(size));
        } else {
            if (is80PercentOfTimes()) {
                translationToReturn = getOneOfTheNewest100Questions();
            } else {
                translationToReturn = getQuestionNotOutOf100Newest();
            }
        }
        Iterator<Date> iter = translationToReturn.getMetadata().getRecentMarkingAsEasy().iterator();
        while (iter.hasNext()) {
            if (TimeUnit.MILLISECONDS.toHours(clock.getTime().getTime() - iter.next().getTime()) >= 1) {
                iter.remove();
            }
        }
        if (translationToReturn.getMetadata().getRecentMarkingAsEasy().size() >= 3) {
            if (questions.size() == 1) {
                throw new QuestionnaireException("There are no more difficult words");
            } else {
                veryEasyQuestions.add(translationToReturn);
                questions.remove(translationToReturn);
                translationToReturn = getRandomTranslation();
            }
        }
        return translationToReturn;
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
        Translation record;
        try {
            record = dao.getById(translation.getId());
        } catch (RuntimeException e) {
            return false;
        }
        TranslationMetadata metadata = record.getMetadata();
        if (difficulty == Difficulty.EASY) {
            if (metadata.getRecentMarkingAsEasy().size() < 3) {
                metadata.getRecentMarkingAsEasy().add(clock.getTime());
            }
        } else {
            metadata.getRecentMarkingAsEasy().clear();
        }
        int recordsUpdated = dao.update(translation.getId(), translation.getForeignWord(), translation.getNativeWord(), new TranslationMetadata(difficulty, metadata.getRecentMarkingAsEasy()));
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
                Translation byId = dao.getById(t.getId());

                dao.update(translation.getId(), translation.getForeignWord(), translation.getNativeWord(), new TranslationMetadata(difficulty, byId.getMetadata().getRecentMarkingAsEasy()));
                return true;
            }
        }
        return false;
    }

    public void reloadData() {
        reloadTranslations();
    }
}
