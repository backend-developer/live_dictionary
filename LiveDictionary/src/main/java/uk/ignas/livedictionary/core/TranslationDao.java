package uk.ignas.livedictionary.core;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import org.apache.commons.lang.BooleanUtils;
import uk.ignas.livedictionary.core.answer.AnswerAtTime;
import uk.ignas.livedictionary.core.answer.AnswerDao;
import uk.ignas.livedictionary.core.label.Label;
import uk.ignas.livedictionary.core.label.LabelDao;
import uk.ignas.livedictionary.core.util.DatabaseFacade;
import uk.ignas.livedictionary.core.util.Transactable;

import java.util.*;

import static java.util.Arrays.asList;
import static org.apache.commons.lang.Validate.notNull;

public class TranslationDao {

    public static final int ERROR_OCURRED = -1;

    private final LabelDao labelDao;

    private final DatabaseFacade databaseFacade;

    private final AnswerDao answerDao;

    public static class Translations {
        public static final String TABLE_NAME = "translations";

        public static final String ID = "id";

        public static final String NATIVE_WORD = "nativeWord";

        public static final String FOREIGN_WORD = "foreignWord";
    }



    public TranslationDao(LabelDao labelDao, DatabaseFacade databaseFacade, AnswerDao answerDao) {
        this.labelDao = labelDao;
        this.databaseFacade = databaseFacade;
        this.answerDao = answerDao;
    }


    public void insert(final List<Translation> translations) {
        Transactable<Void> runnable = new Transactable<Void>() {
            public Void perform() {
                for (Translation translation : translations) {
                    if (!insertSingle(translation)) {
                        throw new RuntimeException("could not insert all values");
                    }
                }
                return null;
            }
        };
        databaseFacade.doInTransaction(runnable);
    }

    public boolean insertSingle(final Translation translation) {
        Transactable<Boolean> transactable = new Transactable<Boolean>() {
            public Boolean perform() {
                long id = insertSingleUsingDb(translation);
                boolean result = (id != ERROR_OCURRED);
                if (result) {
                    for (Label label : translation.getMetadata().getLabels()) {
                        final Translation translation1 = new Translation((int) id, translation);
                        labelDao.addLabelledTranslation(translation1.getId(), label);
                    }
                }
                return result;
            }
        };

        return BooleanUtils.isTrue(databaseFacade.doInTransaction(transactable));
    }

    private long insertSingleUsingDb(Translation translation) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Translations.NATIVE_WORD, translation.getNativeWord().get());
        contentValues.put(Translations.FOREIGN_WORD, translation.getForeignWord().get());
        return databaseFacade.insert(Translations.TABLE_NAME, contentValues);
    }


    public int update(final Translation translation) {
        Transactable<Integer> transactable = new Transactable<Integer>() {
            @Override
            public Integer perform() {
                int result;
                notNull(translation.getId());
                try {
                    int numOfTranslationsUpdated = updateSingleTranslation(translation);
                    result = numOfTranslationsUpdated;
                } catch (SQLiteConstraintException e) {
                    deleteById(translation.getId());
                    return 1;
                }
                labelDao.deleteLabelledTranslationsByTranslationIds(asList(translation.getId()));
                for (Label l : translation.getMetadata().getLabels()) {
                    labelDao.addLabelledTranslation(translation.getId(), l);
                }
                return result;
            }
        };

        return databaseFacade.doInTransaction(transactable);
    }

    private int updateSingleTranslation(Translation translation) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Translations.NATIVE_WORD, translation.getNativeWord().get());
        contentValues.put(Translations.FOREIGN_WORD, translation.getForeignWord().get());
        return databaseFacade.update(Translations.TABLE_NAME, contentValues, Translations.ID + " = ? ",
                          new String[]{String.valueOf(translation.getId())});
    }

    public void delete(final Collection<Translation> translations) {
        Transactable<Void> transactable = new Transactable<Void>() {
            @Override
            public Void perform() {
                List<Integer> ids = collectIds(translations);
                ids.removeAll(Collections.singleton(null));
                answerDao.deleteAnswersByTranslationIds(ids);
                labelDao.deleteLabelledTranslationsByTranslationIds(ids);
                for (Translation translation : translations) {
                    deleteSingle(translation);
                }
                return null;
            }
        };
        databaseFacade.doInTransaction(transactable);
    }




    private List<Integer> collectIds(Collection<Translation> translations) {
        List<Integer> translationIdsToDelete = new ArrayList<>();
        for (Translation translation : translations) {
            translationIdsToDelete.add(translation.getId());
        }
        return translationIdsToDelete;
    }

    private Integer deleteById(int id) {
        String tableName = Translations.TABLE_NAME;
        String condition = Translations.ID + " = ? ";
        String[] args = {String.valueOf(id)};
        return databaseFacade.delete(tableName, condition, args);
    }

    private Integer deleteSingle(Translation translation) {
        if (translation.getId() != null) {
            return deleteById(translation.getId());
        } else {
            return 0;
        }
    }

    public List<Translation> getAllTranslations() {
        List<Translation> translations = new ArrayList<>();

        Cursor res = null;
        try {
            String query = "select " +
                           Translations.ID + ", " +
                           Translations.FOREIGN_WORD + ", " +
                           Translations.NATIVE_WORD +
                           " from " + Translations.TABLE_NAME;
            res = databaseFacade.rawQuery(query);
            res.moveToFirst();

            while (!res.isAfterLast()) {
                translations.add(new Translation(res.getInt(res.getColumnIndex(Translations.ID)), new ForeignWord(
                    res.getString(res.getColumnIndex(Translations.FOREIGN_WORD))), new NativeWord(
                    res.getString(res.getColumnIndex(Translations.NATIVE_WORD))),
                                                 new TranslationMetadata(new ArrayList<AnswerAtTime>())));
                res.moveToNext();
            }
        } finally {
            if (res != null) {
                res.close();
            }
        }

        return translations;
    }




}
