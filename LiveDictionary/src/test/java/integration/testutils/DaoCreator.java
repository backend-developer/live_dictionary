package integration.testutils;


import org.robolectric.Robolectric;
import uk.ignas.livedictionary.LiveDictionaryActivity;
import uk.ignas.livedictionary.core.TranslationDao;

public class DaoCreator {
    public static TranslationDao createEmpty() {
        TranslationDao dao = create();
        dao.delete(dao.getAllTranslations());
        return dao;
    }

    public static TranslationDao create() {
        LiveDictionaryActivity activity = Robolectric.setupActivity(LiveDictionaryActivity.class);
        return new TranslationDao(activity);
    }
}
