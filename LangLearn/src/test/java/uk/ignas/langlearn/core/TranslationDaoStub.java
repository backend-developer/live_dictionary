package uk.ignas.langlearn.core;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class TranslationDaoStub implements TranslationDao {
    private LinkedHashMap<Translation, Difficulty> inMemoryTranslations = new LinkedHashMap<>();

    @Override
    public void insert(List<Translation> translations) {
        for (Translation t: translations) {
            insertSingle(t);
        }
    }

    @Override
    public boolean insertSingle(Translation translation) {
        if (inMemoryTranslations.containsKey(translation)) {
            return false;
        }
        inMemoryTranslations.put(translation, Difficulty.EASY);
        return true;
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
