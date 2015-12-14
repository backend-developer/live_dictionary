package uk.ignas.langlearn;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.core.deps.guava.collect.ImmutableMap;
import android.test.ActivityInstrumentationTestCase2;
import org.junit.Before;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class QuestionnaireActivityIntegrationTest extends ActivityInstrumentationTestCase2<QuestionnaireActivity> {

    private QuestionsStorage questionsStorage;

    public QuestionnaireActivityIntegrationTest() {
        super(QuestionnaireActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        questionsStorage = mock(QuestionsStorage.class);
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        injectMockedDependencies();
    }

    private void injectMockedDependencies() {
        QuestionnaireApplication app = (QuestionnaireApplication) getInstrumentation().getTargetContext().getApplicationContext();
        QuestionnaireComponent component = DaggerQuestionnaireComponent.builder().questionnaireModule(new QuestionnaireModule() {
            @Override
            QuestionsStorage provideQuestionsStorage() {
                    return questionsStorage;
            }
        }).build();
        app.setComponent(component);
    }

    public void testQuestionShownIsLoadedUsingStorage() {
        when(questionsStorage.getQuestions()).thenReturn(ImmutableMap.<Translation, Difficulty>of(
                new Translation("student", "anything"), Difficulty.EASY
        ));
        getActivity();
        onView(withId(R.id.question_label)).check(matches(withText("student")));
    }
}
