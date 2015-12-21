package uk.ignas.langlearn.core;

import android.content.Context;
import android.os.Environment;
import uk.ignas.langlearn.core.parser.DbUtils;

import java.io.File;

public class DataImporterExporter {

    private Context context;

    public DataImporterExporter(Context context) {
        this.context = context;
    }

    public void importAndValidateTranslations() {
        File externalDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        String dataToImportFileName = "SpanishWords.txt";
        File dataToImportFile = new File(externalDir, dataToImportFileName);
        String exportedDataFileName = "PlaneTextExportedFile.txt";
        File exportedDataFile = new File(externalDir, exportedDataFileName);

        if (!externalDir.exists()) {
            throw new RuntimeException("application dir cannot be created");
        }

        try {
            new DbUtils(context).importFromFile(dataToImportFile.getAbsolutePath());
            reexport(exportedDataFileName);
            new DbUtils(context).validateImportAndExportWorksConsistently(dataToImportFile.getAbsolutePath(), exportedDataFile.getAbsolutePath());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void reexport(String exportedDataFileName) {
        File externalDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        if (!externalDir.exists()) {
            throw new RuntimeException("application dir cannot be created");
        }
        File planeTextExportedFile = new File(externalDir, exportedDataFileName);

        if (planeTextExportedFile.exists()) {
            if(!planeTextExportedFile.delete()) {
                throw new RuntimeException("data cannot be exported. File cannot be deleted");
            }
        }
        new DbUtils(context).export(planeTextExportedFile.getAbsolutePath());
    }
}
