package uk.ignas.livedictionary;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import uk.ignas.livedictionary.core.*;

public class LiveDictionaryActivity extends Activity implements ModifyDictionaryDialog.ModifyDictionaryListener {
    private static final String TAG = LiveDictionaryActivity.class.getName();
    private static final Translation EMPTY_TRANSLATION = new Translation(new ForeignWord(""), new NativeWord(""));
    private static final int PICK_IMPORT_FILE_RESULT_CODE = 1;
    private static final int PICK_EXPORT_FILE_RESULT_CODE = 2;

    private Button showTranslationButton;
    private Button markTranslationAsEasyButton;
    private Button markTranslationAsDifficultButton;
    private TextView correctAnswerView;
    private TextView questionLabel;

    private volatile Translation currentTranslation = EMPTY_TRANSLATION;
    private Dictionary dictionary;
    private TranslationDao dao;
    private GuiError guiError;

    private ImportExportActivity importExportActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire);

        injectDependenciesAndSetupThem();
    }

    private void injectDependenciesAndSetupThem() {
        correctAnswerView = (TextView) findViewById(R.id.correct_answer);
        questionLabel = (TextView) findViewById(R.id.question_label);

        showTranslationButton = (Button) findViewById(R.id.show_translation_button);
        markTranslationAsEasyButton = (Button) findViewById(R.id.submit_translation_as_easy_button);
        markTranslationAsDifficultButton = (Button) findViewById(R.id.submit_translation_as_difficult_button);

        guiError = new GuiError(this);
        try {
            dao = new TranslationDaoSqlite(LiveDictionaryActivity.this);
            dictionary = new Dictionary(dao);
            importExportActivity = new ImportExportActivity(new DataImporterExporter(dao), dictionary, guiError);

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
                    dictionary.mark(currentTranslation, Answer.CORRECT);
                    publishNextTranslation();
                }
            });

            markTranslationAsDifficultButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dictionary.mark(currentTranslation, Answer.INCORRECT);
                    publishNextTranslation();
                }
            });
        }catch (Exception e){
            Log.e(TAG, "critical error ", e);
            guiError.showErrorDialogAndExitActivity(e.getMessage());
        }
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
            guiError.showErrorDialogAndContinue(e.getMessage());
            currentTranslation = EMPTY_TRANSLATION;
        } catch (RuntimeException e) {
            guiError.showErrorDialogAndExitActivity(e.getMessage());
        }
        enableTranslationAndNotSubmittionButtons(true);
        askUserToTranslate();
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
        switch (item.getItemId()) {
            case R.id.add_translation_button:
                ModifyDictionaryDialog
                        .onInsertingTranslation(this)
                        .show();
                return true;
            case R.id.update_translation_button:
                ModifyDictionaryDialog
                        .onUpdatingTranslation(this, currentTranslation)
                        .show();
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
            case R.id.import_data_button:
                importExportActivity.startActivity(this, PICK_IMPORT_FILE_RESULT_CODE);
                return true;
            case R.id.export_data_button:
                importExportActivity.startActivity(this, PICK_EXPORT_FILE_RESULT_CODE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case PICK_IMPORT_FILE_RESULT_CODE:
                importExportActivity.handleImportResult(resultCode, data);
                break;
            case PICK_EXPORT_FILE_RESULT_CODE:
                importExportActivity.handleExportResult(resultCode, data);
                break;
        }
    }
}
