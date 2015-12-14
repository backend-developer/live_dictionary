package uk.ignas.langlearn;

import android.app.Activity;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = QuestionnaireModule.class)
public abstract class QuestionnaireComponent {
    public abstract void inject(QuestionnaireActivity activity);
    public void inject(Activity activity) {
        if (activity instanceof QuestionnaireActivity) {
            inject((QuestionnaireActivity) activity);
        }
    }

    public static final class Initializer {
        public static QuestionnaireComponent init() {
            return DaggerQuestionnaireComponent.builder().build();
        }
    }
}
