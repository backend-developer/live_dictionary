package integration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import uk.ignas.livedictionary.BuildConfig;
import uk.ignas.livedictionary.core.ForeignWord;
import uk.ignas.livedictionary.core.NativeWord;
import uk.ignas.livedictionary.core.Translation;
import uk.ignas.livedictionary.core.TranslationDao;
import uk.ignas.livedictionary.core.answer.Answer;
import uk.ignas.livedictionary.core.answer.AnswerDao;
import uk.ignas.livedictionary.core.label.Label;
import uk.ignas.livedictionary.core.label.LabelDao;
import uk.ignas.livedictionary.testutils.DaoCreator;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import static com.google.common.collect.Iterables.getLast;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class DaoIntegrationTest {

    @Test
    public void shouldInsertTranslations() {
        TranslationDao translationDao = DaoCreator.cleanDbAndCreateTranslationDao();
        boolean inserted = translationDao
            .insertSingleWithLabels(new Translation(new ForeignWord("la palabra"), new NativeWord("a word")));

        Translation translation = translationDao.getAllTranslations().get(0);

        assertThat(inserted, is(true));
        assertThat(translation.getNativeWord().get(), is(equalTo("a word")));
    }

    @Test
    public void shouldNotInsertDuplicateTranslations() {
        TranslationDao translationDao = DaoCreator.cleanDbAndCreateTranslationDao();
        translationDao
            .insertSingleWithLabels(new Translation(new ForeignWord("la palabra"), new NativeWord("a word")));
        boolean insertedDuplicate = translationDao
            .insertSingleWithLabels(new Translation(new ForeignWord("la palabra"), new NativeWord("a word")));

        Integer numberOfSameRecords = translationDao.getAllTranslations().size();

        assertThat(insertedDuplicate, is(false));
        assertThat(numberOfSameRecords, is(equalTo(1)));
    }

    @Test
    public void shouldInsertAnswerToExistingTranslation() {
        TranslationDao translationDao = DaoCreator.cleanDbAndCreateTranslationDao();
        AnswerDao answersDao = DaoCreator.createAnswerDao();
        translationDao.insertSingleWithLabels(new Translation(new ForeignWord("la palabra"), new NativeWord("a word")));
        Integer translationId = translationDao.getAllTranslations().get(0).getId();

        boolean inserted = answersDao.logAnswer(translationId, Answer.CORRECT, new Date());

        assertThat(inserted, is(true));
    }

    @Test
    public void shouldNotInsertAnswerToNotExistentQuestion() {
        AnswerDao answersDao = DaoCreator.clearDbAndCreateAnswerDao();
        int unexistentTranslationId = 15;

        boolean inserted = answersDao.logAnswer(unexistentTranslationId, Answer.CORRECT, new Date());

        assertThat(inserted, is(false));
    }

    @Test
    public void shouldNotInsertAnswerWithNullTranslationId() {
        AnswerDao answersDao = DaoCreator.clearDbAndCreateAnswerDao();

        boolean inserted = answersDao.logAnswer(null, Answer.CORRECT, new Date());

        assertThat(inserted, is(false));
    }

    @Test
    public void shouldSilentlyIgnoreDeletingWithoutId() {
        TranslationDao translationDao = DaoCreator.cleanDbAndCreateTranslationDao();
        Translation translation = new Translation(new ForeignWord("la palabra"), new NativeWord("a word"));

        translationDao.delete(Collections.singleton(translation));

        assertThat(translationDao.getAllTranslations(), empty());
    }

    @Test
    public void shouldInsertTranslationAlongWithLabels() {
        TranslationDao translationDao = DaoCreator.cleanDbAndCreateTranslationDao();
        LabelDao labelDao = DaoCreator.clearDbAndCreateLabelDao();
        Translation translationToInsert = new Translation(new ForeignWord("la palabra"), new NativeWord("a word"));
        translationToInsert.getMetadata().getLabels().add(Label.C);

        translationDao.insertSingleWithLabels(translationToInsert);

        Translation translation = translationDao.getAllTranslations().get(0);
        Collection<Integer> translationIds = labelDao.getTranslationIdsWithLabel(Label.C);
        assertThat(translationIds, hasSize(1));
        assertThat(getLast(translationIds), notNullValue());
        assertThat(translation.getId(), is(getLast(translationIds)));
    }

    @Test
    public void shouldInsertMultipleTranslationAlongWithLabels() {
        TranslationDao translationDao = DaoCreator.cleanDbAndCreateTranslationDao();
        LabelDao labelDao = DaoCreator.clearDbAndCreateLabelDao();
        Translation translationToInsert = new Translation(new ForeignWord("la palabra"), new NativeWord("a word"));
        translationToInsert.getMetadata().getLabels().add(Label.C);

        translationDao.insert(newArrayList(translationToInsert));

        Translation translation = translationDao.getAllTranslations().get(0);
        Collection<Integer> translationIds = labelDao.getTranslationIdsWithLabel(Label.C);
        assertThat(translationIds, hasSize(1));
        assertThat(getLast(translationIds), notNullValue());
        assertThat(translation.getId(), is(getLast(translationIds)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void updatingWithoutIdShouldThrow() {
        TranslationDao translationDao = DaoCreator.cleanDbAndCreateTranslationDao();
        Translation translationToInsert = new Translation(new ForeignWord("la palabra"), new NativeWord("a word"));
        translationDao.insertSingleWithLabels(translationToInsert);
        Translation translationWithoutId = new Translation(new ForeignWord("palabra nueva"), new NativeWord("new word"));

        translationDao.updateAlongWithLabels(translationWithoutId);
    }

    @Test
    public void updatingTranslationShouldAddLabels() {
        TranslationDao translationDao = DaoCreator.cleanDbAndCreateTranslationDao();
        LabelDao labelDao = DaoCreator.clearDbAndCreateLabelDao();
        Translation translationToInsert = new Translation(new ForeignWord("la palabra"), new NativeWord("a word"));
        translationDao.insertSingleWithLabels(translationToInsert);
        Translation insertedTranslation = getLast(translationDao.getAllTranslations());
        insertedTranslation.getMetadata().getLabels().add(Label.D);

        translationDao.updateAlongWithLabels(insertedTranslation);

        Collection<Integer> translationIds = labelDao.getTranslationIdsWithLabel(Label.D);
        assertThat(translationIds, hasSize(1));
        assertThat(getLast(translationIds), notNullValue());
        assertThat(insertedTranslation.getId(), is(getLast(translationIds)));
    }

    @Test
    public void updatingTranslationShouldDeleteLabels() {
        TranslationDao translationDao = DaoCreator.cleanDbAndCreateTranslationDao();
        LabelDao labelDao = DaoCreator.clearDbAndCreateLabelDao();
        Translation translationToInsert = new Translation(new ForeignWord("la palabra"), new NativeWord("a word"));
        translationToInsert.getMetadata().getLabels().add(Label.C);
        translationDao.insertSingleWithLabels(translationToInsert);
        Translation insertedTranslation = getLast(translationDao.getAllTranslations());
        insertedTranslation.getMetadata().getLabels().remove(Label.C);

        translationDao.updateAlongWithLabels(insertedTranslation);

        Collection<Integer> translationIds = labelDao.getTranslationIdsWithLabel(Label.C);
        assertThat(translationIds, hasSize(0));
    }

    @Test
    public void deletingSingleTranslationShouldCascadeDeleteLabelledTranslations() {
        TranslationDao translationDao = DaoCreator.cleanDbAndCreateTranslationDao();
        LabelDao labelDao = DaoCreator.createLabelDao();
        translationDao.insertSingleWithLabels(new Translation(new ForeignWord("la palabra"), new NativeWord("a word")));
        Translation translation = translationDao.getAllTranslations().get(0);
        labelDao.addLabelledTranslation(translation.getId(), Label.A);

        translationDao.delete(Collections.singleton(translation));

        assertThat(translationDao.getAllTranslations(), empty());
        assertThat(labelDao.getTranslationIdsWithLabel(Label.A), empty());
    }

    @Test
    public void deletingSingleTranslationShouldCascadeDeleteAnswers() {
        TranslationDao translationDao = DaoCreator.cleanDbAndCreateTranslationDao();
        AnswerDao answerDao = DaoCreator.createAnswerDao();
        translationDao.insertSingleWithLabels(new Translation(new ForeignWord("la palabra"), new NativeWord("a word")));
        Translation translation = translationDao.getAllTranslations().get(0);
        answerDao.logAnswer(translation.getId(), Answer.CORRECT, new Date());

        translationDao.delete(Collections.singleton(translation));

        assertThat(translationDao.getAllTranslations(), empty());
        assertThat(answerDao.getAnswersLogByTranslationId().values(), empty());
    }

    @Test
    public void deletingMultipleTranslationShouldCascadeDeleteAnswers() {
        TranslationDao translationDao = DaoCreator.cleanDbAndCreateTranslationDao();
        AnswerDao answerDao = DaoCreator.createAnswerDao();
        translationDao.insertSingleWithLabels(new Translation(new ForeignWord("la palabra"), new NativeWord("a word")));
        translationDao.insertSingleWithLabels(new Translation(new ForeignWord("la otra"), new NativeWord("other")));
        answerDao.logAnswer(translationDao.getAllTranslations().get(0).getId(), Answer.CORRECT, new Date());
        answerDao.logAnswer(translationDao.getAllTranslations().get(1).getId(), Answer.CORRECT, new Date());

        translationDao.delete(translationDao.getAllTranslations());

        assertThat(translationDao.getAllTranslations(), empty());
        assertThat(answerDao.getAnswersLogByTranslationId().values(), empty());
    }
}
