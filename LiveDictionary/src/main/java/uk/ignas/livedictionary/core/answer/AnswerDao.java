package uk.ignas.livedictionary.core.answer;

import com.google.common.collect.ListMultimap;

import java.util.List;

public interface AnswerDao {
    void deleteAnswersByTranslationIds(List<Integer> translationIdsToDelete);
    boolean logAnswer(Integer translationId, AnswerAtTime answerAtTime);

    ListMultimap<Integer, AnswerAtTime> getAnswersLogByTranslationId();
}
