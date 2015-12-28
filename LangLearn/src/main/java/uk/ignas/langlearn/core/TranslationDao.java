package uk.ignas.langlearn.core;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface TranslationDao {
    void insert(List<Translation> translations);

    boolean insertSingle(Translation translation);

    int update(int id, ForeignWord foreignWord, NativeWord nativeWord, Difficulty difficulty);

    void delete(Collection<Translation> translations);

    List<Translation> getAllTranslations();
}
