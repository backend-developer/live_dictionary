package integration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import uk.ignas.livedictionary.BuildConfig;
import uk.ignas.livedictionary.core.*;
import uk.ignas.livedictionary.core.answer.AnswerDao;
import uk.ignas.livedictionary.core.label.Label;
import uk.ignas.livedictionary.core.label.LabelDao;
import uk.ignas.livedictionary.core.Labeler;
import uk.ignas.livedictionary.testutils.DaoCreator;

import java.util.*;

import static com.google.common.collect.Iterables.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static junit.framework.Assert.fail;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class LabelerIntegrationTest {
    private TranslationDao translationDao = DaoCreator.cleanDbAndCreateTranslationDao();
    private LabelDao labelDao = DaoCreator.clearDbAndCreateLabelDao();
    private AnswerDao answerDao = DaoCreator.clearDbAndCreateAnswerDao();
    private Labeler labeler = new Labeler(translationDao, new DaoObjectsFetcher(labelDao, answerDao), labelDao);

    private Translation createForeignToNativeTranslation(String foreignWord, String nativeWord) {
        return new Translation(new ForeignWord(foreignWord), new NativeWord(nativeWord));
    }

    @Test
    public void unlabeledTranslationShouldNotHaveLabel() {
        translationDao.insert(singletonList(createForeignToNativeTranslation("la palabra", "word")));

        Collection<Translation> labelledTranslations = labeler.getLabelled(Label.A);

        assertThat(labelledTranslations, notNullValue());
        assertThat(labelledTranslations, hasSize(0));
    }

    @Test
    public void shouldAddLabel() {
        translationDao.insert(singletonList(createForeignToNativeTranslation("la palabra", "word")));
        Translation translation = translationDao.getAllTranslations().iterator().next();

        labeler.addLabel(translation, Label.A);
        Collection<Translation> labelledTranslations = labeler.getLabelled(Label.A);

        assertThat(labelledTranslations, notNullValue());
        assertThat(labelledTranslations, hasSize(1));
        assertThat(getFirst(labelledTranslations, null).getForeignWord(), is(new ForeignWord("la palabra")));
    }

    @Test
    public void shouldBeAbleToRemoveLabel() {
        translationDao.insert(singletonList(createForeignToNativeTranslation("la palabra", "word")));
        Translation translation = translationDao.getAllTranslations().iterator().next();

        labeler.addLabel(translation, Label.A);
        labeler.removeLabel(translation, Label.A);
        Collection<Translation> labelledTranslations = labeler.getLabelled(Label.A);

        assertThat(labelledTranslations, notNullValue());
        assertThat(labelledTranslations, hasSize(0));
    }

    @Test
    public void shouldBeAbleToRemoveSpecificLabel() {
        translationDao.insert(singletonList(createForeignToNativeTranslation("la palabra", "word")));
        Translation translation = translationDao.getAllTranslations().iterator().next();

        labeler.addLabel(translation, Label.A);
        labeler.addLabel(translation, Label.B);
        labeler.removeLabel(translation, Label.B);
        Collection<Translation> translationsWithLabelA = labeler.getLabelled(Label.A);
        Collection<Translation> translationsWithLabelB = labeler.getLabelled(Label.B);

        assertThat(translationsWithLabelA, notNullValue());
        assertThat(translationsWithLabelA, hasSize(1));
        assertThat(translationsWithLabelB, notNullValue());
        assertThat(translationsWithLabelB, hasSize(0));
    }

    @Test
    public void shouldBeSilentlyIgnoreRemovalOfUnexistingLabel() {
        translationDao.insert(singletonList(createForeignToNativeTranslation("la palabra", "word")));
        Translation translation = translationDao.getAllTranslations().iterator().next();

        labeler.removeLabel(translation, Label.A);
        Collection<Translation> labelledTranslations = labeler.getLabelled(Label.A);

        assertThat(labelledTranslations, notNullValue());
        assertThat(labelledTranslations, hasSize(0));
    }

    @Test
    public void shouldDeleteLabelledWordWithAllLabels() {
        translationDao.insert(singletonList(createForeignToNativeTranslation("la palabra", "word")));
        Translation translation = translationDao.getAllTranslations().iterator().next();

        labeler.addLabel(translation, Label.A);
        labeler.addLabel(translation, Label.B);
        translationDao.delete(asList(translation));
        Collection<Translation> translationsLabelledWithA = labeler.getLabelled(Label.A);
        Collection<Translation> translationsLabelledWithB = labeler.getLabelled(Label.B);

        assertThat(translationsLabelledWithA, notNullValue());
        assertThat(translationsLabelledWithA, hasSize(0));
        assertThat(translationsLabelledWithB, notNullValue());
        assertThat(translationsLabelledWithB, hasSize(0));
    }

    @Test
    public void deletingLabelledWordShouldNotRemoveLabelsFromOtherWords() {
        translationDao.insert(singletonList(createForeignToNativeTranslation("la palabra", "word")));
        translationDao.insert(singletonList(createForeignToNativeTranslation("la cocina", "kitchen")));
        Translation translation1 = getFirst(translationDao.getAllTranslations(), null);
        Translation translation2 = getLast(translationDao.getAllTranslations());

        labeler.addLabel(translation1, Label.A);
        labeler.addLabel(translation2, Label.A);
        translationDao.delete(asList(translation1));
        Collection<Translation> labelledTranslations = labeler.getLabelled(Label.A);

        assertThat(labelledTranslations, notNullValue());
        assertThat(labelledTranslations, hasSize(1));
        assertThat(getFirst(labelledTranslations, null).getNativeWord(), is(new NativeWord("kitchen")));
    }


    @Test
    public void shouldNotAddSameLabelTwice() {
        translationDao.insert(singletonList(createForeignToNativeTranslation("la palabra", "word")));
        Iterator<Translation> iterator = translationDao.getAllTranslations().iterator();
        Translation translation = iterator.next();

        labeler.addLabel(translation, Label.A);
        labeler.addLabel(translation, Label.A);
        Collection<Translation> labelledTranslations = labeler.getLabelled(Label.A);

        assertThat(labelledTranslations, notNullValue());
        assertThat(labelledTranslations, hasSize(1));
    }

    @Test
    public void shouldBeAbleToAddDifferentLabels() {
        translationDao.insert(singletonList(createForeignToNativeTranslation("la palabra", "word")));
        Iterator<Translation> iterator = translationDao.getAllTranslations().iterator();
        Translation translation = iterator.next();

        labeler.addLabel(translation, Label.A);
        labeler.addLabel(translation, Label.B);
        Collection<Translation> translationsWithLabel1 = labeler.getLabelled(Label.A);
        Collection<Translation> translationsWithLabel2 = labeler.getLabelled(Label.B);

        assertThat(translationsWithLabel1, notNullValue());
        assertThat(translationsWithLabel1, hasSize(1));
        assertThat(translationsWithLabel2, notNullValue());
        assertThat(translationsWithLabel2, hasSize(1));
    }

    @Test()
    public void shouldThrowIfNotPersistedTranslationIsPassedIn() {
        Translation translation = createForeignToNativeTranslation("la palabra", "word");

        try {
            labeler.addLabel(translation, Label.A);
            fail();
        } catch (Exception e) {
            //expected
        }
    }
}
