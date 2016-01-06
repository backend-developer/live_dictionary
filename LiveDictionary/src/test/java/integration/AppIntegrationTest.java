package integration;

import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import uk.ignas.livedictionary.BuildConfig;
import uk.ignas.livedictionary.LiveDictionaryActivity;
import uk.ignas.livedictionary.core.*;
import uk.ignas.livedictionary.core.Dictionary;
import uk.ignas.livedictionary.testutils.LiveDictionaryDsl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import static com.google.common.collect.Iterables.getFirst;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class AppIntegrationTest {

    public static final String IMPORT_FILE_NAME = "import.txt";
    public static final String LIVE_DATA_RESOURCE_NAME = "exported_translations.txt";
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
    public void exportFileShouldContainSameNumberOfLines() throws IOException, URISyntaxException {
        DataImporterExporter dataImporterExporter = createImportedAndimportDataToDao(LIVE_DATA_RESOURCE_NAME, createSqliteDao());

        dataImporterExporter.export(EXPORT_FILE_NAME);

        int numberOfLines = 2652;
        validateNumberOfEntriesInFile(EXPORT_FILE_NAME, numberOfLines);
        validateNumberOfEntriesInFile(IMPORT_FILE_NAME, numberOfLines);
        validateImportedAndExportedFilesMatch(IMPORT_FILE_NAME, EXPORT_FILE_NAME);
    }

    @Test
    public void exportAndImportFilesShouldHaveSameOrderOfRecords() throws IOException, URISyntaxException {

        DataImporterExporter dataImporterExporter = createImportedAndimportDataToDao(LIVE_DATA_RESOURCE_NAME, createSqliteDao());
        dataImporterExporter.export(EXPORT_FILE_NAME);

        assertThat(readFile(IMPORT_FILE_NAME).get(0), is(equalTo("morado - purple")));
        assertThat(readFile(EXPORT_FILE_NAME).get(0), is(equalTo("morado - purple")));
    }

    @Test
    public void importedDataToDbShouldPreserveAnOrderInFile() throws IOException, URISyntaxException {
        TranslationDao dao = createSqliteDao();

        createImportedAndimportDataToDao(LIVE_DATA_RESOURCE_NAME, dao);

        assertThat(getFirst(dao.getAllTranslations(), null).getForeignWord().get(), is(equalTo("morado")));
    }

    @Test
    public void importedDataReplaceDbContents() throws IOException, URISyntaxException {
        TranslationDao dao = createSqliteDao();
        Translation translationsToBeReplaced = new Translation(new ForeignWord("palabra para borrar"), new NativeWord("word to delete"));
        dao.insertSingle(translationsToBeReplaced);

        createImportedAndimportDataToDao(LIVE_DATA_RESOURCE_NAME, dao);

        assertThat(dao.getAllTranslations(), not(hasItem(translationsToBeReplaced)));
    }

    @Test
    public void newestTranslationsShouldBeAskedMoreOftenThanOldOnes() throws IOException, URISyntaxException {
        TranslationDao dao = createSqliteDao();
        createImportedAndimportDataToDao(LIVE_DATA_RESOURCE_NAME, dao);
        Dictionary q = new Dictionary(dao);
        List<Translation> translations = dao.getAllTranslations();
        int size = translations.size();
        List<Translation> eldestTranslations = translations.subList(0, 100);
        List<Translation> newestTranslations = translations.subList(size - 100, size);

        List<Translation> retrieved = LiveDictionaryDsl.retrieveTranslationsNTimes(q, 10);

        int eldestCounter = LiveDictionaryDsl.countPercentageOfRetrievedNativeWordInExpectedSet(retrieved, eldestTranslations);
        int newestCounter = LiveDictionaryDsl.countPercentageOfRetrievedNativeWordInExpectedSet(retrieved, newestTranslations);
        assertThat(newestCounter, is(greaterThan(eldestCounter)));
    }

    private TranslationDao createSqliteDao() {
        LiveDictionaryActivity activity = Robolectric.setupActivity(LiveDictionaryActivity.class);
        return new TranslationDaoSqlite(activity);
    }

    private DataImporterExporter createImportedAndimportDataToDao(String liveDataResourceName, TranslationDao dao) throws URISyntaxException, IOException {
        URL resource = Resources.getResource(liveDataResourceName);
        File importFile = new File(resource.toURI());
        Files.copy(importFile, new File(IMPORT_FILE_NAME));
        DataImporterExporter dataImporterExporter = new DataImporterExporter(dao);

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