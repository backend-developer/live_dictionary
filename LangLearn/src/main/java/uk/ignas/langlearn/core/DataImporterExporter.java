package uk.ignas.langlearn.core;

import android.content.Context;
import com.google.common.collect.Sets;
import uk.ignas.langlearn.core.db.TranslationDao;
import uk.ignas.langlearn.core.parser.DbUtils;
import uk.ignas.langlearn.core.parser.TranslationParser;

import java.io.*;
import java.util.*;

public class DataImporterExporter {
    private File defaultAppDirFile;
    private TranslationParser translationParser = new TranslationParser();
    private Context context;
    private TranslationDao dao;
    private DbUtils dbUtils;

    public DataImporterExporter(Context context, TranslationDao dao, File defaultAppDirFile) {
        this.context = context;
        this.dao = dao;
        this.defaultAppDirFile = defaultAppDirFile;
        dbUtils = new DbUtils(dao);
    }

    public void importAndValidateTranslations() {
        File externalDir = defaultAppDirFile;
        String dataToImportFileName = "SpanishWords.txt";
        File dataToImportFile = new File(externalDir, dataToImportFileName);

        if (!externalDir.exists()) {
            throw new RuntimeException("application dir cannot be created");
        }

        try {
            importFromFile(dataToImportFile.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void reexport(String pathToFile) {
        File planeTextExportedFile = new File(pathToFile);
        if (!planeTextExportedFile.getParentFile().exists()) {
            throw new RuntimeException("application dir cannot be created");
        }

        export(planeTextExportedFile.getAbsolutePath());
    }

    public void importFromFile(String planeTextFilePath) throws IOException {
        List<String> planeText = readFile(planeTextFilePath);

        LinkedHashSet<Translation> translationsToInsert = new LinkedHashSet<>();

        for (String planeTextLine : planeText) {
            Translation parsed = translationParser.parse(planeTextLine);
            if (parsed != null) {
                translationsToInsert.add(parsed);
            }
        }

        Set<Translation> translationsFromDb = dbUtils.getTranslationsFromDb().keySet();
        Sets.SetView<Translation> toAdd = Sets.difference(translationsToInsert, translationsFromDb);
        Sets.SetView<Translation> toRemove = Sets.difference(translationsFromDb, translationsToInsert);

        List<Translation> reversedToAdd = new ArrayList<>(toAdd);
        Collections.reverse(reversedToAdd);

        dao.insert(reversedToAdd);
        dao.delete(toRemove);
    }

    public void export(String planeTextExportedPath) {

        LinkedHashMap<Translation, Difficulty> translationsFromDb = dbUtils.getTranslationsFromDb();
        try {
            writeTranslations(planeTextExportedPath, translationsFromDb.keySet());
        } catch (Exception e) {
            throw new RuntimeException();
        }
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
