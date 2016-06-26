package integration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import uk.ignas.livedictionary.BuildConfig;
import uk.ignas.livedictionary.core.*;
import uk.ignas.livedictionary.core.label.Label;
import uk.ignas.livedictionary.core.label.LabelDao;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Iterables.getLast;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class DaoIntegrationTest {

    @Test
    public void dbShouldHaveSeedData() {
        TranslationDao dao = DaoCreator.createTranslationDao();

        List<Translation> allTranslations = dao.getAllTranslations();

        assertThat(allTranslations, hasSize(17));
    }

    @Test
    public void shouldSilentlyIgnoreDeletingWithoutId() {
        TranslationDao dao = DaoCreator.createEmpty();
        Translation translation = new Translation(new ForeignWord("la palabra"), new NativeWord("a word"));

        dao.delete(Collections.singleton(translation));

        assertThat(dao.getAllTranslations(), empty());
    }

    @Test
    public void shouldInsertTranslationAlongWithLabels() {
        TranslationDao dao = DaoCreator.createEmpty();
        LabelDao labelDao = DaoCreator.clearDbAndCreateLabelDao();
        Translation translationToInsert = new Translation(new ForeignWord("la palabra"), new NativeWord("a word"));
        translationToInsert.getMetadata().getLabels().add(Label.C);

        dao.insertSingle(translationToInsert);

        Translation translation = dao.getAllTranslations().get(0);
        Collection<Integer> translationIds = labelDao.getTranslationIdsWithLabel(Label.C);
        assertThat(translationIds, hasSize(1));
        assertThat(getLast(translationIds), notNullValue());
        assertThat(translation.getId(), is(getLast(translationIds)));
    }

    @Test
    public void updatingTranslationShouldAddLabels() {
        TranslationDao dao = DaoCreator.createEmpty();
        LabelDao labelDao = DaoCreator.clearDbAndCreateLabelDao();
        Translation translationToInsert = new Translation(new ForeignWord("la palabra"), new NativeWord("a word"));
        dao.insertSingle(translationToInsert);
        Translation insertedTranslation = getLast(dao.getAllTranslations());
        insertedTranslation.getMetadata().getLabels().add(Label.D);

        dao.update(insertedTranslation);

        Collection<Integer> translationIds = labelDao.getTranslationIdsWithLabel(Label.D);
        assertThat(translationIds, hasSize(1));
        assertThat(getLast(translationIds), notNullValue());
        assertThat(insertedTranslation.getId(), is(getLast(translationIds)));
    }

    @Test
    public void updatingTranslationShouldDeleteLabels() {
        TranslationDao dao = DaoCreator.createEmpty();
        LabelDao labelDao = DaoCreator.clearDbAndCreateLabelDao();
        Translation translationToInsert = new Translation(new ForeignWord("la palabra"), new NativeWord("a word"));
        translationToInsert.getMetadata().getLabels().add(Label.C);
        dao.insertSingle(translationToInsert);
        Translation insertedTranslation = getLast(dao.getAllTranslations());
        insertedTranslation.getMetadata().getLabels().remove(Label.C);

        dao.update(insertedTranslation);

        Collection<Integer> translationIds = labelDao.getTranslationIdsWithLabel(Label.C);
        assertThat(translationIds, hasSize(0));
    }

    @Test
    public void deletingSingleTranslationShouldCascadeDeleteAnswers() {
        TranslationDao dao = DaoCreator.createEmpty();
        AnswerDao answerDao = DaoCreator.createAnswerDao();
        dao.insertSingle(new Translation(new ForeignWord("la palabra"), new NativeWord("a word")));
        Translation translation = dao.getAllTranslations().get(0);
        answerDao.logAnswer(translation, Answer.CORRECT, new Date());

        dao.delete(Collections.singleton(translation));

        assertThat(dao.getAllTranslations(), empty());
        assertThat(answerDao.getAnswersLogByTranslationId().values(), empty());
    }

    @Test
    public void deletingMultipleTranslationShouldCascadeDeleteAnswers() {
        TranslationDao dao = DaoCreator.createEmpty();
        AnswerDao answerDao = DaoCreator.createAnswerDao();
        dao.insertSingle(new Translation(new ForeignWord("la palabra"), new NativeWord("a word")));
        dao.insertSingle(new Translation(new ForeignWord("la otra"), new NativeWord("other")));
        answerDao.logAnswer(dao.getAllTranslations().get(0), Answer.CORRECT, new Date());
        answerDao.logAnswer(dao.getAllTranslations().get(1), Answer.CORRECT, new Date());

        dao.delete(dao.getAllTranslations());

        assertThat(dao.getAllTranslations(), empty());
        assertThat(answerDao.getAnswersLogByTranslationId().values(), empty());
    }


}
