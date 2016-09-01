package uk.ignas.livedictionary.core;

import java.util.Collection;
import java.util.List;

public interface TranslationDao {
    void insert(final List<Translation> translations);
    boolean insertSingleWithLabels(final Translation translation);
    int updateAlongWithLabels(final Translation translation);
    void delete(final Collection<Translation> translations);
    List<Translation> getAllTranslations();
}
