package uk.ignas.langlearn.core;

import uk.ignas.langlearn.core.Difficulty;
import uk.ignas.langlearn.core.Translation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public interface TranslationDao {
    void insert(List<Translation> translations);

    int update(String originalWord, String translatedWord, Difficulty difficulty);

    void delete(Set<Translation> translations);

    LinkedHashMap<Translation, Difficulty> getAllTranslations();
}
