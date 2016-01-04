package uk.ignas.langlearn.core;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import uk.ignas.langlearn.BuildConfig;
import uk.ignas.langlearn.LiveDictionaryActivity;
import uk.ignas.langlearn.R;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class RoboElectricTest {

    @Test
    @Ignore
    public void clickingLogin_shouldStartLoginActivity() {
        LiveDictionaryActivity activity = Robolectric.setupActivity(LiveDictionaryActivity.class);
        activity.findViewById(R.id.show_translation_button).performClick();
        TextView answerView = (TextView) activity.findViewById(R.id.correct_answer);
        assertThat(answerView.getText().toString(), is(equalTo("morado")));
    }
}
