package uk.ignas.langlearn;

import android.content.Context;
import android.os.Environment;
import uk.ignas.langlearn.core.Difficulty;
import uk.ignas.langlearn.core.Translation;
import uk.ignas.langlearn.core.parser.DbUtils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

public class QuestionsStorage {

    private LinkedHashMap<Translation, Difficulty> questionsList;


    public LinkedHashMap<Translation, Difficulty> getQuestions(Context context) {
        if (questionsList == null) {
            this.questionsList = loadQuestions(context);
        }
        return questionsList;
    }

    private LinkedHashMap<Translation, Difficulty> loadQuestions(Context context) {
        LinkedHashMap<Translation, Difficulty> questionsList;
        File externalDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File applicationDir = new File(externalDir, "LangLearn");
        File planeTextFileDir = new File(externalDir, "SpanishWords.txt");
        File translations = new File(applicationDir, "translations");

        if (!externalDir.exists() || (!applicationDir.mkdirs() && !applicationDir.exists())) {
            throw new RuntimeException("application dir cannot be created");
        }

        try {
            new DbUtils(context).buildDbFromPlainTextFile(planeTextFileDir.getAbsolutePath(), translations.getAbsolutePath());
            questionsList = new DbUtils(context).getTranslationsFromDb(translations);

        } catch (IOException e) {
            throw new RuntimeException("asd");
        }
        return questionsList;
    }
}
