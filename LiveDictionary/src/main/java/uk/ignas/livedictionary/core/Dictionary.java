package uk.ignas.livedictionary.core;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import uk.ignas.livedictionary.core.answer.Answer;
import uk.ignas.livedictionary.core.answer.AnswerDao;
import uk.ignas.livedictionary.core.label.Label;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static uk.ignas.livedictionary.core.util.ExceptionAnalyser.isUniqueConstraintViolation;

public class Dictionary {
    public static final int DIFFICULT_TRANSLATIONS_LIMIT = 20;

    public static final int NEWEST_100_QUESTIONS = 100;

    public static final int PROBABILITY_OF_80_PERCENT = 80;

    private TranslationDao translationDao;

    private final AnswerDao answerDao;

    private DaoObjectsFetcher fetcher;

    private Labeler labeler;

    private Clock clock;

    private final TranslationSelectionStrategy selectionStrategy;

    public Dictionary(TranslationDao translationDao, AnswerDao answerDao, DaoObjectsFetcher fetcher, Labeler labeler,
                      Clock clock, TranslationSelectionStrategy selectionStrategy) {
        this.translationDao = translationDao;
        this.answerDao = answerDao;
        this.fetcher = fetcher;
        this.labeler = labeler;
        this.clock = clock;
        this.selectionStrategy = selectionStrategy;

        reloadTranslations();
    }

    private void reloadTranslations() {
        List<Translation> translations = translationDao.getAllTranslations();
        fetcher.fetchAnswersLog(translations);
        Collection<Translation> labelledA = labeler.getLabelled(Label.A);
        Collection<Translation> labelledB = labeler.getLabelled(Label.B);

        List<Translation> nonLabelledTranslations = new ArrayList<>(translations);
        nonLabelledTranslations.removeAll(labelledA);
        nonLabelledTranslations.removeAll(labelledB);
        selectionStrategy.updateState(nonLabelledTranslations);
    }

    public Translation getRandomTranslation() {
        Optional<Translation> translation = selectionStrategy.selectTranslation();
        if (translation.isPresent()) {
            return translation.get();
        } else {
            throw new LiveDictionaryException("no questions found");
        }
    }

    public void mark(Translation translation, Answer answer) {
        boolean logged = answerDao.logAnswer(translation.getId(), answer, clock.getTime());
        if (!logged) {
            throw new IllegalArgumentException("answered not logged. translationId = " + translation.getId());
        }
        reloadData();
    }

    public void insert(Translation translation) {
        translationDao.insertSingleWithLabels(translation);
        reloadTranslations();
    }

    public void delete(Translation translation) {
        translationDao.delete(singleton(translation));
        reloadTranslations();
    }

    public boolean update(Translation translation) {
        boolean updated = updateSingle(translation);
        reloadTranslations();
        return updated;
    }

    private boolean updateSingle(Translation translation) {
        List<Translation> translations = translationDao.getAllTranslations();

        boolean updated = getIds(translations).contains(translation.getId());
        try {
            translationDao.updateAlongWithLabels(translation);
        } catch (Exception e) {
            if (isUniqueConstraintViolation(e)) {
                translationDao.delete(asList(translation));
            } else {
                throw e;
            }
        }
        return updated;
    }

    private List<Integer> getIds(List<Translation> translations) {
        return newArrayList(Iterables.transform(translations, new Function<Translation, Integer>() {

            @Override
            public Integer apply(Translation translation) {
                return translation.getId();
            }
        }));
    }

    public void reloadData() {
        reloadTranslations();
    }
}
