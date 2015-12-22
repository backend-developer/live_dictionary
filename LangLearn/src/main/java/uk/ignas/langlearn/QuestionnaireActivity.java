package uk.ignas.langlearn;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import uk.ignas.langlearn.core.DataImporterExporter;
import uk.ignas.langlearn.core.Difficulty;
import uk.ignas.langlearn.core.Questionnaire;
import uk.ignas.langlearn.core.Translation;
import uk.ignas.langlearn.core.db.TranslationDaoSqlite;
import uk.ignas.langlearn.core.parser.DbUtils;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Set;

public class QuestionnaireActivity extends Activity {
    private Button translationButton;
    private Button knownWordButton;
    private Button unknownWordButton;
    private Button exportDataButton;
    private EditText exportDataFileEditText;
    private LinkedHashMap<Translation, Difficulty> questionsList;

    private Translation currentWord = new Translation("defaultWord", "defaultTranslation");
    private Questionnaire questionnaire;
    private TranslationDaoSqlite dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire);
    }

    @Override
    protected void onResume() {
        super.onResume();

        File externalStoragePublicDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        dao = new TranslationDaoSqlite(QuestionnaireActivity.this);
        final DataImporterExporter dataImporterExporter = new DataImporterExporter(this, dao, externalStoragePublicDirectory);
        dataImporterExporter.importAndValidateTranslations();

        questionnaire = new Questionnaire(dao);

        final TextView correctAnswerView = (TextView) findViewById(R.id.correct_answer);

        final TextView questionLabel = (TextView) findViewById(R.id.question_label);

        publishNextWord(questionLabel, correctAnswerView);

        translationButton = (Button) findViewById(R.id.show_translation_button);
        knownWordButton = (Button) findViewById(R.id.known_word_submision_button);
        unknownWordButton = (Button) findViewById(R.id.unknown_word_submision_button);
        exportDataButton = (Button) findViewById(R.id.export_data_button);
        exportDataFileEditText = (EditText) findViewById(R.id.export_data_path_textedit);
        File defaultExportFile = new File(externalStoragePublicDirectory, "ExportedByUserRequest.txt");
        exportDataFileEditText.setText(defaultExportFile.getAbsolutePath());

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
                dataImporterExporter.reexport(exportDataFileEditText.getText().toString());
            }
        });
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
