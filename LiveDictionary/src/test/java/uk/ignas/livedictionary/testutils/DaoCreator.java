package uk.ignas.livedictionary.testutils;


import org.robolectric.Robolectric;
import uk.ignas.livedictionary.LiveDictionaryActivity;
import uk.ignas.livedictionary.core.SqliteTranslationDao;
import uk.ignas.livedictionary.core.TranslationDao;
import uk.ignas.livedictionary.core.answer.AnswerDao;
import uk.ignas.livedictionary.core.answer.SqliteAnswerDao;
import uk.ignas.livedictionary.core.label.LabelDao;
import uk.ignas.livedictionary.core.label.SqliteLabelDao;
import uk.ignas.livedictionary.core.util.DatabaseFacade;

public class DaoCreator {
    public static TranslationDao cleanDbAndCreateTranslationDao() {
        clearDb();
        return createTranslationDao();
    }

    public static AnswerDao clearDbAndCreateAnswerDao() {
        clearDb();
        return createAnswerDao();
    }

    public static LabelDao clearDbAndCreateLabelDao() {
        clearDb();
        return createLabelDao();
    }

    private static void clearDb() {
        TranslationDao translationDao = createTranslationDao();
        translationDao.delete(translationDao.getAllTranslations());
    }

    public static AnswerDao createAnswerDao() {
        DatabaseFacade database = createDatabase();
        return new SqliteAnswerDao(database);
    }

    public static LabelDao createLabelDao() {
        DatabaseFacade databaseFacade = createDatabase();
        return new SqliteLabelDao(databaseFacade);
    }

    public static TranslationDao createTranslationDao() {
        DatabaseFacade databaseFacade = createDatabase();
        LabelDao labelDao = new SqliteLabelDao(databaseFacade);
        AnswerDao answerDao = new SqliteAnswerDao(databaseFacade);
        return new SqliteTranslationDao(labelDao, databaseFacade, answerDao);
    }

    private static DatabaseFacade createDatabase() {
        LiveDictionaryActivity activity = Robolectric.setupActivity(LiveDictionaryActivity.class);
        return new DatabaseFacade(activity);
    }
}
