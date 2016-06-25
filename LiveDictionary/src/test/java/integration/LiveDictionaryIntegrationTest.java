package integration;

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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ignas.livedictionary.testutils.LiveDictionaryDsl.countPercentageOfRetrievedNativeWordsHadExpectedPattern;
import static uk.ignas.livedictionary.testutils.LiveDictionaryDsl.createForeignToNativeTranslation;
import static uk.ignas.livedictionary.testutils.LiveDictionaryDsl.retrieveTranslationsNTimes;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class LiveDictionaryIntegrationTest {
    private static final Date NOW;

    private Clock clock = new Clock();

    static {
        Calendar c = Calendar.getInstance();
        c.set(2015, Calendar.JANUARY, 1, 12, 0);
        NOW = c.getTime();
    }

    public static final Date LEVEL_1_STAGING_PERIOD_NOT_YET_PASSED =
        createDateDifferingBy(NOW, 3 * 60 + 59, Calendar.MINUTE);

    private static int uniqueSequence = 0;

    private TranslationDao dao = DaoCreator.createEmpty();
    private LabelDao labelDao = DaoCreator.clearDbAndCreateLabelDao();
    private DaoObjectsFetcher fetcher = new DaoObjectsFetcher(labelDao, dao);
    private Labeler labeler = new Labeler(dao, fetcher, labelDao);
    private Dictionary dictionary = new Dictionary(dao, fetcher, labeler, clock);

    @Test
    public void shouldThrowWhenIfThereAreNoTranslationToRetrieve() {
        Dictionary dictionary = new Dictionary(dao, fetcher, labeler, clock);
        try {
            dictionary.getRandomTranslation();
            fail();
        } catch (LiveDictionaryException e) {
            assertTrue(e.getMessage().contains("no questions found"));
        }
    }


    @Test
    public void shouldNotCrashWhenThereAreFewTranslations() {
        dao.insertSingle(createForeignToNativeTranslation("palabra", "word"));
        dictionary.reloadData();

        Translation translation = dictionary.getRandomTranslation();

        assertThat(translation.getForeignWord().get(), is(equalTo("palabra")));
    }

    @Test
    public void shouldNotCrashWhenAllTheWordsAreIncorrectlyAnswered() {
        dao.insertSingle(createForeignToNativeTranslation("palabra", "word"));
        Translation translation = dao.getAllTranslations().get(0);
        dictionary.reloadData();
        dictionary.mark(translation, Answer.INCORRECT);

        List<Translation> translations = LiveDictionaryDsl.retrieveTranslationsNTimes(dictionary, 10);

        int percentage =
            LiveDictionaryDsl.countPercentageOfRetrievedNativeWordsHadExpectedPattern(translations, "word");
        assertThat(percentage, is(100));
    }


    @Test
    public void shouldSynchronizeWithDbOnDemand() {
        dao.insertSingle(createForeignToNativeTranslation("la palabra", "word"));

        dictionary.reloadData();

        Translation translation = dictionary.getRandomTranslation();
        assertThat(translation.getForeignWord().get(), is(equalTo("la palabra")));
    }

    @Test
    public void shouldPersistDifficultTranslations() {
        dao.insertSingle(createForeignToNativeTranslation("palabra", "word"));
        Translation translation = getOnlyElement(dao.getAllTranslations());
        dictionary.reloadData();

        dictionary.mark(translation, Answer.INCORRECT);

        Collection<AnswerAtTime> recentAnswers = dao.getAnswersLogByTranslationId().values();
        assertThat(getLast(recentAnswers).getAnswer(), is(equalTo(Answer.INCORRECT)));
    }

    @Test
    public void shouldGetNewest100TranslationsWith80PercentProbability() {
        dao.insert(getNTranslationsWithNativeWordStartingWith(100, "Other"));
        dao.insert(getNTranslationsWithNativeWordStartingWith(100, "LastQ"));
        dictionary.reloadData();

        final List<Translation> retrievedTranslations = retrieveTranslationsNTimes(dictionary, 1000);

        int percentage = countPercentageOfRetrievedNativeWordsHadExpectedPattern(retrievedTranslations, "LastQ");
        assertThat(percentage, allOf(greaterThan(75), lessThan(85)));
    }

    @Test
    public void shouldHandle100Translations() {
        for (int i = 0; i < 100; i++) {
            dao.insert(getNTranslationsWithNativeWordStartingWith(100, "Any"));
            dictionary.reloadData();

            List<Translation> translations = LiveDictionaryDsl.retrieveTranslationsNTimes(dictionary, 100);

            int percentage =
                LiveDictionaryDsl.countPercentageOfRetrievedNativeWordsHadExpectedPattern(translations, "Any");
            assertThat(percentage, is(100));
        }
    }

    @Test
    public void shouldNotRetrieveLabelledWords() {
        dao.insertSingle(createForeignToNativeTranslation("la palabra", "a word"));
        dao.insertSingle(createForeignToNativeTranslation("la cocina", "a kitchen"));
        Translation labelledTranslation = retrieveTranslationWithNativeWordFromDb("a kitchen");
        labelDao.addLabelledTranslation(labelledTranslation, Label.A);
        dictionary.reloadData();

        List<Translation> translations = LiveDictionaryDsl.retrieveTranslationsNTimes(dictionary, 10);

        int percentage = LiveDictionaryDsl.countPercentageOfRetrievedNativeWordsHadExpectedPattern(translations, "a word");
        assertThat(percentage, is(100));
    }

    private Translation retrieveTranslationWithNativeWordFromDb(String nativeWord) {
        for (Translation t : dao.getAllTranslations()) {
            if (t.getNativeWord().get().equals(nativeWord)) {
                return t;
            }
        }
        throw new RuntimeException("no such word: '" + nativeWord + "' in database");
    }

    @Test
    public void afterFinding20DifficultTranslationsShouldNeverAskForOthers() {
        dao.insert(getNTranslationsWithNativeWordStartingWith(100, "Other"));
        dao.insert(getNTranslationsWithNativeWordStartingWith(20, "DifficultWord"));
        dictionary.reloadData();
        for (Translation t : new HashSet<>(dao.getAllTranslations())) {
            if (t.getNativeWord().get().contains("DifficultWord")) {
                dictionary.mark(t, Answer.INCORRECT);
            }
        }

        final List<Translation> retrievedTranslations = retrieveTranslationsNTimes(dictionary, 100);

        int percentage =
            countPercentageOfRetrievedNativeWordsHadExpectedPattern(retrievedTranslations, "DifficultWord");
        assertThat(percentage, is(equalTo(100)));
    }

    @Test
    public void diffucultTranslationsWhichAreAlreadyBecameEasyShouldStopBeingAskedEvery20thTime() {
        dao.insert(getNTranslationsWithNativeWordStartingWith(80, "Other"));
        dao.insert(getNTranslationsWithNativeWordStartingWith(10, "DifficultWord"));
        dao.insert(getNTranslationsWithNativeWordStartingWith(10, "WasDifficultButNowEasyWord"));
        dictionary.reloadData();
        for (Translation t : new HashSet<>(dao.getAllTranslations())) {
            if (t.getNativeWord().get().contains("DifficultWord")) {
                dictionary.mark(t, Answer.INCORRECT);
            }
            if (t.getNativeWord().get().contains("WasDifficultButNowEasyWord")) {
                dictionary.mark(t, Answer.INCORRECT);
                dictionary.mark(t, Answer.CORRECT);
            }
        }

        final List<Translation> retrievedTranslations = retrieveTranslationsNTimes(dictionary, 1000);

        int percentage =
            countPercentageOfRetrievedNativeWordsHadExpectedPattern(retrievedTranslations, "DifficultWord");
        assertThat(percentage, allOf(greaterThan(45), lessThan(55)));
    }

    @Test
    public void difficultTranslationsShouldBeAskedEvery20thTime() {
        dao.insert(getNTranslationsWithNativeWordStartingWith(100, "Other"));
        dao.insert(getNTranslationsWithNativeWordStartingWith(10, "DifficultWord"));
        dictionary.reloadData();
        for (Translation t : new HashSet<>(dao.getAllTranslations())) {
            if (t.getNativeWord().get().contains("DifficultWord")) {
                dictionary.mark(t, Answer.INCORRECT);
            }
        }

        final List<Translation> retrievedTranslations = retrieveTranslationsNTimes(dictionary, 1000);

        int percentage =
            countPercentageOfRetrievedNativeWordsHadExpectedPattern(retrievedTranslations, "DifficultWord");
        assertThat(percentage, allOf(greaterThan(45), lessThan(55)));
    }

    @Test
    public void difficultTranslationsShouldBeAskedEvery20thTimeEvenIfTheyWerePassedInitially() {
        dao.insert(getNTranslationsWithNativeWordStartingWith(100, "Other"));
        dao.insert(getNTranslationsWithNativeWordStartingWith(10, "DifficultWord"));
        for (Translation t : new HashSet<>(dao.getAllTranslations())) {
            if (t.getNativeWord().get().contains("DifficultWord")) {
                dao.logAnswer(t, Answer.INCORRECT, new Date());
            }
        }
        dictionary.reloadData();

        final List<Translation> retrievedTranslations = retrieveTranslationsNTimes(dictionary, 1000);

        int percentage =
            countPercentageOfRetrievedNativeWordsHadExpectedPattern(retrievedTranslations, "DifficultWord");
        assertThat(percentage, allOf(greaterThan(45), lessThan(55)));
    }

    @Test
    public void mistakenTranslationShouldBeAsked3TimesToBeRestrictedFromBeingAskedByPromotionPeriod() {
        dao.insertSingle(createForeignToNativeTranslation("palabra", "word"));
        Translation translation = dao.getAllTranslations().get(0);
        dictionary.reloadData();
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
        dictionary.reloadData();
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
        dictionary.reloadData();

        dictionary.mark(easyTranslation, Answer.CORRECT);
        dictionary.mark(easyTranslation, Answer.CORRECT);

        List<Translation> notYetStaged = LiveDictionaryDsl.retrieveTranslationsNTimes(dictionary, 10);
        int percentage = LiveDictionaryDsl.countPercentageOfRetrievedNativeWordsHadExpectedPattern(notYetStaged,
                                                                                                   otherTranslation
                                                                                                       .getNativeWord()
                                                                                                       .get());
        assertThat(percentage, is(equalTo(100)));
    }

    @Test
    public void shouldInsertTranslation() {

        dictionary.insert(createForeignToNativeTranslation("la palabra", "word"));

        assertThat(dao.getAllTranslations().size(), is(equalTo(1)));
        assertThat(dictionary.getRandomTranslation().getForeignWord().get(), is("la palabra"));
    }

    @Test
    public void shouldNotInsertDuplicates() {

        dictionary.insert(createForeignToNativeTranslation("duplicate", "dup_translation"));
        dictionary.insert(createForeignToNativeTranslation("duplicate", "dup_translation"));

        assertThat(dao.getAllTranslations().size(), is(equalTo(1)));
    }

    @Test
    public void shouldDeleteTranslation() {
        dao.insertSingle(createForeignToNativeTranslation("word", "la palabra"));
        Translation translation = dao.getAllTranslations().get(0);
        dictionary.reloadData();

        dictionary.delete(translation);

        assertThat(dao.getAllTranslations(), not(hasItem(translation)));
        try {
            dictionary.getRandomTranslation();
            fail();
        } catch (LiveDictionaryException e) {
            assertThat(e.getMessage(), containsString("no questions found"));
        }
    }

    @Test
    public void answersShouldBeDeletedAlongWithTranslation() {
        dao.insertSingle(createForeignToNativeTranslation("word", "la palabra"));
        Translation translation = dao.getAllTranslations().get(0);
        dictionary.reloadData();
        dictionary.mark(translation, Answer.CORRECT);

        dictionary.delete(translation);

        assertThat(dao.getAnswersLogByTranslationId().get(translation.getId()), empty());
    }

    @Test
    public void shouldNotAllowToAnswerTranslationsWithoutId() {

        boolean isUpdated =
            dictionary.mark(createForeignToNativeTranslation("duplicate", "dup_translation"), Answer.INCORRECT);

        assertThat(isUpdated, is(false));
    }

    @Test
    public void shouldNotAllowToAnswerTranslationsApsentInDb() {
        int nonexistentId = 8949861;

        boolean isUpdated = dictionary
            .mark(new Translation(nonexistentId, new ForeignWord("la duplicado"), new NativeWord("duplication")),
                  Answer.INCORRECT);

        assertThat(isUpdated, is(false));
    }

    @Test
    public void shouldAllowAnswerIncorrectly() {
        dao.insert(singletonList(createForeignToNativeTranslation("word", "la palabra")));
        Translation translation = dao.getAllTranslations().iterator().next();

        boolean isUpdated = dictionary.mark(translation, Answer.INCORRECT);

        assertThat(isUpdated, is(true));
    }

    @Test
    public void shouldUpdateTranslation() {
        dao.insert(singletonList(createForeignToNativeTranslation("la palabra", "word")));
        dictionary.reloadData();
        Translation translation = dao.getAllTranslations().iterator().next();

        boolean isUpdated = dictionary.update(
            new Translation(translation.getId(), new ForeignWord("la palabra cambiada"),
                            new NativeWord("modified word")));

        assertThat(isUpdated, is(true));
        Translation modifiedWord = dao.getAllTranslations().iterator().next();
        assertThat(modifiedWord.getForeignWord().get(), is(equalTo("la palabra cambiada")));
        assertThat(modifiedWord.getNativeWord().get(), is(equalTo("modified word")));
        assertThat(dictionary.getRandomTranslation().getNativeWord().get(), is("modified word"));
    }

    @Test
    public void shouldDeleteIfUpdateEndsUpWithExistingTranslation() {
        dao.insert(singletonList(createForeignToNativeTranslation("la palabra", "word")));
        dao.insert(singletonList(createForeignToNativeTranslation("la cocina", "a kitchen")));
        dictionary.reloadData();
        Translation translation = retrieveTranslationWithNativeWordFromDb("a kitchen");

        boolean isUpdated = dictionary.update(
            new Translation(translation.getId(), new ForeignWord("la palabra"),
                            new NativeWord("word")));

        assertThat(isUpdated, is(true));
        assertThat(dao.getAllTranslations(), hasSize(1));
        Translation modifiedWord = getLast(dao.getAllTranslations());
        assertThat(modifiedWord.getForeignWord().get(), is(equalTo("la palabra")));
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
            translations.add(new Translation(new ForeignWord("desconocido" + getUniqueInt()),
                                             new NativeWord(prefix + getUniqueInt())));
        }
        return translations;
    }

    private int getUniqueInt() {
        return uniqueSequence++;
    }
}
