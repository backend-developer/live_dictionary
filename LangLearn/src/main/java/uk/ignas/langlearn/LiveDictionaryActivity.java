package uk.ignas.langlearn;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.google.common.base.Supplier;
import uk.ignas.langlearn.core.*;

import java.io.File;

public class LiveDictionaryActivity extends Activity implements OnModifyDictionaryClickListener.ModifyDictionaryListener, Supplier<Translation> {
    private static final Translation EMPTY_TRANSLATION = new Translation(new ForeignWord(""), new NativeWord(""));
    public static final String TAG = LiveDictionaryActivity.class.getName();

    private Button showTranslationButton;
    private Button markTranslationAsEasyButton;
    private Button markTranslationAsDifficultButton;
    private Button addTranslationButton;
    private Button updateTranslationButton;
    private Button deleteTranslationButton;
    private Button importDataButton;
    private Button exportDataButton;
    private EditText importDataFileEditText;
    private EditText exportDataFileEditText;
    private TextView correctAnswerView;
    private TextView questionLabel;

    private volatile Translation currentTranslation = EMPTY_TRANSLATION;
    private Dictionary dictionary;
    private TranslationDaoSqlite dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire);

        correctAnswerView = (TextView) findViewById(R.id.correct_answer);
        questionLabel = (TextView) findViewById(R.id.question_label);

        showTranslationButton = (Button) findViewById(R.id.show_translation_button);
        markTranslationAsEasyButton = (Button) findViewById(R.id.submit_translation_as_easy_button);
        markTranslationAsDifficultButton = (Button) findViewById(R.id.submit_translation_as_difficult_button);
        addTranslationButton = (Button) findViewById(R.id.add_translation_button);
        updateTranslationButton = (Button) findViewById(R.id.update_translation_button);
        deleteTranslationButton = (Button) findViewById(R.id.delete_translation_button);
        importDataButton = (Button) findViewById(R.id.import_data_button);
        exportDataButton = (Button) findViewById(R.id.export_data_button);

        importDataFileEditText = (EditText) findViewById(R.id.import_data_path_textedit);
        exportDataFileEditText = (EditText) findViewById(R.id.export_data_path_textedit);
    }

    @Override
    protected void onResume() {
        super.onResume();

        File externalStoragePublicDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        dao = new TranslationDaoSqlite(LiveDictionaryActivity.this);
        final DataImporterExporter dataImporterExporter = new DataImporterExporter(dao);

        try {
            dictionary = new Dictionary(dao);
        }catch (Exception e){
            Log.e(TAG, "critical error ", e);
            showErrorDialogAndExitActivity(e.getMessage());
        }

        final File defaultImportFile = new File(externalStoragePublicDirectory, "SpanishEnglishTranslations.txt");
        File defaultExportFile = new File(externalStoragePublicDirectory, "ExportedByUserRequest.txt");

        importDataFileEditText.setText(defaultImportFile.getAbsolutePath());
        exportDataFileEditText.setText(defaultExportFile.getAbsolutePath());

        publishNextTranslation();
        showTranslationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTranslation();
            }
        });

        markTranslationAsEasyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publishNextTranslation();
                dictionary.mark(currentTranslation, Difficulty.EASY);
            }
        });

        markTranslationAsDifficultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publishNextTranslation();
                dictionary.mark(currentTranslation, Difficulty.DIFFICULT);
            }
        });

        deleteTranslationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(LiveDictionaryActivity.this)
                        .setMessage("Delete this translation?")
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dictionary.delete(currentTranslation);
                                publishNextTranslation();
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

        View.OnClickListener onAddTranslationListener = OnModifyDictionaryClickListener.onInsertingTranslation(this);
        addTranslationButton.setOnClickListener(onAddTranslationListener);
        View.OnClickListener onUpdateTranslationListener = OnModifyDictionaryClickListener.onUpdatingTranslation(this, this);
        updateTranslationButton.setOnClickListener(onUpdateTranslationListener);

        importDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    dataImporterExporter.importFromFile(importDataFileEditText.getText().toString());
                } catch (RuntimeException e) {
                    showErrorDialogAndContinue(e.getMessage());
                }
                dictionary.reloadData();
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
        markTranslationAsEasyButton.setEnabled(isSubmittionPhase);
        markTranslationAsDifficultButton.setEnabled(isSubmittionPhase);
    }

    private void publishNextTranslation() {
        try {
            currentTranslation = dictionary.getRandomTranslation();
        } catch (LiveDictionaryException e) {
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_actions, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void createTranslation(Translation translation) {
        dictionary.insert(translation);
    }

    @Override
    public void updateTranslation(Translation translation) {
        dictionary.update(translation);
        currentTranslation = translation;
        showTranslation();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action item click
        switch (item.getItemId()) {
            case R.id.add_translation_button:
                View.OnClickListener onAddTranslationListener = OnModifyDictionaryClickListener.onInsertingTranslation(this);
                onAddTranslationListener.onClick(null);
                return true;
            case R.id.update_translation_button:
                View.OnClickListener onUpdateTranslationListener = OnModifyDictionaryClickListener.onUpdatingTranslation(this, this);
                onUpdateTranslationListener.onClick(null);
                return true;
            case R.id.delete_translation_button:
                new AlertDialog.Builder(LiveDictionaryActivity.this)
                        .setMessage("Delete this translation?")
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dictionary.delete(currentTranslation);
                                publishNextTranslation();
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Translation get() {
        return currentTranslation;
    }
}
