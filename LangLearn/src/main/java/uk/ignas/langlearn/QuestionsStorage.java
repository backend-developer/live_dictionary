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
        File planeTextFile = new File(externalDir, "SpanishWords.txt");

        if (!externalDir.exists() || (!applicationDir.mkdirs() && !applicationDir.exists())) {
            throw new RuntimeException("application dir cannot be created");
        }

        try {
            new DbUtils(context).importFromFile(planeTextFile.getAbsolutePath());
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

    public void exportData() {
        File externalDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File planeTextExportedFile = new File(externalDir, "PlaneTextExportedFile.txt");
        if (externalDir.exists() || !planeTextExportedFile.exists()) {
            LinkedHashMap<Translation, Difficulty> translationsFromDb = new DbUtils(context).getTranslationsFromDb();
            try {
                writeTranslations(planeTextExportedFile.getAbsolutePath(), translationsFromDb.keySet());
            } catch (Exception e) {
                throw new RuntimeException();
            }
        } else {
            throw new RuntimeException();
        }
    }

    private void writeTranslations(String path, Set<Translation> translations) throws IOException {
        File fout = new File(path);
        FileOutputStream fos = new FileOutputStream(fout);

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

        for (Translation translation : translations) {
            bw.write(translation.getTranslatedWord() + " - " + translation.getOriginalWord());
            bw.newLine();
        }

        bw.close();
    }
}
