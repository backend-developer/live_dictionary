package uk.ignas.langlearn.core;

import java.util.ArrayList;
import java.util.List;

public class LiveDictionaryDsl {
    public static List<Translation> retrieveWordsNTimes(Questionnaire questionnaire, int timesToExecute) {
        final List<Translation> retrievedWords = new ArrayList<>();
        for (int i = 0; i < timesToExecute; i++) {
            retrievedWords.add(questionnaire.getRandomTranslation());
        }
        return retrievedWords;
    }



    public static int countPercentageOfRetrievedWordsInExpectedSet(List<Translation> retrieved, List<Translation> expectedSet) {
        int timesInterested = 0;
        for (Translation t: retrieved) {
            if (expectedSet.contains(t)) {
                timesInterested++;
            }
        }
        int timesTotal = retrieved.size();
        return calculatePercentage(timesInterested, timesTotal);
    }

    public static int countPercentageOfRetrievedWordsHadExpectedPattern(List<Translation> retrievedWords, String expectedPattern) {
        int timesInterested = 0;
        for (Translation w: retrievedWords) {
            if (w.getOriginalWord().contains(expectedPattern)) {
                timesInterested++;
            }
        }
        int timesTotal = retrievedWords.size();
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
