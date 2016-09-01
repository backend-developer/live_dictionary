package uk.ignas.livedictionary.core.label;

import android.database.Cursor;
import com.google.common.base.Joiner;
import uk.ignas.livedictionary.core.util.DatabaseFacade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SqliteLabelDao implements LabelDao{
    private final DatabaseFacade databaseFacade;

    private static class LabelledTranslation {
        public static final String TABLE_NAME = "labelled_translation";

        public static final String ID = "id";

        public static final String TRANSLATION_ID = "translation_id";

        public static final String LABEL_ID = "label_id";
    }

    private static class Label {
        public static final String TABLE_NAME = "label";

        public static final String ID = "id";

        public static final String NAME = "translation_id";
    }

    public SqliteLabelDao(DatabaseFacade databaseFacade) {
        this.databaseFacade = databaseFacade;
    }

    public void addLabelledTranslation(Integer translationId, uk.ignas.livedictionary.core.label.Label label) {
        databaseFacade.execSql("insert into " +
                               LabelledTranslation.TABLE_NAME + " (" +
                               LabelledTranslation.TRANSLATION_ID + ", " +
                               LabelledTranslation.LABEL_ID + ") " + "VALUES (" +
                               translationId + ", " +
                               label.getId() + ")");
    }

    public void deleteLabelledTranslationsByTranslationIds(List<Integer> translationIds) {
        String inClause = Joiner.on(", ").join(translationIds);
        databaseFacade.execSql("DELETE FROM " + LabelledTranslation.TABLE_NAME + " WHERE " +
                               LabelledTranslation.TRANSLATION_ID + " IN (" + inClause + ") ");
    }

    public void deleteLabelledTranslation(Integer translationId, uk.ignas.livedictionary.core.label.Label label) {
        databaseFacade.execSql("DELETE FROM " + LabelledTranslation.TABLE_NAME + " WHERE " +
                               LabelledTranslation.TRANSLATION_ID + " = " + translationId + " AND " +
                               LabelledTranslation.LABEL_ID + " = " + label.getId());
    }

    public Collection<Integer> getTranslationIdsWithLabel(uk.ignas.livedictionary.core.label.Label label) {
        List<Integer> translationIds = new ArrayList<>();

        Cursor res = null;
        try {
            String sql = "select " +
                         LabelledTranslation.TRANSLATION_ID + " " +
                         " from " + LabelledTranslation.TABLE_NAME + " where " +
                         LabelledTranslation.LABEL_ID + " = " + label.getId();
            res = databaseFacade.rawQuery(sql);
            res.moveToFirst();

            while (!res.isAfterLast()) {
                translationIds.add(res.getInt(res.getColumnIndex(LabelledTranslation.TRANSLATION_ID)));
                res.moveToNext();
            }
        } finally {
            if (res != null) {
                res.close();
            }
        }
        return translationIds;
    }

}
