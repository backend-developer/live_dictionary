package uk.ignas.livedictionary.core.answer;

import android.content.ContentValues;
import android.database.Cursor;
import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import uk.ignas.livedictionary.core.util.DatabaseFacade;

import java.util.Date;
import java.util.List;

public class SqliteAnswerDao implements AnswerDao {
    public static final int ERROR_OCURRED = -1;

    public static class AnswersLog {

        public static final String TABLE_NAME = "answers_log";

        public static final String ID = "id";

        public static final String TRANSLATION_ID = "translation_id";

        public static final String TIME_ANSWERED = "time_answered";

        public static final String IS_CORRECT = "is_correct";

        public static final String FEEDBACK = "feedback";
    }

    private final DatabaseFacade databaseFacade;

    public SqliteAnswerDao(DatabaseFacade databaseFacade) {
        this.databaseFacade = databaseFacade;
    }

    public void deleteAnswersByTranslationIds(List<Integer> translationIdsToDelete) {
        String inClause = Joiner.on(", ").join(translationIdsToDelete);
        databaseFacade.execSql("DELETE FROM " + AnswersLog.TABLE_NAME + " WHERE " +
                               AnswersLog.TRANSLATION_ID + " IN (" + inClause + ") ");
    }

    @Override
    public boolean logAnswer(Integer translationId, AnswerAtTime answerAtTime) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(AnswersLog.TRANSLATION_ID, translationId);
        if (answerAtTime.getFeedback() != null) {
            contentValues.put(AnswersLog.FEEDBACK, answerAtTime.getFeedback().name());
        }
        contentValues.put(AnswersLog.TIME_ANSWERED, answerAtTime.getTimepoint().getTime());
        contentValues.put(AnswersLog.IS_CORRECT, answerAtTime.getAnswer().isCorrect());
        long id = databaseFacade.insert(AnswersLog.TABLE_NAME, contentValues);
        return id != ERROR_OCURRED;
    }

    public ListMultimap<Integer, AnswerAtTime> getAnswersLogByTranslationId() {
        ListMultimap<Integer, AnswerAtTime> answersLogByTranslationId = ArrayListMultimap.create();

        Cursor res = null;
        try {
            String sql = "select " +
                         AnswersLog.IS_CORRECT + ", " +
                         AnswersLog.TRANSLATION_ID + ", " +
                         AnswersLog.TIME_ANSWERED + ", " +
                         AnswersLog.FEEDBACK +
                         " from " + AnswersLog.TABLE_NAME;
            res = databaseFacade.rawQuery(sql);
            res.moveToFirst();

            while (!res.isAfterLast()) {
                int newTranslationId = res.getInt(res.getColumnIndex(AnswersLog.TRANSLATION_ID));
                Answer answer =
                    res.getInt(res.getColumnIndex(AnswersLog.IS_CORRECT)) > 0 ? Answer.CORRECT : Answer.INCORRECT;
                long timeOfAnswer = res.getLong(res.getColumnIndex(AnswersLog.TIME_ANSWERED));
                String feedbackString = res.getString(res.getColumnIndex(AnswersLog.FEEDBACK));
                Feedback feedback = feedbackString != null ? Feedback.valueOf(feedbackString) : null;
                answersLogByTranslationId
                    .put(newTranslationId, new AnswerAtTime(answer, new Date(timeOfAnswer), feedback));
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
