package uk.ignas.langlearn.core.db;

import uk.ignas.langlearn.core.Difficulty;
import uk.ignas.langlearn.core.Translation;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class TranslationDaoStub implements TranslationDao {
    private LinkedHashMap<Translation, Difficulty> inMemoryTranslations = new LinkedHashMap<>();

    @Override
    public void insert(List<Translation> translations) {
        for (Translation t: translations) {
            inMemoryTranslations.put(t, Difficulty.EASY);
        }
    }

    @Override
    public int update(String originalWord, String translatedWord, Difficulty difficulty) {
        Translation wordToUpgrade = new Translation(originalWord, translatedWord);
        if (inMemoryTranslations.containsKey(wordToUpgrade)) {
            inMemoryTranslations.put(wordToUpgrade, difficulty);
            return 1;
        } else {
            return 0;
        }

    }

    @Override
    public void delete(Set<Translation> translations) {
        for (Translation t: translations) {
            inMemoryTranslations.remove(t);
        }
    }

    @Override
    public LinkedHashMap<Translation, Difficulty> getAllTranslations() {
        return inMemoryTranslations;
    }
}
