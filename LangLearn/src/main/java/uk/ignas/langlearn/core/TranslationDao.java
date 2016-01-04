package uk.ignas.langlearn.core;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface TranslationDao {
    void insert(List<Translation> translations);

    boolean insertSingle(Translation translation);

    int update(int id, ForeignWord foreignWord, NativeWord nativeWord);

    void delete(Collection<Translation> translations);

    List<Translation> getAllTranslations();

    Translation getById(int id);

    boolean logAnswer(Translation translation, Difficulty difficulty, Date time);
}
