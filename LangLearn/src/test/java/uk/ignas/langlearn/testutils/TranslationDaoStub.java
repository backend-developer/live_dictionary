package uk.ignas.langlearn.testutils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import uk.ignas.langlearn.core.*;

import java.util.*;

public class TranslationDaoStub implements TranslationDao {
    private List<Translation> inMemoryTranslations = new ArrayList<>();
    private ListMultimap<Integer, AnswerAtTime> answersByTranslationId = ArrayListMultimap.create();
    private int sequence = 1;
    @Override
    public void insert(List<Translation> translations) {
        for (Translation t: translations) {
            insertSingle(t);
        }
    }

    @Override
    public boolean insertSingle(Translation translation) {
        if (inMemoryTranslations.contains(translation)) {
            return false;
        }

        inMemoryTranslations.add(new Translation(
                sequence++,
                translation.getForeignWord(),
                translation.getNativeWord()));
        return true;
    }

    @Override
    public int update(int id, ForeignWord foreignWord, NativeWord nativeWord) {
        Translation oldTranslation = getTranslationById(id);
        if (oldTranslation != null) {
            Translation translationsToUpgrade = new Translation(foreignWord, nativeWord);
            inMemoryTranslations.remove(oldTranslation);
            inMemoryTranslations.add(new Translation(id, translationsToUpgrade));
            return 1;
        } else {
            return 0;
        }
    }

    private Translation getTranslationById(int id) {
        Translation oldTranslation = null;
        for (Translation t: inMemoryTranslations) {
            if (t.getId() == id) {
                oldTranslation = t;
            }
        }
        return oldTranslation;
    }

    @Override
    public void delete(Collection<Translation> translations) {
        for (Translation t: translations) {
            answersByTranslationId.removeAll(t.getId());
            inMemoryTranslations.remove(t);
        }
    }

    @Override
    public List<Translation> getAllTranslations() {
        List<Translation> copy = new ArrayList<>();
        for (Translation t : inMemoryTranslations) {
            copy.add(new Translation(
                    t.getId(),
                    t.getForeignWord(),
                    t.getNativeWord(),
                    TranslationMetadata.copy(t.getMetadata())));
        }
        return copy;
    }

    @Override
    public ListMultimap<Integer, AnswerAtTime> getAnswersLogByTranslationId() {
        return answersByTranslationId;
    }

    @Override
    public boolean logAnswer(Translation translation, Answer answer, Date time) {
        List<AnswerAtTime> answerAtTimes = answersByTranslationId.get(translation.getId());
        boolean doesTranslationExist = getTranslationById(translation.getId()) != null;
        if (doesTranslationExist) {
            answerAtTimes.add(new AnswerAtTime(answer, time));
        }
        return doesTranslationExist;
    }
}
