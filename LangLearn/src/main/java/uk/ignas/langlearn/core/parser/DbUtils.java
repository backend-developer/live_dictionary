package uk.ignas.langlearn.core.parser;

import android.content.Context;
import uk.ignas.langlearn.core.Difficulty;
import uk.ignas.langlearn.core.Translation;
import uk.ignas.langlearn.core.db.DBHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

public class DbUtils {

    private final Context context;

    public DbUtils(Context context) {
        this.context = context;
    }

    public LinkedHashMap<Translation, Difficulty> getTranslationsFromDb() {
        LinkedHashMap<Translation, Difficulty> allTranslations = new DBHelper(context).getAllTranslations();
        List<Translation> translations = new ArrayList<>(allTranslations.keySet());
        Collections.reverse(translations);
        LinkedHashMap<Translation, Difficulty> allTranslationsReversed = new LinkedHashMap<>();
        for (Translation translation : translations) {
            allTranslationsReversed.put(translation, allTranslations.get(translation));
        }
        return allTranslationsReversed;
    }


}
