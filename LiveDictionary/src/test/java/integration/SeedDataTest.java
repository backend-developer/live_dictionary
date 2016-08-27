package integration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import uk.ignas.livedictionary.BuildConfig;
import uk.ignas.livedictionary.core.Translation;
import uk.ignas.livedictionary.core.TranslationDao;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class SeedDataTest {
    @Test
    public void dbShouldHaveSeedData() {
        TranslationDao translationDao = DaoCreator.createTranslationDao(false);

        List<Translation> allTranslations = translationDao.getAllTranslations();

        assertThat(allTranslations, hasSize(17));
    }
}
