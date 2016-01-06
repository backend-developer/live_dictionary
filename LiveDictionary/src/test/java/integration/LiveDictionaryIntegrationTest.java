package integration;

import integration.testutils.DaoCreator;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import uk.ignas.livedictionary.BuildConfig;
import uk.ignas.livedictionary.core.*;
import uk.ignas.livedictionary.core.Dictionary;
import uk.ignas.livedictionary.testutils.LiveDictionaryDsl;

import java.util.*;

import static com.google.common.collect.Iterables.getLast;
import static com.google.common.collect.Iterables.getOnlyElement;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ignas.livedictionary.testutils.LiveDictionaryDsl.countPercentageOfRetrievedNativeWordsHadExpectedPattern;
import static uk.ignas.livedictionary.testutils.LiveDictionaryDsl.retrieveTranslationsNTimes;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class LiveDictionaryIntegrationTest {
    private static final Date NOW;

    static {
        Calendar c = Calendar.getInstance();
        c.set(2015, Calendar.JANUARY, 1, 12, 0);
        NOW = c.getTime();
    }

    public static final Date LEVEL_1_STAGING_PERIOD_NOT_YET_PASSED = createDateDifferingBy(NOW, 3*60+59, Calendar.MINUTE);

    private static int uniqueSequence = 0;

    private TranslationDao dao = DaoCreator.createEmpty();

    @Test
    public void shouldThrowWhenIfThereAreNoTranslationToRetrieve() {
        Dictionary dictionary = new Dictionary(dao);
        try {
            dictionary.getRandomTranslation();
            fail();
        } catch (LiveDictionaryException e) {
            assertTrue(e.getMessage().contains("no questions found"));
        }
    }

    private Translation createForeignToNativeTranslation(String foreignWord, String nativeWord) {
        return new Translation(new ForeignWord(foreignWord), new NativeWord(nativeWord));
    }

    @Test
    public void shouldNotCrashWhenThereAreFewTranslations() {
        dao.insertSingle(createForeignToNativeTranslation("palabra", "word"));
        Dictionary dictionary = new Dictionary(dao);

        Translation translation = dictionary.getRandomTranslation();

        assertThat(translation.getForeignWord().get(), is(equalTo("palabra")));
    }

    @Test
    public void shouldNotCrashWhenAllTheWordsAreIncorrectlyAnswered() {
        dao.insertSingle(createForeignToNativeTranslation("palabra", "word"));
        Translation translation = dao.getAllTranslations().get(0);
        Dictionary dictionary = new Dictionary(dao);
        dictionary.mark(translation, Answer.INCORRECT);

        List<Translation> translations = LiveDictionaryDsl.retrieveTranslationsNTimes(dictionary, 10);

        int percentage = LiveDictionaryDsl.countPercentageOfRetrievedNativeWordsHadExpectedPattern(translations, "word");
        assertThat(percentage, is(100));
    }


    @Test
    public void shouldSynchronizeWithDbOnDemand() {
        Dictionary dictionary = new Dictionary(dao);
        dao.insertSingle(createForeignToNativeTranslation("la palabra", "word"));

        dictionary.reloadData();

        Translation translation = dictionary.getRandomTranslation();
        assertThat(translation.getForeignWord().get(), is(equalTo("la palabra")));
    }

    @Test
    public void shouldPersistDifficultTranslations() {
        dao.insertSingle(createForeignToNativeTranslation("palabra", "word"));
        Translation translation = getOnlyElement(dao.getAllTranslations());
        Dictionary dictionary = new Dictionary(dao);

        dictionary.mark(translation, Answer.INCORRECT);

        Collection<AnswerAtTime> recentAnswers = dao.getAnswersLogByTranslationId().values();
        assertThat(getLast(recentAnswers).getAnswer(), is(equalTo(Answer.INCORRECT)));
    }

    @Test
    public void shouldGetNewest100TranslationsWith80PercentProbability() {
        dao.insert(getNTranslationsWithNativeWordStartingWith(100, "Other"));
        dao.insert(getNTranslationsWithNativeWordStartingWith(100, "LastQ"));
        Dictionary dictionary = new Dictionary(dao);

        final List<Translation> retrievedTranslations = retrieveTranslationsNTimes(dictionary, 1000);

        int percentage = countPercentageOfRetrievedNativeWordsHadExpectedPattern(retrievedTranslations, "LastQ");
        assertThat(percentage, allOf(greaterThan(75), lessThan(85)));
    }

    @Test
    public void shouldHandle100Translations() {
        for (int i = 0; i < 100; i++) {
            dao.insert(getNTranslationsWithNativeWordStartingWith(100, "Any"));
            Dictionary dictionary = new Dictionary(dao);

            List<Translation> translations = LiveDictionaryDsl.retrieveTranslationsNTimes(dictionary, 100);

            int percentage = LiveDictionaryDsl.countPercentageOfRetrievedNativeWordsHadExpectedPattern(translations, "Any");
            assertThat(percentage, is(100));
        }
    }

    @Test
    public void afterFinding20DifficultTranslationsShouldNeverAskForOthers() {
        dao.insert(getNTranslationsWithNativeWordStartingWith(100, "Other"));
        dao.insert(getNTranslationsWithNativeWordStartingWith(20, "DifficultWord"));
        Dictionary dictionary = new Dictionary(dao);
        for (Translation t: new HashSet<>(dao.getAllTranslations())) {
            if (t.getNativeWord().get().contains("DifficultWord")) {
                dictionary.mark(t, Answer.INCORRECT);
            }
        }

        final List<Translation> retrievedTranslations = retrieveTranslationsNTimes(dictionary, 100);

        int percentage = countPercentageOfRetrievedNativeWordsHadExpectedPattern(retrievedTranslations, "DifficultWord");
        assertThat(percentage, is(equalTo(100)));
    }

    @Test
    public void diffucultTranslationsWhichAreAlreadyBecameEasyShouldStopBeingAskedEvery20thTime() {
        dao.insert(getNTranslationsWithNativeWordStartingWith(80, "Other"));
        dao.insert(getNTranslationsWithNativeWordStartingWith(10, "DifficultWord"));
        dao.insert(getNTranslationsWithNativeWordStartingWith(10, "WasDifficultButNowEasyWord"));
        Dictionary dictionary = new Dictionary(dao);
        for (Translation t: new HashSet<>(dao.getAllTranslations())) {
            if (t.getNativeWord().get().contains("DifficultWord")) {
                dictionary.mark(t, Answer.INCORRECT);
            }
            if (t.getNativeWord().get().contains("WasDifficultButNowEasyWord")) {
                dictionary.mark(t, Answer.INCORRECT);
                dictionary.mark(t, Answer.CORRECT);
            }
        }

        final List<Translation> retrievedTranslations = retrieveTranslationsNTimes(dictionary, 1000);

        int percentage = countPercentageOfRetrievedNativeWordsHadExpectedPattern(retrievedTranslations, "DifficultWord");
        assertThat(percentage, allOf(greaterThan(45), lessThan(55)));
    }

    @Test
    public void difficultTranslationsShouldBeAskedEvery20thTime() {
        dao.insert(getNTranslationsWithNativeWordStartingWith(100, "Other"));
        dao.insert(getNTranslationsWithNativeWordStartingWith(10, "DifficultWord"));
        Dictionary dictionary = new Dictionary(dao);
        for (Translation t: new HashSet<>(dao.getAllTranslations())) {
            if (t.getNativeWord().get().contains("DifficultWord")) {
                dictionary.mark(t, Answer.INCORRECT);
            }
        }

        final List<Translation> retrievedTranslations = retrieveTranslationsNTimes(dictionary, 1000);

        int percentage = countPercentageOfRetrievedNativeWordsHadExpectedPattern(retrievedTranslations, "DifficultWord");
        assertThat(percentage, allOf(greaterThan(45), lessThan(55)));
    }

    @Test
    public void difficultTranslationsShouldBeAskedEvery20thTimeEvenIfTheyWerePassedInitially() {
        dao.insert(getNTranslationsWithNativeWordStartingWith(100, "Other"));
        dao.insert(getNTranslationsWithNativeWordStartingWith(10, "DifficultWord"));
        for (Translation t: new HashSet<>(dao.getAllTranslations())) {
            if (t.getNativeWord().get().contains("DifficultWord")) {
                dao.logAnswer(t, Answer.INCORRECT, new Date());
            }
        }
        Dictionary dictionary = new Dictionary(dao);

        final List<Translation> retrievedTranslations = retrieveTranslationsNTimes(dictionary, 1000);

        int percentage = countPercentageOfRetrievedNativeWordsHadExpectedPattern(retrievedTranslations, "DifficultWord");
        assertThat(percentage, allOf(greaterThan(45), lessThan(55)));
    }
    @Test
    public void mistakenTranslationShouldBeAsked3TimesToBeRestrictedFromBeingAskedByPromotionPeriod() {
        dao.insertSingle(createForeignToNativeTranslation("palabra", "word"));
        Translation translation = dao.getAllTranslations().get(0);
        Dictionary dictionary = new Dictionary(dao);
        dictionary.mark(translation, Answer.CORRECT);
        dictionary.mark(translation, Answer.INCORRECT);
        dictionary.mark(translation, Answer.CORRECT);
        dictionary.mark(translation, Answer.CORRECT);
        dictionary.mark(translation, Answer.CORRECT);

        gettingNextTranslationShouldThroughLDEwithMessage(dictionary, "There are no more difficult words");
    }

    @Test
    public void onceRestrictedByPromotionZerothLevelTranslationShouldNotBeAsked() {
        dao.insertSingle(createForeignToNativeTranslation("palabra", "word"));
        Translation translation = dao.getAllTranslations().get(0);
        Clock clock = mock(Clock.class);
        Dictionary dictionary = new Dictionary(dao, clock);
        when(clock.getTime()).thenReturn(NOW);
        dictionary.mark(translation, Answer.CORRECT);
        dictionary.mark(translation, Answer.CORRECT);
        when(clock.getTime()).thenReturn(LEVEL_1_STAGING_PERIOD_NOT_YET_PASSED);

        gettingNextTranslationShouldThroughLDEwithMessage(dictionary, "There are no more difficult words");
        gettingNextTranslationShouldThroughLDEwithMessage(dictionary, "There are no more difficult words");
    }

    @Test
    public void onceZerothLevelTranslationIsRestrictedByPromotionOtherTranslationsShouldBeAsked() {
        dao.insertSingle(createForeignToNativeTranslation("la palabra", "word"));
        dao.insertSingle(createForeignToNativeTranslation("la frase", "phrase"));
        Translation easyTranslation = dao.getAllTranslations().get(0);
        Translation otherTranslation = dao.getAllTranslations().get(1);
        Dictionary dictionary = new Dictionary(dao);

        dictionary.mark(easyTranslation, Answer.CORRECT);
        dictionary.mark(easyTranslation, Answer.CORRECT);

        List<Translation> notYetStaged = LiveDictionaryDsl.retrieveTranslationsNTimes(dictionary, 10);
        int percentage = LiveDictionaryDsl.countPercentageOfRetrievedNativeWordsHadExpectedPattern(notYetStaged, otherTranslation.getNativeWord().get());
        assertThat(percentage, is(equalTo(100)));
    }



    @Test
    public void shouldInsertTranslation() {
        Dictionary dictionary = new Dictionary(dao);

        dictionary.insert(createForeignToNativeTranslation("la palabra", "word"));

        assertThat(dao.getAllTranslations().size(), is(equalTo(1)));
        assertThat(dictionary.getRandomTranslation().getForeignWord().get(), is("la palabra"));
    }

    @Test
    public void shouldNotInsertDuplicates() {
        Dictionary dictionary = new Dictionary(dao);

        dictionary.insert(createForeignToNativeTranslation("duplicate", "dup_translation"));
        dictionary.insert(createForeignToNativeTranslation("duplicate", "dup_translation"));

        assertThat(dao.getAllTranslations().size(), is(equalTo(1)));
    }

    @Test
    public void shouldDeleteTranslation() {
        dao.insertSingle(createForeignToNativeTranslation("word", "la palabra"));
        Translation translation = dao.getAllTranslations().get(0);
        Dictionary dictionary = new Dictionary(dao);

        dictionary.delete(translation);

        assertThat(dao.getAllTranslations(), not(hasItem(translation)));
        try {
            dictionary.getRandomTranslation();
            fail();
        } catch (LiveDictionaryException e) {
            assertThat(e.getMessage(), containsString("no questions found")) ;
        }
    }

    @Test
    public void answersShouldBeDeletedAlongWithTranslation() {
        dao.insertSingle(createForeignToNativeTranslation("word", "la palabra"));
        Translation translation = dao.getAllTranslations().get(0);
        Dictionary dictionary = new Dictionary(dao);
        dictionary.mark(translation, Answer.CORRECT);

        dictionary.delete(translation);

        assertThat(dao.getAnswersLogByTranslationId().get(translation.getId()), empty());
    }

    @Test
    public void shouldNotAllowToAnswerTranslationsWithoutId() {
        Dictionary dictionary = new Dictionary(dao);

        boolean isUpdated = dictionary.mark(createForeignToNativeTranslation("duplicate", "dup_translation"), Answer.INCORRECT);

        assertThat(isUpdated, is(false));
    }

    @Test
    public void shouldNotAllowToAnswerTranslationsApsentInDb() {
        Dictionary dictionary = new Dictionary(dao);
        int nonexistentId = 8949861;

        boolean isUpdated = dictionary.mark(new Translation(nonexistentId, new ForeignWord("la duplicado"), new NativeWord("duplication")), Answer.INCORRECT);

        assertThat(isUpdated, is(false));
    }

    @Test
    public void shouldAllowAnswerIncorrectly() {
        Dictionary dictionary = new Dictionary(dao);
        dao.insert(singletonList(createForeignToNativeTranslation("word", "la palabra")));
        Translation translation = dao.getAllTranslations().iterator().next();

        boolean isUpdated = dictionary.mark(translation, Answer.INCORRECT);

        assertThat(isUpdated, is(true));
    }

    @Test
    public void shouldUpdateTranslation() {
        dao.insert(singletonList(createForeignToNativeTranslation("la palabra", "word")));
        Dictionary dictionary = new Dictionary(dao);
        Translation translation = dao.getAllTranslations().iterator().next();

        boolean isUpdated = dictionary.update(new Translation(translation.getId(), new ForeignWord("la palabra cambiada"), new NativeWord("modified word")));

        assertThat(isUpdated, is(true));
        Translation modifiedWord = dao.getAllTranslations().iterator().next();
        assertThat(modifiedWord.getForeignWord().get(), is(equalTo("la palabra cambiada")));
        assertThat(modifiedWord.getNativeWord().get(), is(equalTo("modified word")));
        assertThat(dictionary.getRandomTranslation().getNativeWord().get(), is("modified word"));
    }

    private static Date createDateDifferingBy(Date now, int amount, int calendarField) {
        Calendar c = Calendar.getInstance();
        c.setTime(now);
        c.add(calendarField, amount);
        return c.getTime();
    }

    private void gettingNextTranslationShouldThroughLDEwithMessage(Dictionary dictionary, String message) {
        try {
            dictionary.getRandomTranslation();
            fail();
        } catch (LiveDictionaryException e) {
            assertThat(e.getMessage(), Matchers.containsString(message));
        }
    }

    public List<Translation> getNTranslationsWithNativeWordStartingWith(int n, String prefix) {
        List<Translation> translations = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            translations.add(new Translation(new ForeignWord("desconocido" + getUniqueInt()), new NativeWord(prefix + getUniqueInt())));
        }
        return translations;
    }

    private int getUniqueInt() {
        return uniqueSequence++;
    }


}
