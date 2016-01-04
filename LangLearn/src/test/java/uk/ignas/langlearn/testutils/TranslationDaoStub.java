package uk.ignas.langlearn.testutils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import uk.ignas.langlearn.core.*;

import java.util.*;

public class TranslationDaoStub implements TranslationDao {
    private List<Translation> inMemoryTranslations = new ArrayList<>();
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

        TranslationMetadata metadata = translation.getMetadata();
        if (metadata == null) {
            metadata = new TranslationMetadata(new ArrayList<AnswerAtTime>());
        }
        inMemoryTranslations.add(new Translation(sequence++, translation.getForeignWord(), translation.getNativeWord(), metadata));
        return true;
    }

    @Override
    public int update(int id, ForeignWord foreignWord, NativeWord nativeWord) {
        Translation oldTranslation = getTranslationById(id);
        if (oldTranslation != null) {
            Translation translationsToUpgrade = new Translation(foreignWord, nativeWord, oldTranslation.getMetadata());
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
        ListMultimap<Integer, AnswerAtTime> result = ArrayListMultimap.create();
        for (Translation translation : inMemoryTranslations) {
            result.putAll(translation.getId(), translation.getMetadata().getRecentAnswers());
        }
        return result;
    }

    @Override
    public boolean logAnswer(Translation translation, Answer answer, Date time) {

        for (Translation t: inMemoryTranslations) {
            if (Objects.equals(t.getId(), translation.getId())) {
                return t.getMetadata().getRecentAnswers().add(new AnswerAtTime(answer, time));
            }
        }
        return false;
    }
}
