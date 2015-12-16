package uk.ignas.langlearn.core;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.*;


public class QuestionnaireTest {

    @Test
    public void shouldThrowWhenGeneratingQuestionIfQuestionBaseIsEmpty() {
        Questionnaire questionnaire = new Questionnaire(ImmutableMap.<Translation, Difficulty>of());
        try {
            questionnaire.drawQuestion();
            fail();
        } catch (QuestionnaireException e) {
            assertTrue(e.getMessage().contains("no questions found"));
        }
    }

    @Test
    public void shouldGetFirst100QuestionsWith80PercentProbability() {
        Map<Translation, Difficulty> words = ImmutableMap.<Translation, Difficulty>of(
                new Translation("word", "translation"), Difficulty.EASY);

        Questionnaire questionnaire = new Questionnaire(words);
        Translation translation = questionnaire.getRandomTranslation();
        assertThat(translation.getOriginalWord(), is(equalTo("word")));
    }

    @Test
    public void shouldNotCrashWhenThereAreFewWords() {
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

    public Map<Translation, Difficulty> get200QuestionsOutOfWhich100StartsWith(String prefixForFirst100Questions) {
        ImmutableMap.Builder<Translation, Difficulty> builder = ImmutableMap.<Translation, Difficulty>builder();
        for (int i = 0; i < 100; i++) {
            builder.put(new Translation(prefixForFirst100Questions + i, "t" + i), Difficulty.EASY);
        }
        for (int i = 100; i < 200; i++) {
            builder.put(new Translation("B" + i, "t" + i), Difficulty.EASY);
        }
        return builder.build();
    }
}
