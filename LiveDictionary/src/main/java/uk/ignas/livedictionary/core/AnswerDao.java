package uk.ignas.livedictionary.core;

import android.content.ContentValues;
import android.database.Cursor;
import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import uk.ignas.livedictionary.core.util.Dao;

import java.util.Date;
import java.util.List;

public class AnswerDao {
    public static final int ERROR_OCURRED = -1;

    public static class AnswersLog {

        public static final String TABLE_NAME = "answers_log";

        public static final String ID = "id";

        public static final String TRANSLATION_ID = "translation_id";

        public static final String TIME_ANSWERED = "time_answered";

        public static final String IS_CORRECT = "is_correct";

    }
    private final Dao dao;

    public AnswerDao(Dao dao) {
        this.dao = dao;
    }

    public void deleteAnswersByTranslationIds(List<Integer> translationIdsToDelete) {
        String inClause = Joiner.on(", ").join(translationIdsToDelete);
        dao.execSql("DELETE FROM " + AnswersLog.TABLE_NAME + " WHERE " +
                    AnswersLog.TRANSLATION_ID + " IN (" + inClause + ") ");
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
