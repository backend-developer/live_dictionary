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
import com.google.common.base.Supplier;
import uk.ignas.langlearn.core.*;

import java.io.File;

public class QuestionnaireActivity extends Activity {
    private Button translationButton;
    private Button knownWordButton;
    private Button unknownWordButton;
    private Button addWordButton;
    private Button updateWordButton;
    private Button deleteWordButton;
    private Button exportDataButton;
    private EditText exportDataFileEditText;
    private TextView correctAnswerView;
    private TextView questionLabel;

    private Translation currentTranslation = new Translation(new ForeignWord("defaultWord"), new NativeWord("defaultTranslation"));
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
        final DataImporterExporter dataImporterExporter = new DataImporterExporter(dao, externalStoragePublicDirectory);
        dataImporterExporter.importAndValidateTranslations();

        questionnaire = new Questionnaire(dao);

        correctAnswerView = (TextView) findViewById(R.id.correct_answer);
        questionLabel = (TextView) findViewById(R.id.question_label);

        publishNextWord();

        translationButton = (Button) findViewById(R.id.show_translation_button);
        knownWordButton = (Button) findViewById(R.id.known_word_submision_button);
        unknownWordButton = (Button) findViewById(R.id.unknown_word_submision_button);
        addWordButton = (Button) findViewById(R.id.add_word_button);
        updateWordButton = (Button) findViewById(R.id.update_word_button);
        deleteWordButton = (Button) findViewById(R.id.delete_word_button);
        exportDataButton = (Button) findViewById(R.id.export_data_button);
        exportDataFileEditText = (EditText) findViewById(R.id.export_data_path_textedit);
        File defaultExportFile = new File(externalStoragePublicDirectory, "ExportedByUserRequest.txt");
        exportDataFileEditText.setText(defaultExportFile.getAbsolutePath());

        enableTranslationAndNotSubmittionButtons(true);
        translationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                correctAnswerView.setText(currentTranslation.getForeignWord().get());
                enableTranslationAndNotSubmittionButtons(false);
            }
        });

        knownWordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publishNextWord();
                enableTranslationAndNotSubmittionButtons(true);
                questionnaire.markKnown(currentTranslation);
            }
        });

        unknownWordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publishNextWord();
                enableTranslationAndNotSubmittionButtons(true);
                questionnaire.markUnknown(currentTranslation);
            }
        });

        deleteWordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(QuestionnaireActivity.this)
                        .setMessage("Delete this word?")
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                questionnaire.delete(currentTranslation);
                                publishNextWord();
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

        CurrentTranslationSupplier translationSupplier = new CurrentTranslationSupplier();
        View.OnClickListener onAddWordListener = OnModifyDictionaryClickListener.onInsertingWord(QuestionnaireActivity.this, questionnaire);
        addWordButton.setOnClickListener(onAddWordListener);
        View.OnClickListener onUpdateWordListener = OnModifyDictionaryClickListener.onUpdatingWord(QuestionnaireActivity.this, questionnaire, translationSupplier);
        updateWordButton.setOnClickListener(onUpdateWordListener);

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

    private void publishNextWord() {
        try {
            currentTranslation = questionnaire.getRandomTranslation();
            questionLabel.setText(currentTranslation.getNativeWord().get());
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

    public class CurrentTranslationSupplier implements Supplier<Translation>{

        @Override
        public Translation get() {
            return currentTranslation;
        }
    }
}
