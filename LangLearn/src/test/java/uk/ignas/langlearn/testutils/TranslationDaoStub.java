package uk.ignas.langlearn.testutils;

import uk.ignas.langlearn.core.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class TranslationDaoStub implements TranslationDao {
    private LinkedHashMap<Translation, Difficulty> inMemoryTranslations = new LinkedHashMap<>();
    private int sequence = 1;
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

        inMemoryTranslations.put(new Translation(sequence++, translation), Difficulty.EASY);
        return true;
    }

    @Override
    public int update(int id, ForeignWord foreignWord, NativeWord nativeWord, Difficulty difficulty) {
        Translation translationsToUpgrade = new Translation(foreignWord, nativeWord);
        Translation oldTranslation = getTranslationById(id);
        if (oldTranslation != null) {
            inMemoryTranslations.remove(oldTranslation);
            inMemoryTranslations.put(new Translation(id, translationsToUpgrade), difficulty);
            return 1;
        } else {
            return 0;
        }
    }

    private Translation getTranslationById(int id) {
        Translation oldTranslation = null;
        for (Translation t: inMemoryTranslations.keySet()) {
            if (t.getId() == id) {
                oldTranslation = t;
            }
        }
        return oldTranslation;
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
