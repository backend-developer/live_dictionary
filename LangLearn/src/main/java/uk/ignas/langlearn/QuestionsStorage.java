package uk.ignas.langlearn;

import android.content.Context;
import android.os.Environment;
import uk.ignas.langlearn.core.Difficulty;
import uk.ignas.langlearn.core.Translation;
import uk.ignas.langlearn.core.db.DBHelper;
import uk.ignas.langlearn.core.parser.DbUtils;

import java.io.*;
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
        String dataToImportFileName = "SpanishWords.txt";
        File dataToImportFile = new File(externalDir, dataToImportFileName);
        String exportedDataFileName = "PlaneTextExportedFile.txt";
        File exportedDataFile = new File(externalDir, exportedDataFileName);

        if (!externalDir.exists() || (!applicationDir.mkdirs() && !applicationDir.exists())) {
            throw new RuntimeException("application dir cannot be created");
        }

        try {
            new DbUtils(context).importFromFile(dataToImportFile.getAbsolutePath());
            reexport(exportedDataFileName);
            new DbUtils(context).validateImportAndExportWorksConsistently(dataToImportFile.getAbsolutePath(), exportedDataFile.getAbsolutePath());
            questionsList = new DbUtils(context).getTranslationsFromDb();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return questionsList;
    }

    public void reexport(String exportedDataFileName) {
        File externalDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        if (!externalDir.exists()) {
            throw new RuntimeException("application dir cannot be created");
        }
        File planeTextExportedFile = new File(externalDir, exportedDataFileName);

        if (planeTextExportedFile.exists()) {
            if(!planeTextExportedFile.delete()) {
                throw new RuntimeException("data cannot be exported. File cannot be deleted");
            }
        }
        new DbUtils(context).export(planeTextExportedFile.getAbsolutePath());
    }

    public void markUnknown(Set<Translation> unknownQuestions) {
        for (Translation t: unknownQuestions) {
            new DBHelper(context).update(t.getOriginalWord(), t.getTranslatedWord(), Difficulty.HARD);
        }
    }
}
