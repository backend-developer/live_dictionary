package uk.ignas.langlearn.core.parser;

import android.content.Context;
import com.opencsv.CSVReader;
import uk.ignas.langlearn.core.Difficulty;
import uk.ignas.langlearn.core.Translation;
import uk.ignas.langlearn.core.db.DBHelper;

import java.io.*;
import java.util.*;

public class DbUtils {

    public static final char ENTRY_SEPARATOR = '|';
    public static final String SCV_HEADER = "Word" + ENTRY_SEPARATOR + "Translation";
    private TranslationParser translationParser = new TranslationParser();
    private final Context context;

    public DbUtils(Context context) {
        this.context = context;
    }

    public LinkedHashMap<Translation, Difficulty> getTranslationsFromDb(File translations) throws IOException {
        LinkedHashMap<Translation, Difficulty> questionList = new LinkedHashMap<>();
        InputStream csvStream = new FileInputStream(translations);
        InputStreamReader csvStreamReader = new InputStreamReader(csvStream);
        CSVReader csvReader = new CSVReader(csvStreamReader, ENTRY_SEPARATOR);

        String[] line;

        // throw away the header
        csvReader.readNext();

        while ((line = csvReader.readNext()) != null) {
            questionList.put(new Translation(line[0], line[1]), Difficulty.EASY);
        }

        return new DBHelper(context).getAllTranslations();
    }

    public void buildDbFromPlainTextFile(String planeTextFilePath, String csvFilePath) throws IOException {
        List<String> planeText = readFile(planeTextFilePath);
        List<String> csvText = new ArrayList<>();
        csvText.add(SCV_HEADER);
        DBHelper dbHelper = new DBHelper(context);
        dbHelper.deleteAll();

        Set<Translation> uniqueValidTranslations = new HashSet<>();

        for (String planeTextLine : planeText) {
            Translation parsed = translationParser.parse(planeTextLine);
            if (parsed != null) {
                uniqueValidTranslations.add(parsed);
            }
        }
        for (Translation translation: uniqueValidTranslations) {
            csvText.add(translation.getOriginalWord() + ENTRY_SEPARATOR + translation.getTranslatedWord());
        }
        new DBHelper(context).insert(uniqueValidTranslations);
        writeLines(csvFilePath, csvText);
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

    private void writeLines(String path, List<String> lines) throws IOException {
        File fout = new File(path);
        FileOutputStream fos = new FileOutputStream(fout);

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

        for (String line : lines) {
            bw.write(line);
            bw.newLine();
        }

        bw.close();
    }
}
