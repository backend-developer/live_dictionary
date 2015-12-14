package uk.ignas.langlearn;

import android.app.Application;

public class QuestionnaireApplication extends Application {

    private QuestionnaireComponent component;

    public void onCreate() {
        super.onCreate();
        System.setProperty("dexmaker.dexcache", getApplicationContext().getCacheDir().getAbsolutePath());
        component = DaggerQuestionnaireComponent.Initializer.init();
    }

    public QuestionnaireComponent getComponent() {
        return component;
    }

    public void setComponent(QuestionnaireComponent component) {
        this.component = component;
    }
}
