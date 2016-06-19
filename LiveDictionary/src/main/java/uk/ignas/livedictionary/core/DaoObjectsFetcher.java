package uk.ignas.livedictionary.core;

import com.google.common.base.Function;
import com.google.common.collect.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.Validate.notNull;

public class DaoObjectsFetcher {

    private TranslationDao dao;

    public DaoObjectsFetcher(TranslationDao dao) {
        this.dao = dao;
    }

    public void fetchLabels(Collection<Translation> translations) {
        checkForNonNullIds(translations);

        Multimap<Label, Integer> labelsToTranslationIds = getLabelsToTranslationIds();
        ImmutableListMultimap<Integer, Translation> idsToTranslations = indexById(translations);
        checkForDuplicateIds(translations, idsToTranslations);
        addLabelsToTranslations(labelsToTranslationIds, idsToTranslations);
    }

    private void addLabelsToTranslations(Multimap<Label, Integer> labelsToTranslationIds,
                                         ImmutableListMultimap<Integer, Translation> idsToTranslations) {
        for (Map.Entry<Label, Integer> labelToTranslationId : labelsToTranslationIds.entries()) {
            Label label = labelToTranslationId.getKey();
            Integer translationId = labelToTranslationId.getValue();
            ImmutableList<Translation> translationsWithId = idsToTranslations.get(translationId);
            translationsWithId.get(0).getMetadata().getLabels().add(label);
        }
    }

    private void checkForDuplicateIds(Collection<Translation> translations,
                                      ImmutableListMultimap<Integer, Translation> idsToTranslations) {
        if (idsToTranslations.keySet().size() != translations.size()) {
            throw new IllegalArgumentException("more than 1 translation has the same id");
        }
    }

    private void checkForNonNullIds(Collection<Translation> translations) {
        for (Translation t : translations) {
            notNull(t.getId());
        }
    }

    private Multimap<Label, Integer> getLabelsToTranslationIds() {
        Multimap<Label, Integer> labelsToTranslationIds = ArrayListMultimap.<Label, Integer>create();
        for (Label l: Label.values()) {
            labelsToTranslationIds.putAll(l, dao.getTranslationIdsWithLabel(l));
        }
        return labelsToTranslationIds;
    }

    private ImmutableListMultimap<Integer, Translation> indexById(Collection<Translation> translations) {
        return Multimaps.index(translations, new Function<Translation, Integer>() {
            @Override
            public Integer apply(Translation translation) {
                return translation.getId();
            }
        });
    }

    public void fetchAnswersLog(List<Translation> allTranslations) {
        checkForNonNullIds(allTranslations);

        ListMultimap<Integer, AnswerAtTime> answersLogByTranslationId = dao.getAnswersLogByTranslationId();
        for (Translation translation : allTranslations) {
            List<AnswerAtTime> answersLog = answersLogByTranslationId.get(translation.getId());
            translation.getMetadata().getRecentAnswers().addAll(answersLog);
        }
    }
}
