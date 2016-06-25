package integration;


import org.robolectric.Robolectric;
import uk.ignas.livedictionary.LiveDictionaryActivity;
import uk.ignas.livedictionary.core.Dao;
import uk.ignas.livedictionary.core.LabelDao;
import uk.ignas.livedictionary.core.TranslationDao;

class DaoCreator {
    static TranslationDao createEmpty() {
        clearDb();
        return createTranslationDao();
    }

    static LabelDao clearDbAndCreateLabelDao() {
        clearDb();
        return createLabelDao();
    }

    private static void clearDb() {
        TranslationDao translationDao = createTranslationDao();
        translationDao.delete(translationDao.getAllTranslations());
    }

    static LabelDao createLabelDao() {
        Dao dao = createDatabase();
        return new LabelDao(dao);
    }

    static TranslationDao createTranslationDao() {
        Dao dao = createDatabase();
        LabelDao labelDao = new LabelDao(dao);
        return new TranslationDao(labelDao, dao);
    }

    private static Dao createDatabase() {
        LiveDictionaryActivity activity = Robolectric.setupActivity(LiveDictionaryActivity.class);
        return new Dao(activity);
    }
}
