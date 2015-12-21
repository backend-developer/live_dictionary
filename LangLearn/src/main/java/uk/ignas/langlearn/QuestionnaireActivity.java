package uk.ignas.langlearn;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import uk.ignas.langlearn.core.Questionnaire;
import uk.ignas.langlearn.core.Translation;

import java.util.Set;

public class QuestionnaireActivity extends Activity {
    private Button translationButton;
    private Button knownWordButton;
    private Button unknownWordButton;
    private Button exportDataButton;

    private Translation currentWord = new Translation("defaultWord", "defaultTranslation");
    private Questionnaire questionnaire;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire);
    }

    @Override
    protected void onResume() {
        super.onResume();

        questionnaire = new Questionnaire(new QuestionsStorage(this).getQuestions());

        final TextView correctAnswerView = (TextView) findViewById(R.id.correct_answer);

        final TextView questionLabel = (TextView) findViewById(R.id.question_label);

        publishNextWord(questionLabel, correctAnswerView);

        translationButton = (Button) findViewById(R.id.show_translation_button);
        knownWordButton = (Button) findViewById(R.id.known_word_submision_button);
        unknownWordButton = (Button) findViewById(R.id.unknown_word_submision_button);
        exportDataButton = (Button) findViewById(R.id.export_data_button);

        enableTranslationAndNotSubmittionButtons(true);
        translationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                correctAnswerView.setText(currentWord.getTranslatedWord());
                enableTranslationAndNotSubmittionButtons(false);
            }
        });

        knownWordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publishNextWord(questionLabel, correctAnswerView);
                enableTranslationAndNotSubmittionButtons(true);
                questionnaire.markKnown(currentWord);
            }
        });

        unknownWordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publishNextWord(questionLabel, correctAnswerView);
                enableTranslationAndNotSubmittionButtons(true);
                questionnaire.markUnknown(currentWord);
            }
        });

        exportDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new QuestionsStorage(QuestionnaireActivity.this).exportData();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        Set<Translation> unknownQuestions = questionnaire.getUnknownQuestions();
        new QuestionsStorage(this).markUnknown(unknownQuestions);
    }

    private void enableTranslationAndNotSubmittionButtons(boolean isTranslationPhase) {
        final boolean isSubmittionPhase = !isTranslationPhase;
        translationButton.setEnabled(isTranslationPhase);
        knownWordButton.setEnabled(isSubmittionPhase);
        unknownWordButton.setEnabled(isSubmittionPhase);
    }

    private void publishNextWord(TextView questionLabel, TextView correctAnswerView) {
        try {
            currentWord =questionnaire.getRandomTranslation();
            questionLabel.setText(currentWord.getOriginalWord());
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
