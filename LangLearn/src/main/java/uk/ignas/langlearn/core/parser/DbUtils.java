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

        List<Translation> reversedToAdd = new ArrayList<>(toAdd);
        Collections.reverse(reversedToAdd);

        dbHelper.insert(reversedToAdd);
        dbHelper.delete(toRemove);

        showToast("inserted: " + toAdd.size() + "; removed: " + toRemove.size());
    }

    public void export(String planeTextExportedPath) {
        LinkedHashMap<Translation, Difficulty> translationsFromDb = getTranslationsFromDb();
        try {
            writeTranslations(planeTextExportedPath, translationsFromDb.keySet());
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    public void validateImportAndExportWorksConsistently(String dataToImportFileName, String exportedDataFileName) {
        if (!new File(dataToImportFileName).exists()) {
            throw new RuntimeException("validation failed. invalid import file specified");
        }
        if (!new File(exportedDataFileName).exists()) {
            throw new RuntimeException("validation failed. invalid export file specified");
        }
        List<String> dataToImport = readFile(dataToImportFileName);
        List<String> exportedData = readFile(exportedDataFileName);

        for(Iterator<String> it = dataToImport.iterator(); it.hasNext(); ) {
            if (it.next().isEmpty()) {
                it.remove();
            }
        }

        Set<String> dataToImportSet = new HashSet<>(dataToImport);
        Set<String> exportedDataSet = new HashSet<>(exportedData);
        if (!dataToImportSet.equals(exportedDataSet)) {
            throw new RuntimeException("Import or export does not work properly");
        }
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
