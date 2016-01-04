package uk.ignas.langlearn.core;

import java.util.*;

import static java.util.Collections.singleton;

public class Dictionary {
    public static final int DIFFICULT_TRANSLATIONS_LIMIT = 20;
    public static final int NEWEST_100_QUESTIONS = 100;
    public static final int PROBABILITY_OF_80_PERCENT = 80;

    private List<Translation> questions;
    private final Set<Translation> difficultTranslations = new HashSet<>();
    private final Reminder reminder;
    private List<Translation> veryEasyTranslations = new ArrayList<>();
    private final Random random = new Random();
    private TranslationDao dao;
    private Clock clock;

    public Dictionary(TranslationDao dao) {
        this(dao, new Clock());
    }

    public Dictionary(TranslationDao dao, Clock clock) {
        this.dao = dao;
        this.clock = clock;
        reminder = new Reminder(clock);
        //making sure data structure preserves insertion order even the code is changed
        reloadTranslations();
    }

    private void reloadTranslations() {
        questions = dao.getAllTranslations();
        Collections.reverse(questions);
        this.difficultTranslations.clear();
        for (Translation t : new ArrayList<>(questions)) {
            if (getDifficulty(t.getMetadata()) == Difficulty.DIFFICULT) {
                questions.remove(t);
                difficultTranslations.add(t);
            }
        }
    }

    public Difficulty getDifficulty(TranslationMetadata metadata) {
        if (metadata.getRecentDifficulty().size() == 0) {
            return Difficulty.EASY;
        } else {
            return metadata.getRecentDifficulty().get(metadata.getRecentDifficulty().size() - 1).getDifficulty();
        }
    }

    public Translation getRandomTranslation() {
        if (questions.size() == 0 && difficultTranslations.size() == 0 && veryEasyTranslations.size() == 0) {
            throw new LiveDictionaryException("no questions found");
        }
        Translation translationToReturn = null;
        while (translationToReturn == null) {
            if (questions.size() == 0) {
                throw new LiveDictionaryException("There are no more difficult words");
            }
            Translation candidateTranslation = chooseTranslationPreferingDifficultOrNewer();

            if (reminder.shouldBeReminded(candidateTranslation.getMetadata())) {
                translationToReturn = candidateTranslation;
            } else {
                veryEasyTranslations.add(candidateTranslation);
                questions.remove(candidateTranslation);
            }
        }
        return translationToReturn;
    }





    private Translation chooseTranslationPreferingDifficultOrNewer() {
        Translation translationToReturn;
        if (difficultTranslations.size() > 0 && difficultTranslations.size() > random.nextInt(DIFFICULT_TRANSLATIONS_LIMIT)) {
            translationToReturn = getRandomDifficultTranslation();
        } else {
            translationToReturn = chooseTranslationPreferringNewer();
        }
        return translationToReturn;
    }

    private Translation chooseTranslationPreferringNewer() {
        Translation translationToReturn;
        int size = questions.size();
        if (size <= NEWEST_100_QUESTIONS) {
            translationToReturn = questions.get(random.nextInt(size));
        } else {
            if (is80PercentOfTimes()) {
                translationToReturn = getOneOfTheNewest100Translations();
            } else {
                translationToReturn = getTranslationNotOutOf100Newest();
            }
        }
        return translationToReturn;
    }

    private boolean is80PercentOfTimes() {
        return random.nextInt(100) < PROBABILITY_OF_80_PERCENT;
    }

    private Translation getOneOfTheNewest100Translations() {
        return questions.get(random.nextInt(NEWEST_100_QUESTIONS));
    }

    private Translation getTranslationNotOutOf100Newest() {
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
        boolean result;
        try {
            result = dao.logAnswer(translation, difficulty, clock.getTime());
        } catch (RuntimeException e) {
            return false;
        }
        reloadData();
        return result;
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

                dao.update(translation.getId(), translation.getForeignWord(), translation.getNativeWord(), new TranslationMetadata(difficulty, byId.getMetadata().getRecentDifficulty()));
                return true;
            }
        }
        return false;
    }

    public void reloadData() {
        reloadTranslations();
    }
}
