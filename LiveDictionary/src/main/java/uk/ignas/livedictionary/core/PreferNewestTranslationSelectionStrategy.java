package uk.ignas.livedictionary.core;

import com.google.common.base.Optional;
import uk.ignas.livedictionary.core.answer.Answer;

import java.util.*;

public class PreferNewestTranslationSelectionStrategy implements TranslationSelectionStrategy {
    private List<Translation> translations = new ArrayList<>();

    private final Set<Translation> difficultTranslations = new HashSet<>();

    private List<Translation> veryEasyTranslations = new ArrayList<>();

    private final Reminder reminder;

    private final Random random = new Random();


    public PreferNewestTranslationSelectionStrategy(Clock clock) {
        reminder = new Reminder(clock);
    }

    @Override
    public void updateState(List<Translation> translationsGiven) {
        List<Translation> translations;
        translations = translationsGiven;
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

    private boolean isLastAnswerCorrect(TranslationMetadata metadata) {
        if (metadata.getRecentAnswers().size() == 0) {
            return true;
        } else {
            Answer answer = metadata.getRecentAnswers().get(metadata.getRecentAnswers().size() - 1).getAnswer();
            return answer == Answer.CORRECT;
        }
    }

    @Override
    public Optional<Translation> selectTranslation() {
        if (translations.size() == 0 && difficultTranslations.size() == 0 && veryEasyTranslations.size() == 0) {
            return Optional.absent();
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
        return Optional.of(translationToReturn);
    }

    private Translation chooseTranslationPreferingDifficultOrNewer() {
        if (translations.size() == 0 && difficultTranslations.size() == 0) {
            throw new LiveDictionaryException("There are no more difficult words");
        }
        Translation translationToReturn;
        if (translations.size() == 0 || difficultTranslations.size() > 0 && difficultTranslations.size() > random
            .nextInt(Dictionary.DIFFICULT_TRANSLATIONS_LIMIT))
        {
            translationToReturn = getRandomDifficultTranslation();
        } else {
            translationToReturn = chooseTranslationPreferringNewer();
        }
        return translationToReturn;
    }

    private Translation getRandomDifficultTranslation() {
        ArrayList<Translation> translations = new ArrayList<>(difficultTranslations);
        Collections.shuffle(translations);
        return translations.get(0);
    }

    private Translation chooseTranslationPreferringNewer() {
        Translation translationToReturn;
        int size = translations.size();
        if (size <= Dictionary.NEWEST_100_QUESTIONS) {
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
        return random.nextInt(100) < Dictionary.PROBABILITY_OF_80_PERCENT;
    }

    private Translation getOneOfTheNewest100Translations() {
        return translations.get(random.nextInt(Dictionary.NEWEST_100_QUESTIONS));
    }

    private Translation getTranslationNotOutOf100Newest() {
        int randomGreaterOrEqualsTo100 =
            random.nextInt(translations.size() - Dictionary.NEWEST_100_QUESTIONS) + Dictionary.NEWEST_100_QUESTIONS;
        return translations.get(randomGreaterOrEqualsTo100);
    }
}
