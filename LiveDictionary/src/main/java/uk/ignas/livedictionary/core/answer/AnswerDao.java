package uk.ignas.livedictionary.core.answer;

import android.content.ContentValues;
import android.database.Cursor;
import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import uk.ignas.livedictionary.core.util.DatabaseFacade;

import java.util.Date;
import java.util.List;

public interface AnswerDao {
    void deleteAnswersByTranslationIds(List<Integer> translationIdsToDelete);
    boolean logAnswer(Integer translationId, Answer answer, Date time);
    ListMultimap<Integer, AnswerAtTime> getAnswersLogByTranslationId();
}
