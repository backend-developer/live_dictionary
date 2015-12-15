package uk.ignas.langlearn.parser;

import com.google.common.base.Splitter;
import uk.ignas.langlearn.Translation;

import java.util.List;

public class TranslationParser {


    public Translation parse(String line) {
        List<String> wordAndTranslation = Splitter.on("-").trimResults().splitToList(line);
        if (wordAndTranslation.size() != 2) {
            return null;
        }
        if (wordAndTranslation.get(0).isEmpty() || wordAndTranslation.get(1).isEmpty()) {
            return null;
        }
        return new Translation(wordAndTranslation.get(1), wordAndTranslation.get(0));
    }
}
