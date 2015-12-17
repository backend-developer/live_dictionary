package uk.ignas.langlearn.core;

import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.*;

public class QuestionnaireTest {

    private static int uniqueSequence = 0;

    @Test
    public void shouldThrowWhenGeneratingQuestionIfQuestionBaseIsEmpty() {
        Questionnaire questionnaire = new Questionnaire(new LinkedHashMap<Translation, Difficulty>());
        try {
            questionnaire.getRandomTranslation();
            fail();
        } catch (QuestionnaireException e) {
            assertTrue(e.getMessage().contains("no questions found"));
        }
    }

    @Test
    public void shouldNotCrashWhenThereAreFewWords() {
        LinkedHashMap<Translation, Difficulty> words = new LinkedHashMap<>();
        words.put(new Translation("word", "translation"), Difficulty.EASY);

        Questionnaire questionnaire = new Questionnaire(words);
        Translation translation = questionnaire.getRandomTranslation();
        assertThat(translation.getOriginalWord(), is(equalTo("word")));
    }

    @Test
    public void shouldGetFirst100QuestionsWith80PercentProbability() {
        Questionnaire questionnaire = new Questionnaire(get200QuestionsOutOfWhichNStartsWith(100, "FirstQ"));

        final List<String> retrievedWords = retrieveWordsNTimes(questionnaire, 1000);

        int percentage = countPercentageOfExpectedWordsRetrieved("FirstQ", retrievedWords);
        assertThat(percentage, allOf(greaterThan(75), lessThan(85)));
    }

    @Test
    public void afterFinding20UnknownWordsShouldNeverAskForOthers() {
        LinkedHashMap<Translation, Difficulty> allWords = getNQuestionsStartingWith(100, "Other");
        LinkedHashMap<Translation, Difficulty> unknownWords = getNQuestionsStartingWith(20, "UnknownWord");
        allWords.putAll(unknownWords);
        Questionnaire questionnaire = new Questionnaire(allWords);

        for (Translation t: unknownWords.keySet()) {
            questionnaire.markUnknown(t);
        }
        final List<String> retrievedWords = retrieveWordsNTimes(questionnaire, 100);

        int percentage = countPercentageOfExpectedWordsRetrieved("UnknownWord", retrievedWords);
        assertThat(percentage, is(equalTo(100)));
    }

    private List<String> retrieveWordsNTimes(Questionnaire questionnaire, int timesToExecute) {
        final List<String> retrievedWords = new ArrayList<>();
        for (int i = 0; i < timesToExecute; i++) {
            retrievedWords.add(questionnaire.getRandomTranslation().getOriginalWord());
        }
        return retrievedWords;
    }

    private int countPercentageOfExpectedWordsRetrieved(String expectedPattern, List<String> retrievedWords) {
        int counter;
        int counter1 = 0;
        for (String w: retrievedWords) {
            if (w.contains(expectedPattern)) {
                counter1++;
            }
        }
        counter = counter1;
        if (retrievedWords.size() > 100) {
            int relationTo100 = retrievedWords.size() / 100;
            return counter / relationTo100;
        } else {
            int relationTo100 = 100 / retrievedWords.size();
            return  counter * relationTo100;
        }
    }

    public LinkedHashMap<Translation, Difficulty> get200QuestionsOutOfWhichNStartsWith(int n, String prefixForFirst100Questions) {
        LinkedHashMap<Translation, Difficulty> translations = new LinkedHashMap<>();
        translations.putAll(getNQuestionsStartingWith(n, prefixForFirst100Questions));
        translations.putAll(getNQuestionsStartingWith(200-n, "Other"));
        return translations;
    }

    public LinkedHashMap<Translation, Difficulty> getNQuestionsStartingWith(int n, String prefix) {
        LinkedHashMap<Translation, Difficulty> translations = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) {
            translations.put(new Translation(prefix + getUniqueInt(), "t" + getUniqueInt()), Difficulty.EASY);
        }
        return translations;
    }

    private int getUniqueInt() {
        return uniqueSequence++;
    }
}
