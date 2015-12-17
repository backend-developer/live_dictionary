package uk.ignas.langlearn;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import org.apache.commons.lang3.mutable.MutableObject;
import uk.ignas.langlearn.core.Questionnaire;
import uk.ignas.langlearn.core.Translation;


public class QuestionnaireActivity extends Activity {
    private Button translationButton;
    private Button knownWordButton;
    private Button unknownWordButton;

    private QuestionsStorage questionsStorage = new QuestionsStorage();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire);
    }

    @Override
    protected void onResume() {
        super.onResume();

        final TextView correctAnswerView = (TextView) findViewById(R.id.correct_answer);
        final MutableObject<Translation> currentWord = new MutableObject<>(new Translation("defaultWord", "defaultTranslation"));
        final TextView questionLabel = (TextView) findViewById(R.id.question_label);
        publishNextWord(currentWord, questionLabel, correctAnswerView);

        translationButton = (Button) findViewById(R.id.show_translation_button);
        knownWordButton = (Button) findViewById(R.id.known_word_submision_button);
        unknownWordButton = (Button) findViewById(R.id.unknown_word_submision_button);

        enableTranslationAndNotSubmittionButtons(true);
        translationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                correctAnswerView.setText(currentWord.getValue().getTranslatedWord());
                enableTranslationAndNotSubmittionButtons(false);
            }
        });

        knownWordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publishNextWord(currentWord, questionLabel, correctAnswerView);
                enableTranslationAndNotSubmittionButtons(true);
            }
        });

        unknownWordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publishNextWord(currentWord, questionLabel, correctAnswerView);
                enableTranslationAndNotSubmittionButtons(true);
            }
        });
    }

    private void enableTranslationAndNotSubmittionButtons(boolean enabled) {
        translationButton.setEnabled(enabled);
        knownWordButton.setEnabled(!enabled);
        unknownWordButton.setEnabled(!enabled);
    }

    private void publishNextWord(MutableObject<Translation> currentWord, TextView questionLabel, TextView correctAnswerView) {
        try {

            Questionnaire q = new Questionnaire(questionsStorage.getQuestions());
            currentWord.setValue(q.getRandomTranslation());
            questionLabel.setText(currentWord.getValue().getOriginalWord());
            correctAnswerView.setText("");
        } catch (RuntimeException e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("LangLearn")
                    .setMessage("Error occured:" + e.getMessage())
                    .setPositiveButton("button", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
}
