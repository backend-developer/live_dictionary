package uk.ignas.langlearn.core;

import android.content.Context;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.junit.Test;
import uk.ignas.langlearn.core.db.TranslationDaoStub;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import static com.google.common.collect.Iterables.getLast;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;

public class DataImporterExporterIntegrationTest {

    @Test
    public void exportFileShouldNotContainBlankLines() throws IOException, URISyntaxException {
        URL resource = Resources.getResource("exported_words.txt");
        File wordsToImport = new File(resource.toURI());
        Files.copy(wordsToImport, new File("import.txt"));
        TranslationDaoStub dao = new TranslationDaoStub();
        DataImporterExporter dataImporterExporter = new DataImporterExporter(mock(Context.class), dao, new File("."));

        dataImporterExporter.importFromFile("import.txt");
        dataImporterExporter.export("export.txt");

        validateNumberOfEntriesInExportedFile("export.txt", 2491);
        validateImportedAndExportedFilesMatch("import.txt", "export.txt");
        assertThat(getLast(dao.getAllTranslations().keySet()).getTranslatedWord(), is(equalTo("la chaqueta de piel")));
    }

    private void validateNumberOfEntriesInExportedFile(String exportedDataFileName, int numberOfEntries) {
        if (!new File(exportedDataFileName).exists()) {
            throw new RuntimeException("validation failed. invalid export file specified");
        }
        List<String> exportedData = readFile(exportedDataFileName);
        assertThat(exportedData.size(), is(equalTo(numberOfEntries)));
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