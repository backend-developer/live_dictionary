package uk.ignas.langlearn.core.parser;

import android.content.Context;
import android.widget.Toast;
import com.google.common.collect.Sets;
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

    public void importFromFile(String planeTextFilePath) throws IOException {
        List<String> planeText = readFile(planeTextFilePath);

        DBHelper dbHelper = new DBHelper(context);
        LinkedHashSet<Translation> translationsToInsert = new LinkedHashSet<>();

        for (String planeTextLine : planeText) {
            Translation parsed = translationParser.parse(planeTextLine);
            if (parsed != null) {
                translationsToInsert.add(parsed);
            }
        }

        Set<Translation> translationsFromDb = getTranslationsFromDb().keySet();
        Sets.SetView<Translation> toAdd = Sets.difference(translationsToInsert, translationsFromDb);
        Sets.SetView<Translation> toRemove = Sets.difference(translationsFromDb, translationsToInsert);

        dbHelper.insert(toAdd);
        dbHelper.delete(toRemove);

        showToast("inserted: " + toAdd.size() + "; removed: " + toRemove.size());
    }

    private void showToast(String text) {
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
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
