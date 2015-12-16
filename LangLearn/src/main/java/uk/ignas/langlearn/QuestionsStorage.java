package uk.ignas.langlearn;

import android.os.Environment;
import junit.framework.Assert;
import uk.ignas.langlearn.core.Difficulty;
import uk.ignas.langlearn.core.Translation;
import uk.ignas.langlearn.core.parser.CsvUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class QuestionsStorage {

    private Map<Translation, Difficulty> questionsList;


    public Map<Translation, Difficulty> getQuestions()  {
        if (questionsList == null) {
            this.questionsList = loadQuestions();
        }
        return questionsList;
    }

    private Map<Translation, Difficulty> loadQuestions() {
        Map<Translation, Difficulty> questionsList;
        File externalDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File applicationDir = new File(externalDir, "LangLearn");
        File planeTextFileDir = new File(externalDir, "SpanishWords.txt");
        File translations = new File(applicationDir, "translations");

        boolean mkdirs = applicationDir.mkdirs();
        boolean isDirExists = externalDir.exists();

        try {

            new CsvUtils().buildScvFromPlainTextFile(planeTextFileDir.getAbsolutePath(), translations.getAbsolutePath());
            questionsList = new CsvUtils().getTranslationsFromCsv(translations);

        } catch (IOException e) {
            throw new RuntimeException("asd");
        }
        return questionsList;
    }
}
