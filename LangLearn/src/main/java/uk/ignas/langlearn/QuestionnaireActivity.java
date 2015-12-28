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

public class QuestionnaireActivity extends Activity implements OnModifyDictionaryClickListener.ModifyDictionaryListener, Supplier<Translation> {
    private static final Translation EMPTY_TRANSLATION = new Translation(new ForeignWord(""), new NativeWord(""));

    private Button showTranslationButton;
    private Button knownWordButton;
    private Button unknownWordButton;
    private Button addWordButton;
    private Button updateWordButton;
    private Button deleteWordButton;
    private Button importDataButton;
    private Button exportDataButton;
    private EditText importDataFileEditText;
    private EditText exportDataFileEditText;
    private TextView correctAnswerView;
    private TextView questionLabel;

    private volatile Translation currentTranslation = EMPTY_TRANSLATION;
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
        final DataImporterExporter dataImporterExporter = new DataImporterExporter(dao);

        questionnaire = new Questionnaire(dao);

        correctAnswerView = (TextView) findViewById(R.id.correct_answer);
        questionLabel = (TextView) findViewById(R.id.question_label);

        showTranslationButton = (Button) findViewById(R.id.show_translation_button);
        knownWordButton = (Button) findViewById(R.id.known_word_submision_button);
        unknownWordButton = (Button) findViewById(R.id.unknown_word_submision_button);
        addWordButton = (Button) findViewById(R.id.add_word_button);
        updateWordButton = (Button) findViewById(R.id.update_word_button);
        deleteWordButton = (Button) findViewById(R.id.delete_word_button);
        importDataButton = (Button) findViewById(R.id.import_data_button);
        exportDataButton = (Button) findViewById(R.id.export_data_button);

        importDataFileEditText = (EditText) findViewById(R.id.import_data_path_textedit);
        exportDataFileEditText = (EditText) findViewById(R.id.export_data_path_textedit);

        final File defaultImportFile = new File(externalStoragePublicDirectory, "SpanishWords.txt");
        File defaultExportFile = new File(externalStoragePublicDirectory, "ExportedByUserRequest.txt");

        importDataFileEditText.setText(defaultImportFile.getAbsolutePath());
        exportDataFileEditText.setText(defaultExportFile.getAbsolutePath());

        publishNextWord();
        showTranslationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTranslation();
            }
        });

        knownWordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publishNextWord();
                questionnaire.mark(currentTranslation, Difficulty.EASY);
            }
        });

        unknownWordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publishNextWord();
                questionnaire.mark(currentTranslation, Difficulty.DIFFICULT);
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

        View.OnClickListener onAddWordListener = OnModifyDictionaryClickListener.onInsertingWord(this);
        addWordButton.setOnClickListener(onAddWordListener);
        View.OnClickListener onUpdateWordListener = OnModifyDictionaryClickListener.onUpdatingWord(this, this);
        updateWordButton.setOnClickListener(onUpdateWordListener);

        importDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    dataImporterExporter.importFromFile(importDataFileEditText.getText().toString());
                } catch (RuntimeException e) {
                    showErrorDialogAndContinue(e.getMessage());
                }
                questionnaire.reloadData();
            }
        });

        exportDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    dataImporterExporter.export(exportDataFileEditText.getText().toString());
                } catch (RuntimeException e) {
                    showErrorDialogAndContinue(e.getMessage());
                }
            }
        });
    }

    private void showTranslation() {
        questionLabel.setText(currentTranslation.getNativeWord().get());
        correctAnswerView.setText(currentTranslation.getForeignWord().get());
        enableTranslationAndNotSubmittionButtons(false);
    }

    private void enableTranslationAndNotSubmittionButtons(boolean isTranslationPhase) {
        final boolean isSubmittionPhase = !isTranslationPhase;
        showTranslationButton.setEnabled(isTranslationPhase);
        knownWordButton.setEnabled(isSubmittionPhase);
        unknownWordButton.setEnabled(isSubmittionPhase);
    }

    private void publishNextWord() {
        try {
            currentTranslation = questionnaire.getRandomTranslation();
        } catch (QuestionnaireException e) {
            showErrorDialogAndContinue(e.getMessage());
            currentTranslation = EMPTY_TRANSLATION;
        } catch (RuntimeException e) {
            showErrorDialogAndExitActivity(e.getMessage());
        }
        enableTranslationAndNotSubmittionButtons(true);
        askUserToTranslate();
    }

    private void showErrorDialogAndExitActivity(String message) {
        showErrorDialog(message, true);
    }

    private void showErrorDialogAndContinue(String message) {
        showErrorDialog(message, false);
    }

    private void showErrorDialog(String message, final boolean shouldExitActivity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Live Dictionary")
                .setMessage("Error occured:" + message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (shouldExitActivity) {
                            finish();
                        }
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void askUserToTranslate() {
        questionLabel.setText(currentTranslation.getNativeWord().get());
        correctAnswerView.setText("");
    }

    @Override
    public void createTranslation(Translation translation) {
        questionnaire.insert(translation);
    }

    @Override
    public void updateTranslation(Translation translation) {
        questionnaire.update(translation);
        currentTranslation = translation;
        showTranslation();
    }

    @Override
    public Translation get() {
        return currentTranslation;
    }
}
