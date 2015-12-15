package uk.ignas.langlearn;

import android.os.Environment;
import com.opencsv.CSVReader;
import junit.framework.Assert;
import uk.ignas.langlearn.parser.CsvUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class QuestionsStorage {

    public Map<Translation, Difficulty> getQuestions()  {
        File externalDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File applicationDir = new File(externalDir, "LangLearn");
        File planeTextFileDir = new File(externalDir, "SpanishWords.txt");
        File translations = new File(applicationDir, "translations");

        applicationDir.mkdirs();
        boolean isDirExists = externalDir.exists();
        if (!isDirExists) {
            Assert.fail();
        }

        Map<Translation, Difficulty> questionList = new HashMap<>();

        try {
            new CsvUtils().buildScvFromPlainTextFile(planeTextFileDir.getAbsolutePath(), translations.getAbsolutePath());

            InputStream csvStream = new FileInputStream(translations);
            InputStreamReader csvStreamReader = new InputStreamReader(csvStream);
            CSVReader csvReader = new CSVReader(csvStreamReader, '|');

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
