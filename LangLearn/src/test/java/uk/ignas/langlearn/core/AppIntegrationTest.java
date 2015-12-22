package uk.ignas.langlearn.core;

import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import static com.google.common.collect.Iterables.getLast;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.IsEqual.equalTo;

public class AppIntegrationTest {

    @Test
    public void exportFileShouldNotContainBlankLines() throws IOException, URISyntaxException {
        URL resource = Resources.getResource("exported_words.txt");
        File wordsToImport = new File(resource.toURI());
        Files.copy(wordsToImport, new File("import.txt"));
        TranslationDaoStub dao = new TranslationDaoStub();
        DataImporterExporter dataImporterExporter = new DataImporterExporter(dao, new File("."));

        dataImporterExporter.importFromFile("import.txt");
        dataImporterExporter.export("export.txt");

        validateNumberOfEntriesInFile("export.txt", 2491);
        validateNumberOfEntriesInFile("import.txt", 2627);
        validateImportedAndExportedFilesMatch("import.txt", "export.txt");
        assertThat(readFile("import.txt").get(0), is(equalTo("morado - purple")));
        assertThat(readFile("export.txt").get(0), is(equalTo("morado - purple")));
    }

    @Test
    public void importedDataShouldBeInOrderDataAppearedInFile() throws IOException, URISyntaxException {
        URL resource = Resources.getResource("exported_words.txt");
        File wordsToImport = new File(resource.toURI());
        Files.copy(wordsToImport, new File("import.txt"));
        TranslationDaoStub dao = new TranslationDaoStub();
        DataImporterExporter dataImporterExporter = new DataImporterExporter(dao, new File("."));

        dataImporterExporter.importFromFile("import.txt");

        assertThat(getLast(dao.getAllTranslations().keySet()).getTranslatedWord(), is(equalTo("la chaqueta de piel")));
    }

    @Test
    public void newsetQuestionsShouldBeMixedUpWitOldestOnes() throws IOException, URISyntaxException {
        URL resource = Resources.getResource("exported_words.txt");
        File wordsToImport = new File(resource.toURI());
        Files.copy(wordsToImport, new File("import.txt"));
        TranslationDaoStub dao = new TranslationDaoStub();
        DataImporterExporter dataImporterExporter = new DataImporterExporter(dao, new File("."));
        dataImporterExporter.importFromFile("import.txt");
        Questionnaire q = new Questionnaire(dao);

        List<Translation> translations = new ArrayList<>(dao.getAllTranslations().keySet());
        int size = translations.size();
        List<Translation> eldestTranslations = translations.subList(0, 100);
        List<Translation> newestTranslations = translations.subList(size - 100, size);
        int eldestCounter = 0;
        int newestCounter = 0;

        for (int i = 0; i < 10; i++) {
            Translation translation = q.getRandomTranslation();
            if(eldestTranslations.contains(translation)) {
                eldestCounter++;
            }
            if (newestTranslations.contains(translation)) {
                newestCounter++;
            }
        }
        assertThat(newestCounter, is(greaterThan(eldestCounter)));
    }

    private void validateNumberOfEntriesInFile(String fileName, int expectedNumberOfEntries) {
        if (!new File(fileName).exists()) {
            throw new RuntimeException("validation failed. invalid export file specified");
        }
        List<String> exportedData = readFile(fileName);
        assertThat(exportedData.size(), is(equalTo(expectedNumberOfEntries)));
    }

    public void validateImportedAndExportedFilesMatch(String dataToImportFileName, String exportedDataFileName) {
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
        assertThat(dataToImportSet, is(equalTo(exportedDataSet)));
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