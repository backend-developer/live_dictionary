package uk.ignas.livedictionary;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import uk.ignas.livedictionary.core.*;
import uk.ignas.livedictionary.core.answer.AnswerDao;
import uk.ignas.livedictionary.core.label.Label;
import uk.ignas.livedictionary.core.label.LabelDao;
import uk.ignas.livedictionary.core.Labeler;
import uk.ignas.livedictionary.core.util.DatabaseFacade;

import java.util.ArrayList;
import java.util.List;

public class LabellingActivity extends Activity {
    private static final String TAG = LabellingActivity.class.getName();

    private TranslationDao dao;
    private DaoObjectsFetcher fetcher;
    private GuiError guiError;
    private Labeler labeler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_labelling);

        guiError = new GuiError(this);
        try {
            DatabaseFacade database = new DatabaseFacade(LabellingActivity.this);
            LabelDao labelDao = new LabelDao(database);
            AnswerDao answerDao = new AnswerDao(database);
            this.dao = new TranslationDao(labelDao, database, answerDao);
            fetcher = new DaoObjectsFetcher(labelDao, answerDao);
            labeler = new Labeler(this.dao, fetcher, labelDao);
        } catch (Exception e) {
            Log.e(TAG, "critical error ", e);
            guiError.showErrorDialogAndExitActivity(e);
        }
        final ListView listview = (ListView) findViewById(R.id.listview);

        List<Translation> labelledTranslations = new ArrayList<>(labeler.getLabelled(Label.A));
        final StableArrayAdapter adapter = new StableArrayAdapter(this, labelledTranslations);
        listview.setAdapter(adapter);
    }

    private class StableArrayAdapter extends ArrayAdapter<Translation> {

        private final List<Translation> translations;

        public StableArrayAdapter(Context context, List<Translation> translations) {
            super(context, 0, translations);
            this.translations = translations;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Translation translation = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_translation, parent, false);
            }
            TextView translationTextView = (TextView) convertView.findViewById(R.id.translation);
            translationTextView.setText(translation.getNativeWord().get() + " - " + translation.getForeignWord().get());

            TextView deleteLabelButton = (Button) convertView.findViewById(R.id.delete_label);
            deleteLabelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    labeler.removeLabel(translation, Label.A);
                    translations.remove(translation);
                    notifyDataSetChanged();
                    view.setAlpha(1);
                }
            });
            return convertView;
        }
    }
}