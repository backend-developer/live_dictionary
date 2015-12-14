package uk.ignas.langlearn;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.test.uiautomator.*;
import android.test.InstrumentationTestCase;
import android.widget.EditText;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class QuestionnaireAcceptanceTest extends InstrumentationTestCase {
    private static final long TIME_MILLIS_UNTIL_COMPONENT_IS_LOADED = 2000;
    public static final String PACKAGE = "uk.ignas.langlearn";
    private UiDevice device;
    public static final String WORD = "bread";
    public static final String TRANSLATED_WORD = "la pan";

    public void testUnansweredQuestionIsAskedInitially() throws UiObjectNotFoundException, IOException {
        removePersistentTranslationsAndPlace(WORD, TRANSLATED_WORD);
        int i = 0x00_00_0f;
        reopenQuestionnaireActivity();

        assertEquals(WORD, getQuestionLabel().getText());
        assertEquals("", getAnswerInput().getText());
    }

    public void testErrorsAreShownInDialog() throws UiObjectNotFoundException, IOException {
        removePersistentTranslations();
        reopenQuestionnaireActivity();

        assertEquals("LangLearn", getAlertTitle().getText());
        assertEquals("Invalid CSV format: No Title", getAlertMessage().getText());
    }

    public void testErrorDialogLeadsToQuitingTheApplication() throws UiObjectNotFoundException, IOException {
        removePersistentTranslations();
        reopenQuestionnaireActivity();

        clickDialogButton();

        assertTrue(device.wait(Until.gone(By.pkg(PACKAGE).depth(0)), TIME_MILLIS_UNTIL_COMPONENT_IS_LOADED));
    }

    private void reopenQuestionnaireActivity() {
        device = UiDevice.getInstance(getInstrumentation());
        Context context = getInstrumentation().getContext();

        device.pressHome();
        try {
            TimeUnit.SECONDS.sleep(6);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Intent intent = context.getPackageManager().getLaunchIntentForPackage("uk.ignas.langlearn");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);



        context.startActivity(intent);
        assertTrue(device.wait(Until.hasObject(By.pkg(PACKAGE).depth(1)), TIME_MILLIS_UNTIL_COMPONENT_IS_LOADED));
    }

    private void removePersistentTranslationsAndPlace(String word, String translated) throws IOException {
        if (Objects.equals(Environment.getExternalStorageState(), Environment.MEDIA_MOUNTED)) {
            String data = "Word,TranslatedWord\n" + word + "," + translated;
            AndroidContextBasedFileUtils.createQuestionsFileWithData(data);
        } else {
            fail();
        }
    }

    private void removePersistentTranslations() throws IOException {
        if (Objects.equals(Environment.getExternalStorageState(), Environment.MEDIA_MOUNTED)) {
            String data = "";
            AndroidContextBasedFileUtils.createQuestionsFileWithData(data);
        } else {
            fail();
        }
    }

    private UiObject getQuestionLabel() {
        UiObject questionLabel = device.findObject(new UiSelector().resourceIdMatches(".*question_label.*"));
        assertTrue(questionLabel.exists());
        return questionLabel;
    }

    private UiObject getAnswerInput() {
        UiObject answerInput = device.findObject(new UiSelector().className(EditText.class));
        assertTrue(answerInput.exists());
        return answerInput;
    }

    private UiObject getAlertTitle() {
        UiObject alertTitle = device.findObject(new UiSelector().resourceIdMatches(".*alertTitle.*"));
        assertTrue(alertTitle.exists());
        return alertTitle;
    }

    private UiObject getAlertMessage() {
        UiObject alertMessage = device.findObject(new UiSelector().resourceIdMatches(".*message.*"));
        assertTrue(alertMessage.exists());
        return alertMessage;
    }

    private void clickDialogButton() throws UiObjectNotFoundException {
        UiObject button = device.findObject(new UiSelector().resourceIdMatches(".*button1.*"));
        assertTrue(button.exists());
        button.click();
    }



}
