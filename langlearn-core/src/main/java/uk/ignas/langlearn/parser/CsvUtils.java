package uk.ignas.langlearn.parser;

import uk.ignas.langlearn.Translation;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CsvUtils {

    public static final String SCV_HEADER = "Word|Translation";
    private TranslationParser translationParser = new TranslationParser();

    public static void main(String[] args) throws IOException {
        new CsvUtils().buildScvFromPlainTextFile("/home/ignas/SpanishWords.txt", "/home/ignas/parsed.txt");
    }

    public void buildScvFromPlainTextFile(String planeTextFilePath, String csvFilePath) throws IOException {
        List<String> planeText = readFile(planeTextFilePath);
        List<String> csvText = new ArrayList<>();
        csvText.add(SCV_HEADER);
        for (String planeTextLine: planeText) {
            Translation parsed = translationParser.parse(planeTextLine);
            if (parsed != null) {
                csvText.add(parsed.getOriginalWord() + "|" + parsed.getTranslatedWord());
            }
        }
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
                if (br != null)br.close();
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

        for (String line: lines) {
            bw.write(line);
            bw.newLine();
        }

        bw.close();
    }
}
