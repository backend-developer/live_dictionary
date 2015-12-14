package uk.ignas.langlearn;

import com.google.common.collect.ImmutableMap;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.Random;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Test
public class QuestionnaireTest {

    @Test(expectedExceptions = QuestionnaireException.class, expectedExceptionsMessageRegExp = "no questions found")
    public void shouldThrowWhenGeneratingQuestionIfQuestionBaseIsEmpty() {
        Questionnaire questionnaire = new Questionnaire(new QuestionBase());

        questionnaire.drawQuestion();
    }

    public void shouldChooseRandomlyOneOutOfMultipleQuestionsProvided() {
        Map<Translation, Difficulty> questions = ImmutableMap.<Translation, Difficulty>builder()
                .put(new Translation("w1", "t1"), Difficulty.EASY)
                .put(new Translation("randomlyChosen", "t2"), Difficulty.EASY)
                .put(new Translation("w3", "t3"), Difficulty.EASY)
                .build();
        Random random = mock(Random.class);
        when(random.nextInt(questions.size())).thenReturn(1);
        Questionnaire questionnaire = new Questionnaire(new QuestionBase(questions), random);

        String question = questionnaire.drawQuestion();

        assertThat(question, equalTo("randomlyChosen"));
    }

    public void shouldChooseKnownWordsWithEqualProbability() {
        Map<Translation, Difficulty> questions = createEasyTranslations(4);
        Random random = mock(Random.class);
        when(random.nextInt(questions.size())).thenReturn(0, 1, 2, 3);
        Questionnaire questionnaire = new Questionnaire(new QuestionBase(questions), random);

        String q1 = questionnaire.drawQuestion();
        String q2 = questionnaire.drawQuestion();
        String q3 = questionnaire.drawQuestion();
        String q4 = questionnaire.drawQuestion();

        assertThat(q1, equalTo("w1"));
        assertThat(q2, equalTo("w2"));
        assertThat(q3, equalTo("w3"));
        assertThat(q4, equalTo("w4"));
    }

    private Map<Translation, Difficulty> createEasyTranslations(int n) {
        ImmutableMap.Builder<Translation, Difficulty> easyTranslations = ImmutableMap.builder();
        for (int i = 0; i < n; i++) {
            easyTranslations.put(new Translation("w" + (i + 1), "t" + (i + 1)), Difficulty.EASY);
        }
        return easyTranslations.build();
    }
}
