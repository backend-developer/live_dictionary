package integration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import uk.ignas.livedictionary.BuildConfig;
import uk.ignas.livedictionary.core.*;
import uk.ignas.livedictionary.core.answer.Answer;
import uk.ignas.livedictionary.core.answer.AnswerAtTime;
import uk.ignas.livedictionary.core.answer.AnswerDao;
import uk.ignas.livedictionary.core.label.Label;
import uk.ignas.livedictionary.core.label.LabelDao;

import java.util.*;

import static com.google.common.collect.Iterables.getLast;
import static com.google.common.collect.Iterables.getOnlyElement;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static uk.ignas.livedictionary.testutils.LiveDictionaryDsl.createForeignToNativeTranslation;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class DaoObjectsFetcherTest {

    private TranslationDao dao = DaoCreator.createEmpty();

    private AnswerDao answerDao = DaoCreator.clearDbAndCreateAnswerDao();

    private LabelDao labelDao = DaoCreator.clearDbAndCreateLabelDao();

    private DaoObjectsFetcher fetcher = new DaoObjectsFetcher(labelDao, answerDao);

    public static final int ID1 = 1;

    public static final int ID2 = 2;

    @Test
    public void shouldNotFetchObjectWithoutId() {
        List<Translation> translationsWithoutId = asList(createForeignToNativeTranslation("la palabra", "a word"));

        try {
            fetcher.fetchLabels(translationsWithoutId);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void shouldNotFetchLabelsIfThereAreNoOnesInDb() {
        Translation translation = new Translation(ID1, createForeignToNativeTranslation("la palabra", "a word"));

        fetcher.fetchLabels(asList(translation));

        assertThat(translation.getMetadata().getLabels(), empty());
    }

    @Test
    public void shouldFetchLabel() {
        Translation translation = new Translation(ID1, createForeignToNativeTranslation("la palabra", "a word"));
        dao.insertSingle(translation);
        labelDao.addLabelledTranslation(translation.getId(), Label.A);

        fetcher.fetchLabels(asList(translation));

        assertThat(translation.getMetadata().getLabels(), hasSize(1));
        assertThat(translation.getMetadata().getLabels(), contains(Label.A));
    }

    @Test
    public void shouldFetchMultipleLabels() {
        Translation translation = new Translation(ID1, createForeignToNativeTranslation("la palabra", "a word"));
        dao.insertSingle(translation);
        labelDao.addLabelledTranslation(translation.getId(), Label.A);
        labelDao.addLabelledTranslation(translation.getId(), Label.B);

        fetcher.fetchLabels(asList(translation));

        assertThat(translation.getMetadata().getLabels(), hasSize(2));
        assertThat(translation.getMetadata().getLabels(), containsInAnyOrder(Label.A, Label.B));
    }

    @Test
    public void shouldFetchLabelsForMultipleTranslations() {
        Translation translation1 = new Translation(ID1, createForeignToNativeTranslation("la palabra", "a word"));
        Translation translation2 = new Translation(ID2, createForeignToNativeTranslation("la cocina", "a kitchen"));
        dao.insertSingle(translation1);
        dao.insertSingle(translation2);
        labelDao.addLabelledTranslation(translation1.getId(), Label.A);
        labelDao.addLabelledTranslation(translation2.getId(), Label.B);

        fetcher.fetchLabels(asList(translation1, translation2));

        assertThat(translation1.getMetadata().getLabels(), hasSize(1));
        assertThat(translation1.getMetadata().getLabels(), contains(Label.A));
        assertThat(translation2.getMetadata().getLabels(), hasSize(1));
        assertThat(translation2.getMetadata().getLabels(), contains(Label.B));
    }

    @Test
    public void shouldThrowForDataHavingInvalidIds() {
        Translation translation1 = new Translation(ID1, createForeignToNativeTranslation("la palabra", "a word"));
        Translation translation2 = new Translation(ID1, createForeignToNativeTranslation("la cocina", "a kitchen"));
        dao.insertSingle(translation1);
        dao.insertSingle(translation2);

        try {
            fetcher.fetchLabels(asList(translation1, translation2));
            fail();
        } catch (IllegalArgumentException e) {

        }
    }

    @Test
    public void shouldNotFetchAnswersLogWhenTranslationsAreProvidedWithoutId() {
        List<Translation> translationsWithoutId = asList(createForeignToNativeTranslation("la palabra", "a word"));

        try {
            fetcher.fetchAnswersLog(translationsWithoutId);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void shouldNotFetchAnswersLogWhenThereIsNoAnswersLogged() {
        List<Translation> translationsWithoutId = asList(new Translation(ID1, createForeignToNativeTranslation("la palabra", "a word")));

        fetcher.fetchAnswersLog(translationsWithoutId);

        assertThat(translationsWithoutId.get(0).getMetadata().getRecentAnswers(), hasSize(0));
    }

    @Test
    public void shouldFetchAnswersLogForMultipleTranslations() {
        Translation translation1 = new Translation(ID1, createForeignToNativeTranslation("la palabra", "a word"));
        Translation translation2 = new Translation(ID2, createForeignToNativeTranslation("la cocina", "a kitchen"));
        dao.insertSingle(translation1);
        dao.insertSingle(translation2);
        answerDao.logAnswer(translation1.getId(), Answer.CORRECT, new Date());
        answerDao.logAnswer(translation2.getId(), Answer.INCORRECT, new Date());

        fetcher.fetchAnswersLog(asList(translation1, translation2));

        TranslationMetadata metadata1 = translation1.getMetadata();
        TranslationMetadata metadata2 = translation2.getMetadata();
        assertThat(metadata1.getRecentAnswers(), hasSize(1));
        assertThat(mapToAnswers(metadata1.getRecentAnswers()), contains(Answer.CORRECT));
        assertThat(metadata2.getRecentAnswers(), hasSize(1));
        assertThat(mapToAnswers(metadata2.getRecentAnswers()), contains(Answer.INCORRECT));
    }

    @Test
    public void shouldFetchMultipleAnswersForTranslation() {
        Translation translation = new Translation(ID1, createForeignToNativeTranslation("la palabra", "a word"));
        dao.insertSingle(translation);
        answerDao.logAnswer(translation.getId(), Answer.CORRECT, new Date());
        answerDao.logAnswer(translation.getId(), Answer.INCORRECT, new Date());

        fetcher.fetchAnswersLog(asList(translation));

        TranslationMetadata metadata1 = translation.getMetadata();
        assertThat(metadata1.getRecentAnswers(), hasSize(2));
        assertThat(mapToAnswers(metadata1.getRecentAnswers()), containsInAnyOrder(Answer.CORRECT, Answer.INCORRECT));
    }

    private List<Answer> mapToAnswers(List<AnswerAtTime> answersAtTime) {
        List<Answer> answers = new ArrayList<>();
        for (AnswerAtTime a : answersAtTime) {
            answers.add(a.getAnswer());
        }
        return answers;
    }
}
