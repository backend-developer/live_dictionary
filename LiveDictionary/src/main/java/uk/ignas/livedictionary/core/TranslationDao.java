package uk.ignas.livedictionary.core;

import com.google.common.collect.ListMultimap;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface TranslationDao {
    void insert(List<Translation> translations);

    boolean insertSingle(Translation translation);

    int update(int id, ForeignWord foreignWord, NativeWord nativeWord);

    void delete(Collection<Translation> translations);

    List<Translation> getAllTranslations();

    ListMultimap<Integer, AnswerAtTime> getAnswersLogByTranslationId();

    boolean logAnswer(Translation translation, Answer answer, Date time);
}
