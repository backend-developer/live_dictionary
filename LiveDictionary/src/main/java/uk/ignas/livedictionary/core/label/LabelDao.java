package uk.ignas.livedictionary.core.label;

import android.database.Cursor;
import com.google.common.base.Joiner;
import uk.ignas.livedictionary.core.util.DatabaseFacade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface LabelDao {
    void addLabelledTranslation(Integer translationId, uk.ignas.livedictionary.core.label.Label label);
    void deleteLabelledTranslationsByTranslationIds(List<Integer> translationIds);
    void deleteLabelledTranslation(Integer translationId, uk.ignas.livedictionary.core.label.Label label);
    Collection<Integer> getTranslationIdsWithLabel(uk.ignas.livedictionary.core.label.Label label);
}
