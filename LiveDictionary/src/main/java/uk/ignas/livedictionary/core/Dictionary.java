package uk.ignas.livedictionary.core;

import uk.ignas.livedictionary.core.label.Label;
import uk.ignas.livedictionary.core.label.Labeler;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static uk.ignas.livedictionary.core.util.ExceptionAnalyser.isUniqueConstraintViolation;

public class Dictionary {
    public static final int DIFFICULT_TRANSLATIONS_LIMIT = 20;
    public static final int NEWEST_100_QUESTIONS = 100;
    public static final int PROBABILITY_OF_80_PERCENT = 80;

    private List<Translation> translations;
    private final Set<Translation> difficultTranslations = new HashSet<>();
    private final Reminder reminder;
    private List<Translation> veryEasyTranslations = new ArrayList<>();
    private final Random random = new Random();
    private TranslationDao dao;
    private final AnswerDao answerDao;
    private DaoObjectsFetcher fetcher;
    private Labeler labeler;
    private Clock clock;

    public Dictionary(TranslationDao dao, AnswerDao answerDao, DaoObjectsFetcher fetcher, Labeler labeler, Clock clock) {
        this.dao = dao;
        this.answerDao = answerDao;
        this.fetcher = fetcher;
        this.labeler = labeler;
        this.clock = clock;
        reminder = new Reminder(clock);
        reloadTranslations();
    }

    private void reloadTranslations() {
        List<Translation> translations = dao.getAllTranslations();
        fetcher.fetchAnswersLog(translations);
        translations = filterNonLabelledTranslations(translations);
        Collections.reverse(translations);
        this.difficultTranslations.clear();
        for (Translation t : new ArrayList<>(translations)) {
            TranslationMetadata metadata = t.getMetadata();
            if (!isLastAnswerCorrect(metadata)) {
                translations.remove(t);
                difficultTranslations.add(t);
            }
        }
        this.translations = translations;
    }

    private List<Translation> filterNonLabelledTranslations(List<Translation> translations) {
        List<Translation> nonLabelledTranslations = new ArrayList<>(translations);
        Collection<Translation> labelled = labeler.getLabelled(Label.A);
        nonLabelledTranslations.removeAll(labelled);
        return nonLabelledTranslations;
    }

    private boolean isLastAnswerCorrect(TranslationMetadata metadata) {
        if (metadata.getRecentAnswers().size() == 0) {
            return true;
        } else {
            Answer answer = metadata.getRecentAnswers().get(metadata.getRecentAnswers().size() - 1).getAnswer();
            return answer == Answer.CORRECT;
        }
    }

    public Translation getRandomTranslation() {
        if (translations.size() == 0 && difficultTranslations.size() == 0 && veryEasyTranslations.size() == 0) {
            throw new LiveDictionaryException("no questions found");
        }
        Translation translationToReturn = null;
        while (translationToReturn == null) {
            Translation candidateTranslation = chooseTranslationPreferingDifficultOrNewer();

            if (reminder.shouldBeReminded(candidateTranslation.getMetadata())) {
                translationToReturn = candidateTranslation;
            } else {
                veryEasyTranslations.add(candidateTranslation);
                translations.remove(candidateTranslation);
            }
        }
        return translationToReturn;
    }

    private Translation chooseTranslationPreferingDifficultOrNewer() {
        if (translations.size() == 0 && difficultTranslations.size() == 0) {
            throw new LiveDictionaryException("There are no more difficult words");
        }
        Translation translationToReturn;
        if (translations.size() == 0 ||
                difficultTranslations.size() > 0 &&
                difficultTranslations.size() > random.nextInt(DIFFICULT_TRANSLATIONS_LIMIT)) {
            translationToReturn = getRandomDifficultTranslation();
        } else {
            translationToReturn = chooseTranslationPreferringNewer();
        }
        return translationToReturn;
    }

    private Translation chooseTranslationPreferringNewer() {
        Translation translationToReturn;
        int size = translations.size();
        if (size <= NEWEST_100_QUESTIONS) {
            translationToReturn = translations.get(random.nextInt(size));
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
        return translations.get(random.nextInt(NEWEST_100_QUESTIONS));
    }

    private Translation getTranslationNotOutOf100Newest() {
        int randomGreaterOrEqualsTo100 = random.nextInt(translations.size() - NEWEST_100_QUESTIONS) + NEWEST_100_QUESTIONS;
        return translations.get(randomGreaterOrEqualsTo100);
    }

    private Translation getRandomDifficultTranslation() {
        ArrayList<Translation> translations = new ArrayList<>(difficultTranslations);
        Collections.shuffle(translations);
        return translations.get(0);
    }

    public boolean mark(Translation translation, Answer answer) {
        if (translation.getId() == null) {
            return false;
        }
        boolean result;
        try {
            result = answerDao.logAnswer(translation, answer, clock.getTime());
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

        boolean updatedAsDifficultCount = updateIfIsAnIdOfAnyOfTranslations(translation, difficultTranslations);
        boolean updatedAsEasyCount = updateIfIsAnIdOfAnyOfTranslations(translation, translations);
        reloadTranslations();
        return updatedAsDifficultCount || updatedAsEasyCount;
    }

    private boolean updateIfIsAnIdOfAnyOfTranslations(Translation translation, Collection<Translation> questions) {
        for (Translation t : questions) {
            if (Objects.equals(t.getId(), translation.getId())) {
                try {
                    dao.update(translation);
                } catch (Exception e) {
                    if (isUniqueConstraintViolation(e)) {
                        dao.delete(asList(translation));
                    } else {
                        throw e;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public void reloadData() {
        reloadTranslations();
    }
}
