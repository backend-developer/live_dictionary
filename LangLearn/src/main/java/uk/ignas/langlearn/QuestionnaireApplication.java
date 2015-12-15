package uk.ignas.langlearn;

import android.app.Application;

public class QuestionnaireApplication extends Application {

    public void onCreate() {
        super.onCreate();
        System.setProperty("dexmaker.dexcache", getApplicationContext().getCacheDir().getAbsolutePath());
    }
}
