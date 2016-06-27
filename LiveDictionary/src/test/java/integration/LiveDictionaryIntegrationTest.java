package integration;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import uk.ignas.livedictionary.BuildConfig;
import uk.ignas.livedictionary.core.*;
import uk.ignas.livedictionary.core.Dictionary;
import uk.ignas.livedictionary.core.answer.Answer;
import uk.ignas.livedictionary.core.answer.AnswerAtTime;
import uk.ignas.livedictionary.core.answer.AnswerDao;
import uk.ignas.livedictionary.core.label.Label;
import uk.ignas.livedictionary.core.label.LabelDao;
import uk.ignas.livedictionary.core.Labeler;
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

    private TranslationDao translationDao = DaoCreator.cleanDbAndCreateTranslationDao();
    private LabelDao labelDao = DaoCreator.clearDbAndCreateLabelDao();
    private AnswerDao answerDao = DaoCreator.clearDbAndCreateAnswerDao();
    private DaoObjectsFetcher fetcher = new DaoObjectsFetcher(labelDao, answerDao);
    private Labeler labeler = new Labeler(translationDao, fetcher, labelDao);
    private Dictionary dictionary = new Dictionary(translationDao, answerDao, fetcher, labeler, clock);

    @Test
    public void shouldThrowWhenIfThereAreNoTranslationToRetrieve() {
        try {
            dictionary.getRandomTranslation();
            fail();
        } catch (LiveDictionaryException e) {
            assertTrue(e.getMessage().contains("no questions found"));
        }
    }


    @Test
    public void shouldNotCrashWhenThereAreFewTranslations() {
        translationDao.insertSingle(createForeignToNativeTranslation("palabra", "word"));
        dictionary.reloadData();

        Translation translation = dictionary.getRandomTranslation();

        assertThat(translation.getForeignWord().get(), is(equalTo("palabra")));
    }

    @Test
    public void shouldNotCrashWhenAllTheWordsAreIncorrectlyAnswered() {
        translationDao.insertSingle(createForeignToNativeTranslation("palabra", "word"));
        Translation translation = translationDao.getAllTranslations().get(0);
        dictionary.reloadData();
        dictionary.mark(translation, Answer.INCORRECT);

        List<Translation> translations = LiveDictionaryDsl.retrieveTranslationsNTimes(dictionary, 10);

        int percentage =
            LiveDictionaryDsl.countPercentageOfRetrievedNativeWordsHadExpectedPattern(translations, "word");
        assertThat(percentage, is(100));
    }


    @Test
    public void shouldSynchronizeWithDbOnDemand() {
        translationDao.insertSingle(createForeignToNativeTranslation("la palabra", "word"));

        dictionary.reloadData();

        Translation translation = dictionary.getRandomTranslation();
        assertThat(translation.getForeignWord().get(), is(equalTo("la palabra")));
    }

    @Test
    public void shouldPersistDifficultTranslations() {
        translationDao.insertSingle(createForeignToNativeTranslation("palabra", "word"));
        Translation translation = getOnlyElement(translationDao.getAllTranslations());
        dictionary.reloadData();

        dictionary.mark(translation, Answer.INCORRECT);

        Collection<AnswerAtTime> recentAnswers = answerDao.getAnswersLogByTranslationId().values();
        assertThat(getLast(recentAnswers).getAnswer(), is(equalTo(Answer.INCORRECT)));
    }

    @Test
    public void shouldGetNewest100TranslationsWith80PercentProbability() {
        translationDao.insert(getNTranslationsWithNativeWordStartingWith(100, "Other"));
        translationDao.insert(getNTranslationsWithNativeWordStartingWith(100, "LastQ"));
        dictionary.reloadData();

        final List<Translation> retrievedTranslations = retrieveTranslationsNTimes(dictionary, 1000);

        int percentage = countPercentageOfRetrievedNativeWordsHadExpectedPattern(retrievedTranslations, "LastQ");
        assertThat(percentage, allOf(greaterThan(75), lessThan(85)));
    }

    @Test
    public void shouldHandle100Translations() {
        for (int i = 0; i < 100; i++) {
            translationDao.insert(getNTranslationsWithNativeWordStartingWith(100, "Any"));
            dictionary.reloadData();

            List<Translation> translations = LiveDictionaryDsl.retrieveTranslationsNTimes(dictionary, 100);

            int percentage =
                LiveDictionaryDsl.countPercentageOfRetrievedNativeWordsHadExpectedPattern(translations, "Any");
            assertThat(percentage, is(100));
        }
    }

    @Test
    public void shouldNotRetrieveLabelledWords() {
        translationDao.insertSingle(createForeignToNativeTranslation("la palabra", "a word"));
        translationDao.insertSingle(createForeignToNativeTranslation("la cocina", "a kitchen"));
        Translation labelledTranslation = retrieveTranslationWithNativeWordFromDb("a kitchen");
        labelDao.addLabelledTranslation(labelledTranslation.getId(), Label.A);
        dictionary.reloadData();

        List<Translation> translations = LiveDictionaryDsl.retrieveTranslationsNTimes(dictionary, 10);

        int percentage = LiveDictionaryDsl.countPercentageOfRetrievedNativeWordsHadExpectedPattern(translations, "a word");
        assertThat(percentage, is(100));
    }

    private Translation retrieveTranslationWithNativeWordFromDb(String nativeWord) {
        for (Translation t : translationDao.getAllTranslations()) {
            if (t.getNativeWord().get().equals(nativeWord)) {
                return t;
            }
        }
        throw new RuntimeException("no such word: '" + nativeWord + "' in database");
    }

    @Test
    public void afterFinding20DifficultTranslationsShouldNeverAskForOthers() {
        translationDao.insert(getNTranslationsWithNativeWordStartingWith(100, "Other"));
        translationDao.insert(getNTranslationsWithNativeWordStartingWith(20, "DifficultWord"));
        dictionary.reloadData();
        for (Translation t : new HashSet<>(translationDao.getAllTranslations())) {
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
        translationDao.insert(getNTranslationsWithNativeWordStartingWith(80, "Other"));
        translationDao.insert(getNTranslationsWithNativeWordStartingWith(10, "DifficultWord"));
        translationDao.insert(getNTranslationsWithNativeWordStartingWith(10, "WasDifficultButNowEasyWord"));
        dictionary.reloadData();
        for (Translation t : new HashSet<>(translationDao.getAllTranslations())) {
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
        translationDao.insert(getNTranslationsWithNativeWordStartingWith(100, "Other"));
        translationDao.insert(getNTranslationsWithNativeWordStartingWith(10, "DifficultWord"));
        dictionary.reloadData();
        for (Translation t : new HashSet<>(translationDao.getAllTranslations())) {
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
        translationDao.insert(getNTranslationsWithNativeWordStartingWith(100, "Other"));
        translationDao.insert(getNTranslationsWithNativeWordStartingWith(10, "DifficultWord"));
        for (Translation t : new HashSet<>(translationDao.getAllTranslations())) {
            if (t.getNativeWord().get().contains("DifficultWord")) {
                answerDao.logAnswer(t.getId(), Answer.INCORRECT, new Date());
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
        translationDao.insertSingle(createForeignToNativeTranslation("palabra", "word"));
        Translation translation = translationDao.getAllTranslations().get(0);
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
        translationDao.insertSingle(createForeignToNativeTranslation("palabra", "word"));
        Translation translation = translationDao.getAllTranslations().get(0);
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
        translationDao.insertSingle(createForeignToNativeTranslation("la palabra", "word"));
        translationDao.insertSingle(createForeignToNativeTranslation("la frase", "phrase"));
        Translation easyTranslation = translationDao.getAllTranslations().get(0);
        Translation otherTranslation = translationDao.getAllTranslations().get(1);
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

        assertThat(translationDao.getAllTranslations().size(), is(equalTo(1)));
        assertThat(dictionary.getRandomTranslation().getForeignWord().get(), is("la palabra"));
    }

    @Test
    public void shouldNotInsertDuplicates() {

        dictionary.insert(createForeignToNativeTranslation("duplicate", "dup_translation"));
        dictionary.insert(createForeignToNativeTranslation("duplicate", "dup_translation"));

        assertThat(translationDao.getAllTranslations().size(), is(equalTo(1)));
    }

    @Test
    public void shouldDeleteTranslation() {
        translationDao.insertSingle(createForeignToNativeTranslation("word", "la palabra"));
        Translation translation = translationDao.getAllTranslations().get(0);
        dictionary.reloadData();

        dictionary.delete(translation);

        assertThat(translationDao.getAllTranslations(), not(hasItem(translation)));
        try {
            dictionary.getRandomTranslation();
            fail();
        } catch (LiveDictionaryException e) {
            assertThat(e.getMessage(), containsString("no questions found"));
        }
    }

    @Test
    public void answersShouldBeDeletedAlongWithTranslation() {
        translationDao.insertSingle(createForeignToNativeTranslation("word", "la palabra"));
        Translation translation = translationDao.getAllTranslations().get(0);
        dictionary.reloadData();
        dictionary.mark(translation, Answer.CORRECT);

        dictionary.delete(translation);

        assertThat(answerDao.getAnswersLogByTranslationId().get(translation.getId()), empty());
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
        translationDao.insert(singletonList(createForeignToNativeTranslation("word", "la palabra")));
        Translation translation = translationDao.getAllTranslations().iterator().next();

        boolean isUpdated = dictionary.mark(translation, Answer.INCORRECT);

        assertThat(isUpdated, is(true));
    }

    @Test
    public void shouldUpdateTranslation() {
        translationDao.insert(singletonList(createForeignToNativeTranslation("la palabra", "word")));
        dictionary.reloadData();
        Translation translation = translationDao.getAllTranslations().iterator().next();

        boolean isUpdated = dictionary.update(
            new Translation(translation.getId(), new ForeignWord("la palabra cambiada"),
                            new NativeWord("modified word")));

        assertThat(isUpdated, is(true));
        Translation modifiedWord = translationDao.getAllTranslations().iterator().next();
        assertThat(modifiedWord.getForeignWord().get(), is(equalTo("la palabra cambiada")));
        assertThat(modifiedWord.getNativeWord().get(), is(equalTo("modified word")));
        assertThat(dictionary.getRandomTranslation().getNativeWord().get(), is("modified word"));
    }

    @Test
    public void shouldDeleteIfUpdateEndsUpWithExistingTranslation() {
        translationDao.insert(singletonList(createForeignToNativeTranslation("la palabra", "word")));
        translationDao.insert(singletonList(createForeignToNativeTranslation("la cocina", "a kitchen")));
        dictionary.reloadData();
        Translation translation = retrieveTranslationWithNativeWordFromDb("a kitchen");

        boolean isUpdated = dictionary.update(
            new Translation(translation.getId(), new ForeignWord("la palabra"),
                            new NativeWord("word")));

        assertThat(isUpdated, is(true));
        assertThat(translationDao.getAllTranslations(), hasSize(1));
        Translation modifiedWord = getLast(translationDao.getAllTranslations());
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
