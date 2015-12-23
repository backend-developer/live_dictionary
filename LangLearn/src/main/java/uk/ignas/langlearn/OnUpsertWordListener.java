package uk.ignas.langlearn;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import uk.ignas.langlearn.core.Questionnaire;
import uk.ignas.langlearn.core.Translation;

/**
 * Created by ignas on 12/23/15.
 */
class OnUpsertWordListener implements View.OnClickListener {
    private String errorMessage;
    private Activity context;
    private Questionnaire questionnaire;
    private Integer id;
    private final String initialForeignWordToShow;
    private final String initialNativeWordToShow;
    private String foreignWordToRemember = "";
    private String nativeWordToRemember = "";
    private DictionaryActivity dictionaryActivity;

    public enum DictionaryActivity {INSERTING_WORD, UPDATING_WORD}

    public static OnUpsertWordListener onInsertingWord(Activity context, Questionnaire questionnaire) {
        return new OnUpsertWordListener(context, questionnaire, DictionaryActivity.INSERTING_WORD);
    }

    public static OnUpsertWordListener onUpdatingWord(Activity context, Questionnaire questionnaire, int id, String initialForeignWordToShow, String initialNativeWordToShow) {
        return new OnUpsertWordListener(context, questionnaire, id, initialForeignWordToShow, initialNativeWordToShow, DictionaryActivity.UPDATING_WORD);
    }

    public OnUpsertWordListener(Activity context, Questionnaire questionnaire, DictionaryActivity dictionaryActivity) {
        this(context, questionnaire, null, "", "", dictionaryActivity);
    }

    public OnUpsertWordListener(Activity context, Questionnaire questionnaire, Integer id, String initialForeignWordToShow, String initialNativeWordToShow, DictionaryActivity dictionaryActivity) {
        this.context = context;
        this.questionnaire = questionnaire;
        this.id = id;
        this.initialForeignWordToShow = initialForeignWordToShow;
        this.initialNativeWordToShow = initialNativeWordToShow;
        foreignWordToRemember = initialForeignWordToShow;
        nativeWordToRemember = initialNativeWordToShow;
        this.dictionaryActivity = dictionaryActivity;
    }

    @Override
    public void onClick(View arg0) {
        AlertDialog.Builder b = new AlertDialog.Builder(context);
        LayoutInflater i = context.getLayoutInflater();
        View inflatedDialogView = i.inflate(R.layout.word_translation_dialog, null);

        final TextView errorTextView = (TextView) inflatedDialogView.findViewById(R.id.error_textview);
        if (errorMessage != null) {
            errorTextView.setText(errorMessage);
            errorMessage = null;
        }

        final EditText foreignWordEditText = (EditText) inflatedDialogView.findViewById(R.id.foreign_language_word_edittext);
        final EditText nativeWordEditText = (EditText) inflatedDialogView.findViewById(R.id.native_language_word_edittext);
        foreignWordEditText.setText(initialForeignWordToShow);
        nativeWordEditText.setText(initialNativeWordToShow);
        final AlertDialog dialog = b
                .setView(inflatedDialogView)
                .setPositiveButton(R.string.add_word, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface d, int which) {
                        String foreignWord = foreignWordEditText.getText().toString();
                        String nativeWord = nativeWordEditText.getText().toString();
                        switch (dictionaryActivity) {
                            case INSERTING_WORD:
                                questionnaire.insert(new Translation(nativeWord, foreignWord));
                                break;
                            case UPDATING_WORD:
                                questionnaire.update(new Translation(id, nativeWord, foreignWord));
                                break;
                        }
                        foreignWordToRemember = initialForeignWordToShow;
                        nativeWordToRemember = initialNativeWordToShow;
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })

                .create();
        dialog.setMessage("Add new word");

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                foreignWordToRemember = initialForeignWordToShow;
                nativeWordToRemember = initialNativeWordToShow;
            }
        });
        dialog.show();
    }
}
