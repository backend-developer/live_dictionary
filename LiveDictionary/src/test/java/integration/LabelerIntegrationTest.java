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

import static com.google.common.collect.Iterables.*;
import static java.util.Collections.singletonList;
import static junit.framework.Assert.fail;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ignas.livedictionary.testutils.LiveDictionaryDsl.countPercentageOfRetrievedNativeWordsHadExpectedPattern;
import static uk.ignas.livedictionary.testutils.LiveDictionaryDsl.retrieveTranslationsNTimes;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class LabelerIntegrationTest {
    private TranslationDao dao = DaoCreator.createEmpty();

    private Translation createForeignToNativeTranslation(String foreignWord, String nativeWord) {
        return new Translation(new ForeignWord(foreignWord), new NativeWord(nativeWord));
    }

    @Test
    public void unlabeledTranslationShouldNotHaveLabel() {
        dao.insert(singletonList(createForeignToNativeTranslation("la palabra", "word")));
        Translation translation = dao.getAllTranslations().iterator().next();
        Labeler labeler = new Labeler(dao);

        Collection<Translation> classified = labeler.getLabelled();

        assertThat(classified, notNullValue());
        assertThat(classified, hasSize(0));
    }

    @Test
    public void shouldAddLabel() {
        dao.insert(singletonList(createForeignToNativeTranslation("la palabra", "word")));
        Translation translation = dao.getAllTranslations().iterator().next();
        Labeler labeler = new Labeler(dao);

        labeler.addLabel(translation);
        Collection<Translation> classified = labeler.getLabelled();

        assertThat(classified, notNullValue());
        assertThat(classified, hasSize(1));
        assertThat(getFirst(classified, null).getForeignWord(), is(new ForeignWord("la palabra")));
    }

    @Test
    public void shouldNotAddSameLabelTwice() {
        dao.insert(singletonList(createForeignToNativeTranslation("la palabra", "word")));
        Iterator<Translation> iterator = dao.getAllTranslations().iterator();
        Translation translation = iterator.next();
        Labeler labeler = new Labeler(dao);

        labeler.addLabel(translation);
        labeler.addLabel(translation);
        Collection<Translation> classified = labeler.getLabelled();

        assertThat(classified, notNullValue());
        assertThat(classified, hasSize(1));
    }

    @Test()
    public void shouldThrowIfNotPersistedTranslationIsPassedIn() {
        Translation translation = createForeignToNativeTranslation("la palabra", "word");
        Labeler labeler = new Labeler(dao);

        try {
            labeler.addLabel(translation);
            fail();
        } catch (Exception e) {
            //expected
        }
    }
}
