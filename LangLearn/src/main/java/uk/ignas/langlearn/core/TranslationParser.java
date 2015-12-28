package uk.ignas.langlearn.core;

import com.google.common.base.Splitter;
import uk.ignas.langlearn.core.Translation;


import java.util.List;

public class TranslationParser {
    public Translation parse(String line) {
        List<String> sides = Splitter.on("-").trimResults().splitToList(line);
        if (sides.size() != 2) {
            return null;
        }
        if (sides.get(0).isEmpty() || sides.get(1).isEmpty()) {
            return null;
        }
        return new Translation(new ForeignWord(sides.get(0)), new NativeWord(sides.get(1)));
    }
}
