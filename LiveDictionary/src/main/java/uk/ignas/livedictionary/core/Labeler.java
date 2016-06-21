package uk.ignas.livedictionary.core;

import android.database.sqlite.SQLiteException;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static uk.ignas.livedictionary.core.ExceptionAnalyser.isUniqueConstraintViolation;

public class Labeler {
    private final TranslationDao dao;
    private final DaoObjectsFetcher fetcher;

    public Labeler(TranslationDao dao, DaoObjectsFetcher fetcher) {

        this.dao = dao;
        this.fetcher = fetcher;
    }

    public void addLabel(Translation translation, Label label) {
        try {
            dao.addLabelledTranslation(translation, label);
        } catch (Exception e) {
            rejectDuplicateLabelSilently(e);
        }
    }

    private void rejectDuplicateLabelSilently(Exception e) {
        if (!isUniqueConstraintViolation(e)) {
            throw new SQLiteException("Cannot set label", e);
        }
    }

    public Collection<Translation> getLabelled(Label label) {
        List<Translation> translations = dao.getAllTranslations();
        fetcher.fetchLabels(translations);
        for (Translation t : new ArrayList<>(translations)) {
            if (!t.getMetadata().getLabels().contains(label)) {
                translations.remove(t);
            }
        }
        return translations;
    }

    public void removeLabel(Translation translation, Label label) {
        dao.deleteLabelledTranslation(translation, label);
    }
}
