package uk.ignas.langlearn.core.parser;

import uk.ignas.langlearn.core.Difficulty;
import uk.ignas.langlearn.core.Translation;
import uk.ignas.langlearn.core.db.TranslationDao;
import uk.ignas.langlearn.core.db.TranslationDaoSqlite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

public class DbUtils {

    private TranslationDao dao;

    public DbUtils(TranslationDao dao) {
        this.dao = dao;
    }

    public LinkedHashMap<Translation, Difficulty> getTranslationsFromDb() {
        LinkedHashMap<Translation, Difficulty> allTranslations = dao.getAllTranslations();
        return reverseInsertionOrder(allTranslations);
    }

    private LinkedHashMap<Translation, Difficulty> reverseInsertionOrder(LinkedHashMap<Translation, Difficulty> allTranslations) {
        List<Translation> translations = new ArrayList<>(allTranslations.keySet());
        Collections.reverse(translations);
        LinkedHashMap<Translation, Difficulty> allTranslationsReversed = new LinkedHashMap<>();
        for (Translation translation : translations) {
            allTranslationsReversed.put(translation, allTranslations.get(translation));
        }
        return allTranslationsReversed;
    }
}
