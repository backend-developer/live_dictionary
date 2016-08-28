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

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import static com.google.common.collect.Iterables.getLast;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public  class DaoIntegrationTest {

    private final boolean isStub = false;

    @Test
    public void shouldSilentlyIgnoreDeletingWithoutId() {
        TranslationDao translationDao = DaoCreator.cleanDbAndCreateTranslationDao(isStub);
        Translation translation = new Translation(new ForeignWord("la palabra"), new NativeWord("a word"));

        translationDao.delete(Collections.singleton(translation));

        assertThat(translationDao.getAllTranslations(), empty());
    }

    @Test
    public void shouldInsertTranslationAlongWithLabels() {
        TranslationDao translationDao = DaoCreator.cleanDbAndCreateTranslationDao(false);
        LabelDao labelDao = DaoCreator.clearDbAndCreateLabelDao();
        Translation translationToInsert = new Translation(new ForeignWord("la palabra"), new NativeWord("a word"));
        translationToInsert.getMetadata().getLabels().add(Label.C);

        translationDao.insertSingle(translationToInsert);

        Translation translation = translationDao.getAllTranslations().get(0);
        Collection<Integer> translationIds = labelDao.getTranslationIdsWithLabel(Label.C);
        assertThat(translationIds, hasSize(1));
        assertThat(getLast(translationIds), notNullValue());
        assertThat(translation.getId(), is(getLast(translationIds)));
    }

    @Test
    public void updatingTranslationShouldAddLabels() {
        TranslationDao translationDao = DaoCreator.cleanDbAndCreateTranslationDao(false);
        LabelDao labelDao = DaoCreator.clearDbAndCreateLabelDao();
        Translation translationToInsert = new Translation(new ForeignWord("la palabra"), new NativeWord("a word"));
        translationDao.insertSingle(translationToInsert);
        Translation insertedTranslation = getLast(translationDao.getAllTranslations());
        insertedTranslation.getMetadata().getLabels().add(Label.D);

        translationDao.update(insertedTranslation);

        Collection<Integer> translationIds = labelDao.getTranslationIdsWithLabel(Label.D);
        assertThat(translationIds, hasSize(1));
        assertThat(getLast(translationIds), notNullValue());
        assertThat(insertedTranslation.getId(), is(getLast(translationIds)));
    }

    @Test
    public void updatingTranslationShouldDeleteLabels() {
        TranslationDao translationDao = DaoCreator.cleanDbAndCreateTranslationDao(false);
        LabelDao labelDao = DaoCreator.clearDbAndCreateLabelDao();
        Translation translationToInsert = new Translation(new ForeignWord("la palabra"), new NativeWord("a word"));
        translationToInsert.getMetadata().getLabels().add(Label.C);
        translationDao.insertSingle(translationToInsert);
        Translation insertedTranslation = getLast(translationDao.getAllTranslations());
        insertedTranslation.getMetadata().getLabels().remove(Label.C);

        translationDao.update(insertedTranslation);

        Collection<Integer> translationIds = labelDao.getTranslationIdsWithLabel(Label.C);
        assertThat(translationIds, hasSize(0));
    }

    @Test
    public void deletingSingleTranslationShouldCascadeDeleteAnswers() {
        TranslationDao translationDao = DaoCreator.cleanDbAndCreateTranslationDao(false);
        AnswerDao answerDao = DaoCreator.createAnswerDao();
        translationDao.insertSingle(new Translation(new ForeignWord("la palabra"), new NativeWord("a word")));
        Translation translation = translationDao.getAllTranslations().get(0);
        answerDao.logAnswer(translation.getId(), Answer.CORRECT, new Date());

        translationDao.delete(Collections.singleton(translation));

        assertThat(translationDao.getAllTranslations(), empty());
        assertThat(answerDao.getAnswersLogByTranslationId().values(), empty());
    }

    @Test
    public void deletingMultipleTranslationShouldCascadeDeleteAnswers() {
        TranslationDao translationDao = DaoCreator.cleanDbAndCreateTranslationDao(false);
        AnswerDao answerDao = DaoCreator.createAnswerDao();
        translationDao.insertSingle(new Translation(new ForeignWord("la palabra"), new NativeWord("a word")));
        translationDao.insertSingle(new Translation(new ForeignWord("la otra"), new NativeWord("other")));
        answerDao.logAnswer(translationDao.getAllTranslations().get(0).getId(), Answer.CORRECT, new Date());
        answerDao.logAnswer(translationDao.getAllTranslations().get(1).getId(), Answer.CORRECT, new Date());

        translationDao.delete(translationDao.getAllTranslations());

        assertThat(translationDao.getAllTranslations(), empty());
        assertThat(answerDao.getAnswersLogByTranslationId().values(), empty());
    }


}
