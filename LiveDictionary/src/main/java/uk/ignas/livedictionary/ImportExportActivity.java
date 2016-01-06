package uk.ignas.livedictionary;

import android.app.Activity;
import android.content.Intent;
import uk.ignas.livedictionary.core.DataImporterExporter;
import uk.ignas.livedictionary.core.Dictionary;

public class ImportExportActivity {
    private DataImporterExporter dataImporterExporter;
    private Dictionary dictionary;
    private GuiError guiError;

    public ImportExportActivity(DataImporterExporter dataImporterExporter, Dictionary dictionary, GuiError guiError) {
        this.dataImporterExporter = dataImporterExporter;
        this.dictionary = dictionary;
        this.guiError = guiError;
    }

    public void startActivity(Activity activity, int resultCode) {
        Intent intentToExport = new Intent(Intent.ACTION_GET_CONTENT);
        intentToExport.setType("file/*");
        activity.startActivityForResult(intentToExport, resultCode);
    }

    public void handleImportResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            String filePath = data.getData().getPath();
            try {
                dataImporterExporter.importFromFile(filePath);
            } catch (RuntimeException e) {
                guiError.showErrorDialogAndContinue(e);
            }
            dictionary.reloadData();
        }
    }

    public void handleExportResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            String filePath = data.getData().getPath();
            try {
                dataImporterExporter.export(filePath);
            } catch (RuntimeException e) {
                guiError.showErrorDialogAndContinue(e);
            }
            dictionary.reloadData();
        }
    }
}
