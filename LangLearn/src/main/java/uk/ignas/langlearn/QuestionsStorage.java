package uk.ignas.langlearn;

import android.content.Context;
import android.os.Environment;
import uk.ignas.langlearn.core.Difficulty;
import uk.ignas.langlearn.core.Translation;
import uk.ignas.langlearn.core.db.DBHelper;
import uk.ignas.langlearn.core.parser.DbUtils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Set;

public class QuestionsStorage {

    private LinkedHashMap<Translation, Difficulty> questionsList;

    private Context context;

    public QuestionsStorage(Context context) {

        this.context = context;
    }

    public LinkedHashMap<Translation, Difficulty> getQuestions() {
        if (questionsList == null) {
            this.questionsList = loadQuestions();
        }
        return questionsList;
    }

    private LinkedHashMap<Translation, Difficulty> loadQuestions() {
        LinkedHashMap<Translation, Difficulty> questionsList;
        File externalDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File applicationDir = new File(externalDir, "LangLearn");
        File planeTextFileDir = new File(externalDir, "SpanishWords.txt");

        if (!externalDir.exists() || (!applicationDir.mkdirs() && !applicationDir.exists())) {
            throw new RuntimeException("application dir cannot be created");
        }

        try {
            new DbUtils(context).importFromFile(planeTextFileDir.getAbsolutePath());
            questionsList = new DbUtils(context).getTranslationsFromDb();

        } catch (IOException e) {
            throw new RuntimeException("asd");
        }
        return questionsList;
    }

    public void markUnknown(Set<Translation> unknownQuestions) {
        for (Translation t: unknownQuestions) {
            new DBHelper(context).update(t.getOriginalWord(), t.getTranslatedWord(), Difficulty.HARD);
        }
    }
}
