package integration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import uk.ignas.livedictionary.BuildConfig;
import uk.ignas.livedictionary.core.*;
import uk.ignas.livedictionary.core.answer.Answer;
import uk.ignas.livedictionary.core.answer.AnswerDao;
import uk.ignas.livedictionary.core.label.Label;
import uk.ignas.livedictionary.core.label.LabelDao;
import uk.ignas.livedictionary.testutils.DaoCreator;
import uk.ignas.livedictionary.testutils.LiveDictionaryDsl;

import java.util.List;

import static com.google.common.collect.Iterables.getLast;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.ignas.livedictionary.testutils.LiveDictionaryDsl.createForeignToNativeTranslation;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class LiveDictionaryIntegrationTest {

    private Clock clock = new Clock();

    private TranslationDao translationDao;
    private LabelDao labelDao;
    private AnswerDao answerDao;
    private DaoObjectsFetcher fetcher;
    private Labeler labeler;
    private Dictionary dictionary;

    @Before
    public void setup() {
        translationDao = DaoCreator.cleanDbAndCreateTranslationDao();
        labelDao = DaoCreator.clearDbAndCreateLabelDao();
        answerDao = DaoCreator.clearDbAndCreateAnswerDao();
        fetcher = new DaoObjectsFetcher(labelDao, answerDao);
        labeler = new Labeler(translationDao, fetcher, labelDao);
        dictionary = new Dictionary(translationDao, answerDao, fetcher, labeler, clock, new SequentialSelectionStrategy());
    }

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
    public void shouldInstructStrategyWithAnswersOfTranslation() {
        ArgumentCaptor<List<Translation>> translationCaptor = ArgumentCaptor.forClass((Class)List.class);
        TranslationSelectionStrategy strategy = mock(TranslationSelectionStrategy.class);
        Dictionary dictionary = new Dictionary(translationDao, answerDao, fetcher, labeler, clock, strategy);
        translationDao.insertSingleWithLabels(createForeignToNativeTranslation("palabra", "word"));
        Translation translation = translationDao.getAllTranslations().get(0);
        dictionary.reloadData();

        dictionary.mark(translation, Answer.INCORRECT);

        verify(strategy, atLeastOnce()).updateState(translationCaptor.capture());
        assertThat(translationCaptor.getAllValues(), hasSize(greaterThanOrEqualTo(1)));
        List<Translation> lastStateUpdate = getLast(translationCaptor.getAllValues());
        Translation theOnlyWord = lastStateUpdate.get(0);
        assertThat(theOnlyWord.getMetadata(), org.hamcrest.Matchers.notNullValue());
        assertThat(getLast(theOnlyWord.getMetadata().getRecentAnswers()).getAnswer(), is(Answer.INCORRECT));
    }

    @Test
    public void shouldSynchronizeWithDbOnDemand() {
        translationDao.insertSingleWithLabels(createForeignToNativeTranslation("la palabra", "word"));

        dictionary.reloadData();

        Translation translation = dictionary.getRandomTranslation();
        assertThat(translation.getForeignWord().get(), is(equalTo("la palabra")));
    }

    @Test
    public void shouldNotRetrieveLabelledByAWords() {
        translationDao.insertSingleWithLabels(createForeignToNativeTranslation("la palabra", "a word"));
        translationDao.insertSingleWithLabels(createForeignToNativeTranslation("la cocina", "a kitchen"));
        Translation labelledTranslation = retrieveTranslationWithNativeWordFromDb("a kitchen");
        labelDao.addLabelledTranslation(labelledTranslation.getId(), Label.A);
        dictionary.reloadData();

        List<Translation> translations = LiveDictionaryDsl.retrieveTranslationsNTimes(dictionary, 10);

        int percentage = LiveDictionaryDsl.countPercentageOfRetrievedNativeWordsHadExpectedPattern(translations, "a word");
        assertThat(percentage, is(100));
    }

    @Test
    public void shouldNotRetrieveLabelledByBWords() {
        translationDao.insertSingleWithLabels(createForeignToNativeTranslation("la palabra", "a word"));
        translationDao.insertSingleWithLabels(createForeignToNativeTranslation("la cocina", "a kitchen"));
        Translation labelledTranslation = retrieveTranslationWithNativeWordFromDb("a kitchen");
        labelDao.addLabelledTranslation(labelledTranslation.getId(), Label.B);
        dictionary.reloadData();

        List<Translation> translations = LiveDictionaryDsl.retrieveTranslationsNTimes(dictionary, 10);

        int percentage = LiveDictionaryDsl.countPercentageOfRetrievedNativeWordsHadExpectedPattern(translations, "a word");
        assertThat(percentage, is(100));
    }

    @Test
    public void shouldDeleteTranslation() {
        translationDao.insertSingleWithLabels(createForeignToNativeTranslation("word", "la palabra"));
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

    private Translation retrieveTranslationWithNativeWordFromDb(String nativeWord) {
        for (Translation t : translationDao.getAllTranslations()) {
            if (t.getNativeWord().get().equals(nativeWord)) {
                return t;
            }
        }
        throw new RuntimeException("no such word: '" + nativeWord + "' in database");
    }

}
