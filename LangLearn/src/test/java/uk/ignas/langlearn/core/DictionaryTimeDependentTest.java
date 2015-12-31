package uk.ignas.langlearn.core;

import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;
import uk.ignas.langlearn.testutils.LiveDictionaryDsl;
import uk.ignas.langlearn.testutils.TranslationDaoStub;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DictionaryTimeDependentTest {
    private static final Date NOW;

    static {
        Calendar c = Calendar.getInstance();
        c.set(2015, Calendar.JANUARY, 1, 12, 0);
        NOW = c.getTime();
    }

    public static final Date LEVEL_1_STAGING_PERIOD_PASSED = createDateDifferingBy(NOW, 4, Calendar.HOUR);
    public static final Date LEVEL_1_STAGING_PERIOD_NOT_YET_PASSED = createDateDifferingBy(NOW, 3*60+59, Calendar.MINUTE);

    private TranslationDao dao = new TranslationDaoStub();

    @Test
    public void onceStagedZeroLevelTranslationShouldNotBeAsked() {
        dao.insertSingle(createForeignToNativeTranslation("palabra", "word"));
        Translation translation = dao.getAllTranslations().get(0);
        Clock clock = mock(Clock.class);
        Dictionary dictionary = new Dictionary(dao, clock);
        when(clock.getTime()).thenReturn(NOW);
        dictionary.mark(translation, Difficulty.EASY);
        dictionary.mark(translation, Difficulty.EASY);
        when(clock.getTime()).thenReturn(LEVEL_1_STAGING_PERIOD_NOT_YET_PASSED);

        try {
            dictionary.getRandomTranslation();
            fail();
        } catch (LiveDictionaryException e) {
            assertThat(e.getMessage(), Matchers.containsString("There are no more difficult words"));
        }
        try {
            dictionary.getRandomTranslation();
            fail();
        } catch (LiveDictionaryException e) {
            assertThat(e.getMessage(), Matchers.containsString("There are no more difficult words"));
        }
    }

    @Test
    public void onceZerothLevelTranslationIsStagedOthersShouldBeAsked() {
        dao.insertSingle(createForeignToNativeTranslation("la palabra", "word"));
        dao.insertSingle(createForeignToNativeTranslation("la frase", "phrase"));
        Translation easyTranslation = dao.getAllTranslations().get(0);
        Translation otherTranslation = dao.getAllTranslations().get(1);
        Dictionary dictionary = new Dictionary(dao);

        dictionary.mark(easyTranslation, Difficulty.EASY);
        dictionary.mark(easyTranslation, Difficulty.EASY);

        List<Translation> notYetStaged = LiveDictionaryDsl.retrieveTranslationsNTimes(dictionary, 10);

        int percentage = LiveDictionaryDsl.countPercentageOfRetrievedNativeWordsHadExpectedPattern(notYetStaged, otherTranslation.getNativeWord().get());
        assertThat(percentage, is(equalTo(100)));
    }

    @Test
    public void afterMistakingTranslationIsStagedToLevel1() {
        dao.insertSingle(createForeignToNativeTranslation("palabra", "word"));
        Translation translation = dao.getAllTranslations().get(0);
        Dictionary dictionary = new Dictionary(dao);
        dictionary.mark(translation, Difficulty.EASY);
        dictionary.mark(translation, Difficulty.DIFFICULT);
        dictionary.mark(translation, Difficulty.EASY);

        Translation retrieved = dictionary.getRandomTranslation();

        assertThat(retrieved, is(equalTo(translation)));
    }

    @Test

    public void itIsNotEnoughToAnswerLevel1OnceToStageIt() {
        dao.insertSingle(createForeignToNativeTranslation("palabra", "word"));
        Translation translation = dao.getAllTranslations().get(0);
        Dictionary dictionary = new Dictionary(dao);
        dictionary.mark(translation, Difficulty.EASY);
        dictionary.mark(translation, Difficulty.DIFFICULT);
        dictionary.mark(translation, Difficulty.EASY);
        dictionary.mark(translation, Difficulty.EASY);

        Translation retrieved = dictionary.getRandomTranslation();

        assertThat(retrieved, is(equalTo(translation)));
    }

    @Test
    public void onceStagingPeriodIsFinishedTheWordShouldBeAskedAgain() {
        dao.insertSingle(createForeignToNativeTranslation("palabra", "word"));
        Translation translation = dao.getAllTranslations().get(0);
        Clock clock = mock(Clock.class);
        Dictionary dictionary = new Dictionary(dao, clock);
        when(clock.getTime()).thenReturn(NOW);
        dictionary.mark(translation, Difficulty.EASY);
        dictionary.mark(translation, Difficulty.EASY);
        when(clock.getTime()).thenReturn(LEVEL_1_STAGING_PERIOD_PASSED);

        Translation retrieved = dictionary.getRandomTranslation();

        assertThat(retrieved, is(equalTo(translation)));
    }

    private Translation createForeignToNativeTranslation(String foreignWord, String nativeWord) {
        return new Translation(new ForeignWord(foreignWord), new NativeWord(nativeWord));
    }

    private static Date createDateDifferingBy(Date now, int amount, int calendarField) {
        Calendar c = Calendar.getInstance();
        c.setTime(now);
        c.add(calendarField, amount);
        return c.getTime();
    }
}
