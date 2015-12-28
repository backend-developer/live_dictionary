package uk.ignas.langlearn.core;

import java.io.*;
import java.util.*;

public class DataImporterExporter {
    private TranslationParser translationParser = new TranslationParser();
    private TranslationDao dao;

    public DataImporterExporter(TranslationDao dao) {
        this.dao = dao;
    }

    public void importFromFile(String planeTextFilePath) {
        File planeTextExportedFile = new File(planeTextFilePath);
        if (!planeTextExportedFile.exists()) {
            throw new RuntimeException("file to import doesn't exist");
        }

        try {
            importOrThrow(planeTextFilePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void importOrThrow(String planeTextFilePath) throws IOException {
        List<String> planeText = readFile(planeTextFilePath);

        LinkedHashSet<Translation> translationsToInsert = new LinkedHashSet<>();

        for (String planeTextLine : planeText) {
            Translation parsed = translationParser.parse(planeTextLine);
            if (parsed != null) {
                translationsToInsert.add(parsed);
            }
        }

        List<Translation> translationsFromDb = dao.getAllTranslations();
        dao.delete(translationsFromDb);

        dao.insert(new ArrayList<>(translationsToInsert));
    }

    public void export(String planeTextExportedPath) {
        File planeTextExportedFile = new File(planeTextExportedPath);
        File planeTextExportedFileFromAbsolutePath = new File(planeTextExportedFile.getAbsolutePath());
        if (!planeTextExportedFileFromAbsolutePath.getParentFile().exists()) {
            throw new RuntimeException("folder to export to is not found");
        }

        try {
            writeTranslations(planeTextExportedPath, dao.getAllTranslations());
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    private List<String> readFile(String path) throws IOException {
        BufferedReader br = null;
        List<String> lines = new ArrayList<>();
        try {
            String sCurrentLine;
            br = new BufferedReader(new FileReader(path));
            while ((sCurrentLine = br.readLine()) != null) {
                lines.add(sCurrentLine);
            }
        } finally {
            try {
                if (br != null) br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return lines;
    }

    private void writeTranslations(String path, Collection<Translation> translations) throws IOException {
        File fout = new File(path);
        FileOutputStream fos = new FileOutputStream(fout);

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

        for (Translation translation : translations) {
            bw.write(translation.getForeignWord() + " - " + translation.getNativeWord());
            bw.newLine();
        }

        bw.close();
    }
}
