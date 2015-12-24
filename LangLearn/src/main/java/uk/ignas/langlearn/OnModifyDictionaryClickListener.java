package uk.ignas.langlearn;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import uk.ignas.langlearn.core.ForeignWord;
import uk.ignas.langlearn.core.NativeWord;
import uk.ignas.langlearn.core.Questionnaire;
import uk.ignas.langlearn.core.Translation;
import uk.ignas.langlearn.util.MutableObject;

class OnModifyDictionaryClickListener implements View.OnClickListener {

    public interface ModifyDictionaryListener {
        void createTranslation(Translation translation);
        void updateTranslation(Translation translation);
    }

    private String errorMessage;
    private Activity context;
    private Questionnaire questionnaire;
    private MutableObject<Translation> currentTranslationHolder;
    private DictionaryActivity dictionaryActivity;

    public enum DictionaryActivity {INSERTING_WORD, UPDATING_WORD}

    public static OnModifyDictionaryClickListener onInsertingWord(Activity context, Questionnaire questionnaire) {
        return new OnModifyDictionaryClickListener(context, questionnaire, DictionaryActivity.INSERTING_WORD);
    }

    public static OnModifyDictionaryClickListener onUpdatingWord(Activity context, Questionnaire questionnaire, MutableObject<Translation> currentTranslationHolder) {
        return new OnModifyDictionaryClickListener(context, questionnaire, currentTranslationHolder, DictionaryActivity.UPDATING_WORD);
    }

    private OnModifyDictionaryClickListener(Activity context, Questionnaire questionnaire, DictionaryActivity dictionaryActivity) {
        this(context, questionnaire, new MutableObject<>(new Translation(
                new ForeignWord(""),
                new NativeWord(""))), dictionaryActivity);
    }

    private OnModifyDictionaryClickListener(Activity context, Questionnaire questionnaire, MutableObject<Translation> currentTranslationHolder, DictionaryActivity dictionaryActivity) {
        this.context = context;
        this.questionnaire = questionnaire;
        this.currentTranslationHolder = currentTranslationHolder;
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
        final Translation currentTranslation = currentTranslationHolder.get();
        foreignWordEditText.setText(currentTranslation.getForeignWord().get());
        nativeWordEditText.setText(currentTranslation.getNativeWord().get());
        int okButtonResorce = dictionaryActivity == DictionaryActivity.INSERTING_WORD ? R.string.add_word : R.string.update_word;
        final AlertDialog dialog = b
                .setView(inflatedDialogView)
                .setPositiveButton(okButtonResorce, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface d, int which) {
                        String foreignWord = foreignWordEditText.getText().toString();
                        String nativeWord = nativeWordEditText.getText().toString();
                        switch (dictionaryActivity) {
                            case INSERTING_WORD:
                                ((ModifyDictionaryListener)context).createTranslation(new Translation(
                                        new ForeignWord(foreignWord),
                                        new NativeWord(nativeWord)));
                                break;
                            case UPDATING_WORD:
                                ((ModifyDictionaryListener)context).updateTranslation(new Translation(
                                        currentTranslation.getId(),
                                        new ForeignWord(foreignWord),
                                        new NativeWord(nativeWord)));
                                break;
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        String titleMessage = dictionaryActivity == DictionaryActivity.INSERTING_WORD ? "Add new word" : "Update a word";
        dialog.setMessage(titleMessage);
        dialog.show();
    }
}
