package uk.ignas.livedictionary.core;

import com.google.common.base.Optional;
import org.hamcrest.Matchers;
import org.junit.Test;
import uk.ignas.livedictionary.core.answer.Answer;
import uk.ignas.livedictionary.core.answer.AnswerAtTime;
import uk.ignas.livedictionary.testutils.LiveDictionaryDsl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ignas.livedictionary.testutils.LiveDictionaryDsl.*;

public class PreferNewestTranslationSelectionStrategyTest {
    private static final Date NOW;
    static {
        Calendar c = Calendar.getInstance();
        c.set(2015, Calendar.JANUARY, 1, 12, 0);
        NOW = c.getTime();
    }

    public static final Date LEVEL_1_STAGING_PERIOD_NOT_YET_PASSED =
        createDateDifferingBy(NOW, 3 * 60 + 59, Calendar.MINUTE);



    public static final Date ANY_DATETIME = new Date();

    private static int uniqueSequence = 0;

    private Clock clock = new Clock();
    PreferNewestTranslationSelectionStrategy strategy = new PreferNewestTranslationSelectionStrategy(clock);

    @Test
    public void shouldNotReturnWordWhenTransaltionsAreNotProvided() {

        Optional<Translation> translation = strategy.selectTranslation();

        assertThat(translation.isPresent(), is(false));
    }

    @Test
    public void shouldNotReturnWordWhenNotTransaltionsAreProvided() {
        strategy.updateState(new ArrayList<Translation>());

        Optional<Translation> translation = strategy.selectTranslation();

        assertThat(translation.isPresent(), is(false));
    }

    @Test
    public void shouldReturnSingleTranslationsProvided() {
        strategy.updateState(newArrayList(createForeignToNativeTranslation("palabra", "word")));

        Optional<Translation> translation = strategy.selectTranslation();

        assertThat(translation.isPresent(), is(true));
        assertThat(translation.get().getForeignWord().get(), is(equalTo("palabra")));
    }

    @Test
    public void shouldProvideIncorrectlyAnsweredIfOnlyThoseAreAvailable() {
        Translation translation1 = createForeignToNativeTranslation("palabra", "word");
        translation1.getMetadata().getRecentAnswers().add(new AnswerAtTime(Answer.INCORRECT, ANY_DATETIME));
        strategy.updateState(newArrayList(translation1));

        List<Translation> translations = retrieveTranslationsNTimes(strategy, 10);

        int percentage =
            LiveDictionaryDsl.countPercentageOfRetrievedNativeWordsHadExpectedPattern(translations, "word");
        assertThat(percentage, is(100));
    }

    @Test
    public void shouldGetNewest100TranslationsWith80PercentProbability() {
        List<Translation> translations = new ArrayList<>();
        translations.addAll(getNTranslationsWithNativeWordStartingWith(100, "Other"));
        translations.addAll(getNTranslationsWithNativeWordStartingWith(100, "LastQ"));
        strategy.updateState(translations);

        final List<Translation> retrievedTranslations = retrieveTranslationsNTimes(strategy, 1000);

        int percentage = countPercentageOfRetrievedNativeWordsHadExpectedPattern(retrievedTranslations, "LastQ");
        assertThat(percentage, allOf(greaterThan(75), lessThan(85)));
    }

    @Test
    public void shouldHandle100Translations() {
        for (int i = 0; i < 100; i++) {
            strategy.updateState(getNTranslationsWithNativeWordStartingWith(100, "Any"));

            List<Translation> translations = LiveDictionaryDsl.retrieveTranslationsNTimes(strategy, 100);

            int percentage =
                LiveDictionaryDsl.countPercentageOfRetrievedNativeWordsHadExpectedPattern(translations, "Any");
            assertThat(percentage, Matchers.is(100));
        }
    }

    @Test
    public void afterFinding20DifficultTranslationsShouldNeverAskForOthers() {
        List<Translation> translations = new ArrayList<>();
        translations.addAll(getNTranslationsWithNativeWordStartingWith(100, "Other"));
        translations.addAll(getNTranslationsWithNativeWordStartingWith(20, "DifficultWord"));

        for (Translation t : translations) {
            if (t.getNativeWord().get().contains("DifficultWord")) {
                t.getMetadata().getRecentAnswers().add(new AnswerAtTime(Answer.INCORRECT, ANY_DATETIME));
            }
        }
        strategy.updateState(translations);

        final List<Translation> retrievedTranslations = retrieveTranslationsNTimes(strategy, 100);

        int percentage =
            countPercentageOfRetrievedNativeWordsHadExpectedPattern(retrievedTranslations, "DifficultWord");
        assertThat(percentage, Matchers.is(Matchers.equalTo(100)));
    }

    @Test
    public void diffucultTranslationsWhichAreAlreadyBecameEasyShouldStopBeingAskedEvery20thTime() {
        List<Translation> translations = new ArrayList<>();
        translations.addAll(getNTranslationsWithNativeWordStartingWith(80, "Other"));
        translations.addAll(getNTranslationsWithNativeWordStartingWith(10, "DifficultWord"));
        translations.addAll(getNTranslationsWithNativeWordStartingWith(10, "WasDifficultButNowEasyWord"));

        for (Translation t : translations) {
            if (t.getNativeWord().get().contains("DifficultWord")) {
                t.getMetadata().getRecentAnswers().add(new AnswerAtTime(Answer.INCORRECT, ANY_DATETIME));
            }
            if (t.getNativeWord().get().contains("WasDifficultButNowEasyWord")) {
                t.getMetadata().getRecentAnswers().add(new AnswerAtTime(Answer.INCORRECT, ANY_DATETIME));
                t.getMetadata().getRecentAnswers().add(new AnswerAtTime(Answer.CORRECT, ANY_DATETIME));
            }
        }
        strategy.updateState(translations);

        final List<Translation> retrievedTranslations = retrieveTranslationsNTimes(strategy, 1000);

        int percentage =
            countPercentageOfRetrievedNativeWordsHadExpectedPattern(retrievedTranslations, "DifficultWord");
        assertThat(percentage, allOf(greaterThan(45), lessThan(55)));
    }

    @Test
    public void difficultTranslationsShouldBeAskedEvery20thTime() {
        List<Translation> translations = new ArrayList<>();
        translations.addAll(getNTranslationsWithNativeWordStartingWith(100, "Other"));
        translations.addAll(getNTranslationsWithNativeWordStartingWith(10, "DifficultWord"));
        for (Translation t : translations) {
            if (t.getNativeWord().get().contains("DifficultWord")) {
                t.getMetadata().getRecentAnswers().add(new AnswerAtTime(Answer.INCORRECT, ANY_DATETIME));
            }
        }
        strategy.updateState(translations);

        final List<Translation> retrievedTranslations = retrieveTranslationsNTimes(strategy, 1000);

        int percentage =
            countPercentageOfRetrievedNativeWordsHadExpectedPattern(retrievedTranslations, "DifficultWord");
        assertThat(percentage, allOf(greaterThan(45), lessThan(55)));
    }

    @Test
    public void mistakenTranslationShouldBeAsked3TimesToBeRestrictedFromBeingAskedByPromotionPeriod() {
        Translation translation = createForeignToNativeTranslation("palabra", "word");
        translation.getMetadata().getRecentAnswers().add(new AnswerAtTime(Answer.CORRECT, ANY_DATETIME));
        translation.getMetadata().getRecentAnswers().add(new AnswerAtTime(Answer.INCORRECT, ANY_DATETIME));
        translation.getMetadata().getRecentAnswers().add(new AnswerAtTime(Answer.CORRECT, ANY_DATETIME));
        translation.getMetadata().getRecentAnswers().add(new AnswerAtTime(Answer.CORRECT, ANY_DATETIME));
        translation.getMetadata().getRecentAnswers().add(new AnswerAtTime(Answer.CORRECT, ANY_DATETIME));
        strategy.updateState(newArrayList(translation));

        gettingNextTranslationShouldThroughLDEwithMessage(strategy, "There are no more difficult words");
    }

    @Test
    public void onceRestrictedByPromotionZerothLevelTranslationShouldNotBeAsked() {
        Translation translation = createForeignToNativeTranslation("palabra", "word");
        Clock clock = mock(Clock.class);
        when(clock.getTime()).thenReturn(NOW);
        translation.getMetadata().getRecentAnswers().add(new AnswerAtTime(Answer.CORRECT, ANY_DATETIME));
        translation.getMetadata().getRecentAnswers().add(new AnswerAtTime(Answer.CORRECT, ANY_DATETIME));
        when(clock.getTime()).thenReturn(LEVEL_1_STAGING_PERIOD_NOT_YET_PASSED);
        strategy.updateState(newArrayList(translation));

        gettingNextTranslationShouldThroughLDEwithMessage(strategy, "There are no more difficult words");
        gettingNextTranslationShouldThroughLDEwithMessage(strategy, "There are no more difficult words");
    }

    @Test
    public void onceZerothLevelTranslationIsRestrictedByPromotionOtherTranslationsShouldBeAsked() {
        Translation easyTranslation = createForeignToNativeTranslation("la palabra", "word");
        Translation otherTranslation = createForeignToNativeTranslation("la frase", "phrase");

        easyTranslation.getMetadata().getRecentAnswers().add(new AnswerAtTime(Answer.CORRECT, ANY_DATETIME));
        easyTranslation.getMetadata().getRecentAnswers().add(new AnswerAtTime(Answer.CORRECT, ANY_DATETIME));
        strategy.updateState(newArrayList(easyTranslation, otherTranslation));

        List<Translation> notYetStaged = LiveDictionaryDsl.retrieveTranslationsNTimes(strategy, 10);
        int percentage = LiveDictionaryDsl.countPercentageOfRetrievedNativeWordsHadExpectedPattern(notYetStaged,
                                                                                                   otherTranslation
                                                                                                       .getNativeWord()
                                                                                                       .get());
        assertThat(percentage, Matchers.is(Matchers.equalTo(100)));
    }


    private void gettingNextTranslationShouldThroughLDEwithMessage(TranslationSelectionStrategy strategy, String message) {
        try {
            strategy.selectTranslation();
            fail();
        } catch (LiveDictionaryException e) {
            assertThat(e.getMessage(), Matchers.containsString(message));
        }
    }

    private static Date createDateDifferingBy(Date now, int amount, int calendarField) {
        Calendar c = Calendar.getInstance();
        c.setTime(now);
        c.add(calendarField, amount);
        return c.getTime();
    }


    public List<Translation> getNTranslationsWithNativeWordStartingWith(int n, String prefix) {
        List<Translation> translations = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            translations.add(new Translation(new ForeignWord("desconocido" + getUniqueInt()),
                                             new NativeWord(prefix + getUniqueInt())));
        }
        return translations;
    }


    private int getUniqueInt() {
        return uniqueSequence++;
    }
}