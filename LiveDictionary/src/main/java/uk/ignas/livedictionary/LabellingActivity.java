package uk.ignas.livedictionary;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import uk.ignas.livedictionary.core.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class LabellingActivity extends Activity {
    private static final String TAG = LabellingActivity.class.getName();

    private TranslationDao dao;
    private GuiError guiError;
    private Labeler labeler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_labelling);


        guiError = new GuiError(this);
        try {
            dao = new TranslationDao(LabellingActivity.this);
            labeler = new Labeler(dao);
        } catch (Exception e) {
            Log.e(TAG, "critical error ", e);
            guiError.showErrorDialogAndExitActivity(e);
        }
        final ListView listview = (ListView) findViewById(R.id.listview);

        Collection<Translation> labelled = labeler.getLabelled();
        final ArrayList<String> list = new ArrayList<String>();
        for (Translation t: labelled) {
            list.add(t.toString());
        }
        final StableArrayAdapter adapter = new StableArrayAdapter(this,
                                                                  android.R.layout.simple_list_item_1, list);
        listview.setAdapter(adapter);

    }

    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }

}

