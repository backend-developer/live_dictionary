package uk.ignas.langlearn;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import uk.ignas.langlearn.core.DataImporterExporter;
import uk.ignas.langlearn.core.Questionnaire;
import uk.ignas.langlearn.core.Translation;
import uk.ignas.langlearn.core.TranslationDaoSqlite;

import java.io.File;

public class QuestionnaireActivity extends Activity {
    private Button translationButton;
    private Button knownWordButton;
    private Button unknownWordButton;
    private Button addWordButton;
    private Button deleteWordButton;
    private Button exportDataButton;
    private EditText exportDataFileEditText;
    private TextView correctAnswerView;
    private TextView questionLabel;

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
        deleteWordButton = (Button) findViewById(R.id.delete_word_button);
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
                publishNextWord();
                enableTranslationAndNotSubmittionButtons(true);
                questionnaire.markKnown(currentWord);
            }
        });

        unknownWordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publishNextWord();
                enableTranslationAndNotSubmittionButtons(true);
                questionnaire.markUnknown(currentWord);
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
                                questionnaire.delete(currentWord);
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

        addWordButton.setOnClickListener(new View.OnClickListener() {

            private String errorMessage;
            private String foreignWordToRemember = "";
            private String nativeWordToRemember = "";

            @Override
            public void onClick(View arg0) {
                AlertDialog.Builder b = new AlertDialog.Builder(QuestionnaireActivity.this);
                LayoutInflater i = getLayoutInflater();
                View inflatedDialogView = i.inflate(R.layout.add_word_dialog, null);

                final TextView errorTextView = (TextView) inflatedDialogView.findViewById(R.id.error_textview);
                if (errorMessage != null) {
                    errorTextView.setText(errorMessage);
                    errorMessage = null;
                }

                final EditText foreignWordEditText = (EditText) inflatedDialogView.findViewById(R.id.foreign_language_word_edittext);
                final EditText nativeWordEditText = (EditText) inflatedDialogView.findViewById(R.id.native_language_word_edittext);
                foreignWordEditText.setText(foreignWordToRemember);
                nativeWordEditText.setText(nativeWordToRemember);
                final AlertDialog dialog = b
                        .setView(inflatedDialogView)
                        .setPositiveButton(R.string.add_word, new DialogInterface.OnClickListener(){

                            @Override
                            public void onClick(DialogInterface d, int which) {
                                String foreignWord = foreignWordEditText.getText().toString();
                                String nativeWord = nativeWordEditText.getText().toString();
                                boolean inserted = questionnaire.insert(new Translation(nativeWord, foreignWord));
                                if (!inserted) {
                                    errorMessage = "Duplicate Record";
                                    foreignWordToRemember = foreignWord;
                                    nativeWordToRemember = nativeWord;
                                    addWordButton.callOnClick();
                                } else {
                                    foreignWordToRemember = "";
                                    nativeWordToRemember = "";
                                }
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                foreignWordToRemember = "";
                                nativeWordToRemember = "";
                                dialog.dismiss();
                            }
                        })

                        .create();
                dialog.setMessage("Add new word");

                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        foreignWordToRemember = "";
                        nativeWordToRemember = "";
                    }
                });
                dialog.show();
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

    private void publishNextWord() {
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
