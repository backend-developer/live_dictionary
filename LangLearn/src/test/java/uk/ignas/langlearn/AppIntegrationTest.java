package uk.ignas.langlearn;

import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.junit.After;
import org.junit.Test;
import uk.ignas.langlearn.core.*;

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

    public static final String IMPORT_FILE_NAME = "import.txt";
    public static final String LIVE_DATA_RESOURCE_NAME = "exported_words.txt";
    public static final String EXPORT_FILE_NAME = "export.txt";

    @After
    public void teardown() {
        removeFileIfExists(IMPORT_FILE_NAME);
        removeFileIfExists(EXPORT_FILE_NAME);
    }

    private void removeFileIfExists(String fileName) {
        File importFile = new File(fileName);
        if (importFile.exists()) {
            importFile.delete();
        }
    }

    @Test
    public void exportFileShouldNotContainBlankLines() throws IOException, URISyntaxException {

        DataImporterExporter dataImporterExporter = createImportedAndimportDataToDao(LIVE_DATA_RESOURCE_NAME, new TranslationDaoStub());
        dataImporterExporter.export(EXPORT_FILE_NAME);

        validateNumberOfEntriesInFile(EXPORT_FILE_NAME, 2491);
        validateNumberOfEntriesInFile(IMPORT_FILE_NAME, 2627);
        validateImportedAndExportedFilesMatch(IMPORT_FILE_NAME, EXPORT_FILE_NAME);
        assertThat(readFile(IMPORT_FILE_NAME).get(0), is(equalTo("morado - purple")));
        assertThat(readFile(IMPORT_FILE_NAME).get(0), is(equalTo("morado - purple")));
    }

    @Test
    public void importedDataShouldBeInOrderDataAppearedInFile() throws IOException, URISyntaxException {
        TranslationDaoStub dao = new TranslationDaoStub();

        createImportedAndimportDataToDao(LIVE_DATA_RESOURCE_NAME, dao);

        assertThat(getLast(dao.getAllTranslations().keySet()).getForeignWord().get(), is(equalTo("la chaqueta de piel")));
    }

    @Test
    public void newestQuestionsShouldBeMixedUpWitOldestOnes() throws IOException, URISyntaxException {
        TranslationDaoStub dao = new TranslationDaoStub();
        createImportedAndimportDataToDao(LIVE_DATA_RESOURCE_NAME, dao);
        Questionnaire q = new Questionnaire(dao);
        List<Translation> translations = new ArrayList<>(dao.getAllTranslations().keySet());
        int size = translations.size();
        List<Translation> eldestTranslations = translations.subList(0, 100);
        List<Translation> newestTranslations = translations.subList(size - 100, size);

        List<Translation> retrieved = LiveDictionaryDsl.retrieveWordsNTimes(q, 10);

        int eldestCounter = LiveDictionaryDsl.countPercentageOfRetrievedNativeWordsInExpectedSet(retrieved, eldestTranslations);
        int newestCounter = LiveDictionaryDsl.countPercentageOfRetrievedNativeWordsInExpectedSet(retrieved, newestTranslations);
        assertThat(newestCounter, is(greaterThan(eldestCounter)));
    }



    private DataImporterExporter createImportedAndimportDataToDao(String liveDataResourceName, TranslationDaoStub dao) throws URISyntaxException, IOException {
        URL resource = Resources.getResource(liveDataResourceName);
        File wordsToImport = new File(resource.toURI());
        Files.copy(wordsToImport, new File(IMPORT_FILE_NAME));
        DataImporterExporter dataImporterExporter = new DataImporterExporter(dao, new File("."));

        dataImporterExporter.importFromFile(IMPORT_FILE_NAME);
        return dataImporterExporter;
    }

    private void validateNumberOfEntriesInFile(String fileName, int expectedNumberOfEntries) {
        if (!new File(fileName).exists()) {
            throw new RuntimeException("validation failed. invalid export file specified");
        }
        List<String> exportedData = readFile(fileName);
        assertThat(exportedData.size(), is(equalTo(expectedNumberOfEntries)));
    }

    private void validateImportedAndExportedFilesMatch(String dataToImportFileName, String exportedDataFileName) {
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