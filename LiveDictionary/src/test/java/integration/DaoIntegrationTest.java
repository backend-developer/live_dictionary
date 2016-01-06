package integration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import uk.ignas.livedictionary.BuildConfig;
import uk.ignas.livedictionary.LiveDictionaryActivity;
import uk.ignas.livedictionary.core.*;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class DaoIntegrationTest {

    @Test
    public void dbShouldHaveSeedData() {
        TranslationDao dao = createDao();

        List<Translation> allTranslations = dao.getAllTranslations();

        assertThat(allTranslations, hasSize(17));
    }

    @Test
    public void deletingTranslationShouldCascadeDeleteAnswers() {
        TranslationDao dao = createDaoEmpty();
        dao.insertSingle(new Translation(new ForeignWord("la palabra"), new NativeWord("a word")));
        Translation translation = dao.getAllTranslations().get(0);
        dao.logAnswer(translation, Answer.CORRECT, new Date());

        dao.delete(Collections.singleton(translation));

        assertThat(dao.getAllTranslations(), empty());
        assertThat(dao.getAnswersLogByTranslationId().values(), empty());
    }

    private TranslationDao createDaoEmpty() {
        TranslationDao dao = createDao();
        dao.delete(dao.getAllTranslations());
        return dao;
    }

    private TranslationDao createDao() {
        LiveDictionaryActivity activity = Robolectric.setupActivity(LiveDictionaryActivity.class);
        return new TranslationDaoSqlite(activity);
    }
}
