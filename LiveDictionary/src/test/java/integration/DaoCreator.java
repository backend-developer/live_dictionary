package integration;


import org.robolectric.Robolectric;
import uk.ignas.livedictionary.LiveDictionaryActivity;
import uk.ignas.livedictionary.core.AnswerDao;
import uk.ignas.livedictionary.core.util.Dao;
import uk.ignas.livedictionary.core.label.LabelDao;
import uk.ignas.livedictionary.core.TranslationDao;

class DaoCreator {
    static TranslationDao createEmpty() {
        clearDb();
        return createTranslationDao();
    }

    static AnswerDao clearDbAndCreateAnswerDao() {
        clearDb();
        return createAnswerDao();
    }

    static LabelDao clearDbAndCreateLabelDao() {
        clearDb();
        return createLabelDao();
    }

    private static void clearDb() {
        TranslationDao translationDao = createTranslationDao();
        translationDao.delete(translationDao.getAllTranslations());
    }

    static AnswerDao createAnswerDao() {
        Dao database = createDatabase();
        return new AnswerDao(database);
    }

    static LabelDao createLabelDao() {
        Dao dao = createDatabase();
        return new LabelDao(dao);
    }

    static TranslationDao createTranslationDao() {
        Dao dao = createDatabase();
        LabelDao labelDao = new LabelDao(dao);
        AnswerDao answerDao = new AnswerDao(dao);
        return new TranslationDao(labelDao, dao, answerDao);
    }

    private static Dao createDatabase() {
        LiveDictionaryActivity activity = Robolectric.setupActivity(LiveDictionaryActivity.class);
        return new Dao(activity);
    }
}
