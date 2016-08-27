package integration;


import org.robolectric.Robolectric;
import uk.ignas.livedictionary.LiveDictionaryActivity;
import uk.ignas.livedictionary.core.*;
import uk.ignas.livedictionary.core.answer.AnswerDao;
import uk.ignas.livedictionary.core.util.DatabaseFacade;
import uk.ignas.livedictionary.core.label.LabelDao;

class DaoCreator {
    static TranslationDao cleanDbAndCreateTranslationDao(boolean isStub) {
        clearDb();
        return createTranslationDao(false);
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
        TranslationDao translationDao = createTranslationDao(false);
        translationDao.delete(translationDao.getAllTranslations());
    }

    static AnswerDao createAnswerDao() {
        DatabaseFacade database = createDatabase();
        return new AnswerDao(database);
    }

    static LabelDao createLabelDao() {
        DatabaseFacade databaseFacade = createDatabase();
        return new LabelDao(databaseFacade);
    }

    static TranslationDao createTranslationDao(boolean isStub) {
        if (isStub) {
            LabelDaoStub labelDao = new LabelDaoStub();
            AnswewrDaoStub answewrDaoStub = new AnswewrDaoStub();
            return new TranslationDaoStub(labelDao, answewrDaoStub);
        } else {
            DatabaseFacade databaseFacade = createDatabase();
            LabelDao labelDao = new LabelDao(databaseFacade);
            AnswerDao answerDao = new AnswerDao(databaseFacade);
            return new SqliteTranslationDao(labelDao, databaseFacade, answerDao);
        }
    }

    private static DatabaseFacade createDatabase() {
        LiveDictionaryActivity activity = Robolectric.setupActivity(LiveDictionaryActivity.class);
        return new DatabaseFacade(activity);
    }
}
