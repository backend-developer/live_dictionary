package uk.ignas.livedictionary.core;

import java.util.Collection;
import java.util.List;

public interface TranslationDao {
    void insert(final List<Translation> translations);
    boolean insertSingle(final Translation translation);
    int update(final Translation translation);
    void delete(final Collection<Translation> translations);
    List<Translation> getAllTranslations();
}
