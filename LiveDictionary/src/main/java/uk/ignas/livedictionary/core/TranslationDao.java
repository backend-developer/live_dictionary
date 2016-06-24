package uk.ignas.livedictionary.core;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.apache.commons.lang.BooleanUtils;

import java.util.*;

import static java.util.Arrays.asList;
import static org.apache.commons.lang.Validate.notNull;

public class TranslationDao {

    public static final int ERROR_OCURRED = -1;

    private final Dao dao;

    public static class AnswersLog {

        public static final String TABLE_NAME = "answers_log";

        public static final String ID = "id";

        public static final String TRANSLATION_ID = "translation_id";

        public static final String TIME_ANSWERED = "time_answered";

        public static final String IS_CORRECT = "is_correct";
    }

    public static class Translations {
        public static final String TABLE_NAME = "translations";

        public static final String ID = "id";

        public static final String NATIVE_WORD = "nativeWord";

        public static final String FOREIGN_WORD = "foreignWord";
    }

    public static class LabelledTranslation {
        public static final String TABLE_NAME = "labelled_translation";

        public static final String ID = "id";

        public static final String TRANSLATION_ID = "translation_id";

        public static final String LABEL_ID = "label_id";

    }

    public static class Label {
        public static final String TABLE_NAME = "label";

        public static final String ID = "id";

        public static final String NAME = "translation_id";
    }

    public TranslationDao(Dao dao) {
        this.dao = dao;
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
        dao.doInTransaction(runnable);
    }

    public boolean insertSingle(final Translation translation) {
        Transactable<Boolean> transactable = new Transactable<Boolean>() {
            public Boolean perform() {
                long id = insertSingleUsingDb(translation);
                boolean result = (id != ERROR_OCURRED);
                if (result) {
                    for (uk.ignas.livedictionary.core.Label label : translation.getMetadata().getLabels()) {
                        addLabelledTranslation(new Translation((int) id, translation), label);
                    }
                }
                return result;
            }
        };

        return BooleanUtils.isTrue(dao.doInTransaction(transactable));
    }

    private long insertSingleUsingDb(Translation translation) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Translations.NATIVE_WORD, translation.getNativeWord().get());
        contentValues.put(Translations.FOREIGN_WORD, translation.getForeignWord().get());
        return dao.insert(Translations.TABLE_NAME, contentValues);
    }

    public void addLabelledTranslation(Translation translation, uk.ignas.livedictionary.core.Label label) {
        dao.execSql("insert into " +
                LabelledTranslation.TABLE_NAME + " (" +
                LabelledTranslation.TRANSLATION_ID + ", " +
                LabelledTranslation.LABEL_ID + ") " + "VALUES (" +
                translation.getId() + ", " +
                label.getId() + ")");
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
                deleteLabelledTranslationsByTranslationIds(asList(translation.getId()));
                for (uk.ignas.livedictionary.core.Label l : translation.getMetadata().getLabels()) {
                    addLabelledTranslation(translation, l);
                }
                return result;
            }
        };

        return dao.doInTransaction(transactable);
    }

    private int updateSingleTranslation(Translation translation) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Translations.NATIVE_WORD, translation.getNativeWord().get());
        contentValues.put(Translations.FOREIGN_WORD, translation.getForeignWord().get());
        return dao.update(Translations.TABLE_NAME, contentValues, Translations.ID + " = ? ",
                      new String[]{String.valueOf(translation.getId())});
    }

    public void delete(final Collection<Translation> translations) {
        Transactable<Void> transactable = new Transactable<Void>() {
            @Override
            public Void perform() {
                List<Integer> ids = collectIds(translations);
                ids.removeAll(Collections.singleton(null));
                deleteAnswersByTranslationIds(ids);
                deleteLabelledTranslationsByTranslationIds(ids);
                for (Translation translation : translations) {
                    deleteSingle(translation);
                }
                return null;
            }
        };
        dao.doInTransaction(transactable);
    }

    private void deleteAnswersByTranslationIds(List<Integer> translationIdsToDelete) {
        String inClause = Joiner.on(", ").join(translationIdsToDelete);
        dao.execSql("DELETE FROM " + AnswersLog.TABLE_NAME + " WHERE " +
                AnswersLog.TRANSLATION_ID + " IN (" + inClause + ") ");
    }

    private void deleteLabelledTranslationsByTranslationIds(List<Integer> translationIds) {
        String inClause = Joiner.on(", ").join(translationIds);
        dao.execSql("DELETE FROM " + LabelledTranslation.TABLE_NAME + " WHERE " +
                LabelledTranslation.TRANSLATION_ID + " IN (" + inClause + ") ");
    }

    public void deleteLabelledTranslation(Translation translation, uk.ignas.livedictionary.core.Label label) {
        dao.execSql("DELETE FROM " + LabelledTranslation.TABLE_NAME + " WHERE " +
                LabelledTranslation.TRANSLATION_ID + " = " + translation.getId() + " AND " +
                LabelledTranslation.LABEL_ID + " = " + label.getId());
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
        return dao.delete(tableName, condition, args);
    }

    private Integer deleteSingle(Translation translation) {
        if (translation.getId() != null) {
            return deleteById(translation.getId());
        } else {
            return 0;
        }
    }

    public Collection<Integer> getTranslationIdsWithLabel(uk.ignas.livedictionary.core.Label label) {
        List<Integer> translationIds = new ArrayList<>();

        Cursor res = null;
        try {
            String sql = "select " +
                         LabelledTranslation.TRANSLATION_ID + " " +
                         " from " + LabelledTranslation.TABLE_NAME + " where " +
                         LabelledTranslation.LABEL_ID + " = " + label.getId();
            res = dao.rawQuery(sql);
            res.moveToFirst();

            while (!res.isAfterLast()) {
                translationIds.add(res.getInt(res.getColumnIndex(LabelledTranslation.TRANSLATION_ID)));
                res.moveToNext();
            }
        } finally {
            if (res != null) {
                res.close();
            }
        }
        return translationIds;
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
            res = dao.rawQuery(query);
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

    public boolean logAnswer(Translation translation, Answer answer, Date time) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(AnswersLog.TRANSLATION_ID, translation.getId());
        contentValues.put(AnswersLog.TIME_ANSWERED, time.getTime());
        contentValues.put(AnswersLog.IS_CORRECT, answer.isCorrect());
        long id = dao.insert(AnswersLog.TABLE_NAME, contentValues);
        return id != ERROR_OCURRED;
    }

    public ListMultimap<Integer, AnswerAtTime> getAnswersLogByTranslationId() {
        ListMultimap<Integer, AnswerAtTime> answersLogByTranslationId = ArrayListMultimap.create();

        Cursor res = null;
        try {
            String sql = "select " +
                         AnswersLog.IS_CORRECT + ", " +
                         AnswersLog.TRANSLATION_ID + ", " +
                         AnswersLog.TIME_ANSWERED +
                         " from " + AnswersLog.TABLE_NAME;
            res = dao.rawQuery(sql);
            res.moveToFirst();

            while (!res.isAfterLast()) {
                int newTranslationId = res.getInt(res.getColumnIndex(AnswersLog.TRANSLATION_ID));
                Answer answer =
                    res.getInt(res.getColumnIndex(AnswersLog.IS_CORRECT)) > 0 ? Answer.CORRECT : Answer.INCORRECT;
                long timeOfAnswer = res.getLong(res.getColumnIndex(AnswersLog.TIME_ANSWERED));
                answersLogByTranslationId.put(newTranslationId, new AnswerAtTime(answer, new Date(timeOfAnswer)));
                res.moveToNext();
            }
        } finally {
            if (res != null) {
                res.close();
            }
        }
        return answersLogByTranslationId;
    }
}
