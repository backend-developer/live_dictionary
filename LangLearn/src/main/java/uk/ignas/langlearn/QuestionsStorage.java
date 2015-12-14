package uk.ignas.langlearn;

import android.os.Environment;
import com.opencsv.CSVReader;
import junit.framework.Assert;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class QuestionsStorage {

    public Map<Translation, Difficulty> getQuestions()  {
        File externalDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File applicationDir = new File(externalDir, "LangLearn");
        applicationDir.mkdirs();
        boolean isDirExists = externalDir.exists();
        if (!isDirExists) {
            Assert.fail();
        }
        File translations = new File(applicationDir, "translations");

        Map<Translation, Difficulty> questionList = new HashMap<>();

        try {
            InputStream csvStream = new FileInputStream(translations);
            InputStreamReader csvStreamReader = new InputStreamReader(csvStream);
            CSVReader csvReader = new CSVReader(csvStreamReader);
            String[] line;

            // throw away the header
            csvReader.readNext();

            while ((line = csvReader.readNext()) != null) {
                questionList.put(new Translation(line[0], line[1]), Difficulty.EASY);
            }
        } catch (IOException e) {
            throw new RuntimeException("asd");
        }

        return questionList;
    }
}
