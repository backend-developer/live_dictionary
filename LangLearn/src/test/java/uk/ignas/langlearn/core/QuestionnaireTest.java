package uk.ignas.langlearn.core;

import org.junit.Test;

import java.util.LinkedHashMap;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.*;


public class QuestionnaireTest {

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
        Questionnaire questionnaire = new Questionnaire(get200QuestionsOutOfWhich100StartsWith("FirstQ"));
        int counter = 0;
        for (int i = 0; i < 1000; i++) {
            String originalWord = questionnaire.getRandomTranslation().getOriginalWord();
            if (originalWord.contains("FirstQ")) {
                counter++;
            }
        }
        assertThat(counter, allOf(greaterThan(750), lessThan(850)));
    }

    public LinkedHashMap<Translation, Difficulty> get200QuestionsOutOfWhich100StartsWith(String prefixForFirst100Questions) {
        LinkedHashMap<Translation, Difficulty> translations = new LinkedHashMap<>();
        for (int i = 0; i < 100; i++) {
            translations.put(new Translation(prefixForFirst100Questions + i, "t" + i), Difficulty.EASY);
        }
        for (int i = 100; i < 200; i++) {
            translations.put(new Translation("B" + i, "t" + i), Difficulty.EASY);
        }
        return translations;
    }
}
