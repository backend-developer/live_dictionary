package uk.ignas.langlearn;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.core.deps.guava.collect.ImmutableMap;
import android.test.ActivityInstrumentationTestCase2;
import org.junit.Before;

import java.io.IOException;
import java.util.Map;

public class QuestionsStorageIntegrationTest extends ActivityInstrumentationTestCase2<QuestionnaireActivity> {

    public QuestionsStorageIntegrationTest() {
        super(QuestionnaireActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
    }

    public void testStorageLoadDataToDataStructure() throws IOException {
        AndroidContextBasedFileUtils.createQuestionsFileWithData("Word,TranslatedWord\nceiling,el suelo");
        QuestionsStorage storage = new QuestionsStorage();
        Map<Translation, Difficulty> expected = ImmutableMap.<Translation, Difficulty>of(
                new Translation("ceiling", "el suelo"), Difficulty.EASY
        );

        Map<Translation, Difficulty> questions = storage.getQuestions();

        assertEquals(expected, questions);
    }
}
