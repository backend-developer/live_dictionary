package uk.ignas.langlearn.testutils;

import uk.ignas.langlearn.core.Dictionary;
import uk.ignas.langlearn.core.Translation;

import java.util.ArrayList;
import java.util.List;

public class LiveDictionaryDsl {
    public static List<Translation> retrieveTranslationsNTimes(Dictionary dictionary, int timesToExecute) {
        final List<Translation> retrievedTranslations = new ArrayList<>();
        for (int i = 0; i < timesToExecute; i++) {
            retrievedTranslations.add(dictionary.getRandomTranslation());
        }
        return retrievedTranslations;
    }

    public static int countPercentageOfRetrievedNativeWordInExpectedSet(List<Translation> retrieved, List<Translation> expectedSet) {
        int timesInterested = 0;
        for (Translation t: retrieved) {
            if (expectedSet.contains(t)) {
                timesInterested++;
            }
        }
        int timesTotal = retrieved.size();
        return calculatePercentage(timesInterested, timesTotal);
    }

    public static int countPercentageOfRetrievedNativeWordsHadExpectedPattern(List<Translation> retrievedTranslations, String expectedPattern) {
        int timesInterested = 0;
        for (Translation w: retrievedTranslations) {
            if (w.getNativeWord().get().contains(expectedPattern)) {
                timesInterested++;
            }
        }
        int timesTotal = retrievedTranslations.size();
        return calculatePercentage(timesInterested, timesTotal);
    }

    public static int calculatePercentage(int timesInterested, int timesTotal) {
        if (timesTotal > 100) {
            int relationTo100 = timesTotal / 100;
            return timesInterested / relationTo100;
        } else {
            int relationTo100 = 100 / timesTotal;
            return  timesInterested * relationTo100;
        }
    }
}
