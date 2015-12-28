package uk.ignas.langlearn.core;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface TranslationDao {
    void insert(List<Translation> translations);

    boolean insertSingle(Translation translation);

    int update(int id, ForeignWord foreignWord, NativeWord nativeWord, TranslationMetadata metadata);

    void delete(Collection<Translation> translations);

    List<Translation> getAllTranslations();

    Translation getById(int id);
}
