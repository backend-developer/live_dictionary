package uk.ignas.langlearn;

import android.os.Environment;
import junit.framework.Assert;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class AndroidContextBasedFileUtils {
    public static void createQuestionsFileWithData(String data) throws IOException {
        File translationsFile = AndroidContextBasedFileUtils.prepareTranslationFile();
        clearFileAndPutTranslation(translationsFile, data);
    }

    static File prepareTranslationFile() {
        File externalDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File applicationDir = new File(externalDir, "LangLearn");
        applicationDir.mkdirs();
        boolean isDirExists = externalDir.exists();
        if (!isDirExists) {
            Assert.fail();
        }
        return new File(applicationDir, "translations");
    }

    static void clearFileAndPutTranslation(File translationsFile, String data) throws IOException {
        FileOutputStream fos = new FileOutputStream(translationsFile);
        fos.write(data.getBytes());
        fos.close();
    }
}
