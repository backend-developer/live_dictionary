package uk.ignas.langlearn;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
    private static final String TAG = LiveDictionaryActivity.class.getName();
    private static final Translation EMPTY_TRANSLATION = new Translation(new ForeignWord(""), new NativeWord(""));
    private static final int PICK_IMPORTFILE_RESULT_CODE = 1;
    private static final int PICK_EXPORTFILE_RESULT_CODE = 2;

    private Button showTranslationButton;
    private Button markTranslationAsEasyButton;
    private Button markTranslationAsDifficultButton;
    private TextView correctAnswerView;
    private TextView questionLabel;

    private volatile Translation currentTranslation = EMPTY_TRANSLATION;
    private Dictionary dictionary;
    private TranslationDaoSqlite dao;
    private DataImporterExporter dataImporterExporter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire);

        correctAnswerView = (TextView) findViewById(R.id.correct_answer);
        questionLabel = (TextView) findViewById(R.id.question_label);

        showTranslationButton = (Button) findViewById(R.id.show_translation_button);
        markTranslationAsEasyButton = (Button) findViewById(R.id.submit_translation_as_easy_button);
        markTranslationAsDifficultButton = (Button) findViewById(R.id.submit_translation_as_difficult_button);
    }

    @Override
    protected void onResume() {
        super.onResume();

        dao = new TranslationDaoSqlite(LiveDictionaryActivity.this);
        dataImporterExporter = new DataImporterExporter(dao);

        try {
            dictionary = new Dictionary(dao);
        }catch (Exception e){
            Log.e(TAG, "critical error ", e);
            showErrorDialogAndExitActivity(e.getMessage());
        }

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
            case R.id.import_data_button:
                Intent intentToImport = new Intent(Intent.ACTION_GET_CONTENT);
                intentToImport.setType("file/*");
                startActivityForResult(intentToImport, PICK_IMPORTFILE_RESULT_CODE);
                return true;
            case R.id.export_data_button:
                Intent intentToExport = new Intent(Intent.ACTION_GET_CONTENT);
                intentToExport.setType("file/*");
                startActivityForResult(intentToExport, PICK_EXPORTFILE_RESULT_CODE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case PICK_IMPORTFILE_RESULT_CODE:
                if(resultCode==RESULT_OK){
                    String filePath = data.getData().getPath();
                    try {
                        dataImporterExporter.importFromFile(filePath);
                    } catch (RuntimeException e) {
                        showErrorDialogAndContinue(e.getMessage());
                    }
                    dictionary.reloadData();
                }
                break;
            case PICK_EXPORTFILE_RESULT_CODE:
                if(resultCode==RESULT_OK){
                    String filePath = data.getData().getPath();
                    try {
                        dataImporterExporter.export(filePath);
                    } catch (RuntimeException e) {
                        showErrorDialogAndContinue(e.getMessage());
                    }
                    dictionary.reloadData();
                }
        }
    }

    @Override
    public Translation get() {
        return currentTranslation;
    }
}
