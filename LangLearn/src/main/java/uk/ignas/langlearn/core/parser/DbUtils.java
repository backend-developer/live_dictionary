package uk.ignas.langlearn.core.parser;

import android.content.Context;
import uk.ignas.langlearn.core.Difficulty;
import uk.ignas.langlearn.core.Translation;
import uk.ignas.langlearn.core.db.DBHelper;

import java.io.*;
import java.util.*;

public class DbUtils {

    private TranslationParser translationParser = new TranslationParser();
    private final Context context;

    public DbUtils(Context context) {
        this.context = context;
    }

    public LinkedHashMap<Translation, Difficulty> getTranslationsFromDb() throws IOException {
        return new DBHelper(context).getAllTranslations();
    }

    public void buildDbFromPlainTextFile(String planeTextFilePath) throws IOException {
        List<String> planeText = readFile(planeTextFilePath);

        DBHelper dbHelper = new DBHelper(context);
        dbHelper.deleteAll();

        Set<Translation> uniqueValidTranslations = new HashSet<>();

        for (String planeTextLine : planeText) {
            Translation parsed = translationParser.parse(planeTextLine);
            if (parsed != null) {
                uniqueValidTranslations.add(parsed);
            }
        }
        new DBHelper(context).insert(uniqueValidTranslations);

    }

    private List<String> readFile(String path) {
        BufferedReader br = null;
        List<String> lines = new ArrayList<>();
        try {
            String sCurrentLine;
            br = new BufferedReader(new FileReader(path));
            while ((sCurrentLine = br.readLine()) != null) {
                lines.add(sCurrentLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return lines;
    }
}
