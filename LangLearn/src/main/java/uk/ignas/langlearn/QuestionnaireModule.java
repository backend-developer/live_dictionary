package uk.ignas.langlearn;

import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public class QuestionnaireModule {

    @Provides
    @Singleton
    QuestionsStorage provideQuestionsStorage() {
        return new QuestionsStorage();
    }
}
