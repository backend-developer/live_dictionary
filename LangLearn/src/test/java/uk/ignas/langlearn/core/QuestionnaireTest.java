package uk.ignas.langlearn.core;

import org.junit.Test;
import uk.ignas.langlearn.core.db.TranslationDao;
import uk.ignas.langlearn.core.db.TranslationDaoStub;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.*;

public class QuestionnaireTest {

    private static int uniqueSequence = 0;

    @Test
    public void shouldThrowWhenGeneratingQuestionIfQuestionBaseIsEmpty() {
        TranslationDao dao = new TranslationDaoStub();

        Questionnaire questionnaire = new Questionnaire(dao);
        try {
            questionnaire.getRandomTranslation();
            fail();
        } catch (QuestionnaireException e) {
            assertTrue(e.getMessage().contains("no questions found"));
        }
    }

    @Test
    public void shouldNotCrashWhenThereAreFewWords() {
        LinkedHashMap<Translation, Difficulty> words = new LinkedHashMap<>();
        words.put(new Translation("word", "translation"), Difficulty.EASY);
        TranslationDao dao = new TranslationDaoStub();
        dao.insert(new ArrayList<>(words.keySet()));

        Questionnaire questionnaire = new Questionnaire(dao);
        Translation translation = questionnaire.getRandomTranslation();
        assertThat(translation.getOriginalWord(), is(equalTo("word")));
    }

    @Test
    public void shouldPersistUnknownWords() {
        LinkedHashMap<Translation, Difficulty> words = new LinkedHashMap<>();
        Translation translation = new Translation("word", "translation");
        words.put(translation, Difficulty.EASY);
        TranslationDao dao = new TranslationDaoStub();
        dao.insert(new ArrayList<>(words.keySet()));
        Questionnaire questionnaire = new Questionnaire(dao);

        questionnaire.markUnknown(translation);

        assertThat(dao.getAllTranslations().get(translation), is(equalTo(Difficulty.HARD)));
    }

    @Test
    public void shouldGetNewest100QuestionsWith80PercentProbability() {
        LinkedHashMap<Translation, Difficulty> words = get200QuestionsOutOfWhichNewestNStartsWith(100, "LastQ");
        TranslationDao dao = new TranslationDaoStub();
        dao.insert(new ArrayList<>(words.keySet()));
        Questionnaire questionnaire = new Questionnaire(dao);

        final List<String> retrievedWords = retrieveWordsNTimes(questionnaire, 1000);

        int percentage = countPercentageOfExpectedWordsRetrieved("LastQ", retrievedWords);
        assertThat(percentage, allOf(greaterThan(75), lessThan(85)));
    }

    @Test
    public void shouldHandle100Questions() {
        for (int i = 0; i < 100; i++) {
            TranslationDao dao = new TranslationDaoStub();
            LinkedHashMap<Translation, Difficulty> words = getNQuestionsStartingWith(100, "Any");
            dao.insert(new ArrayList<>(words.keySet()));
            Questionnaire questionnaire = new Questionnaire(dao);

            String retrievedWord = questionnaire.getRandomTranslation().getOriginalWord();

            assertThat(retrievedWord, containsString("Any"));
        }
    }

    @Test
    public void afterFinding20UnknownWordsShouldNeverAskForOthers() {
        LinkedHashMap<Translation, Difficulty> allWords = getNQuestionsStartingWith(100, "Other");
        LinkedHashMap<Translation, Difficulty> unknownWords = getNQuestionsStartingWith(20, "UnknownWord");
        allWords.putAll(unknownWords);
        TranslationDao dao = new TranslationDaoStub();
        dao.insert(new ArrayList<>(allWords.keySet()));
        dao.insert(new ArrayList<>(unknownWords.keySet()));
        Questionnaire questionnaire = new Questionnaire(dao);

        for (Translation t: unknownWords.keySet()) {
            questionnaire.markUnknown(t);
        }
        final List<String> retrievedWords = retrieveWordsNTimes(questionnaire, 100);

        int percentage = countPercentageOfExpectedWordsRetrieved("UnknownWord", retrievedWords);
        assertThat(percentage, is(equalTo(100)));
    }

    @Test
    public void unknownWordsShouldBeAskedEvery20thTime() {
        LinkedHashMap<Translation, Difficulty> allWords = getNQuestionsStartingWith(100, "Other");
        LinkedHashMap<Translation, Difficulty> unknownWords = getNQuestionsStartingWith(10, "UnknownWord");
        allWords.putAll(unknownWords);
        TranslationDao dao = new TranslationDaoStub();
        dao.insert(new ArrayList<>(allWords.keySet()));
        dao.insert(new ArrayList<>(unknownWords.keySet()));
        Questionnaire questionnaire = new Questionnaire(dao);

        for (Translation t: unknownWords.keySet()) {
            questionnaire.markUnknown(t);
        }
        final List<String> retrievedWords = retrieveWordsNTimes(questionnaire, 1000);

        int percentage = countPercentageOfExpectedWordsRetrieved("UnknownWord", retrievedWords);
        assertThat(percentage, allOf(greaterThan(45), lessThan(55)));
    }

    @Test
    public void unknownWordsShouldBeAskedEvery20thTimeEvenIfTheyWerePassedInitially() {
        LinkedHashMap<Translation, Difficulty> allWords = getNQuestionsStartingWith(100, "Other");
        LinkedHashMap<Translation, Difficulty> unknownWords = getNQuestionsStartingWith(10, "UnknownWord", Difficulty.HARD);
        allWords.putAll(unknownWords);
        TranslationDao dao = new TranslationDaoStub();
        dao.insert(new ArrayList<>(allWords.keySet()));
        dao.insert(new ArrayList<>(unknownWords.keySet()));
        for (Translation t: unknownWords.keySet()) {
            dao.update(t.getOriginalWord(), t.getTranslatedWord(), unknownWords.get(t));
        }
        Questionnaire questionnaire = new Questionnaire(dao);

        final List<String> retrievedWords = retrieveWordsNTimes(questionnaire, 1000);


        int percentage = countPercentageOfExpectedWordsRetrieved("UnknownWord", retrievedWords);
        assertThat(percentage, allOf(greaterThan(45), lessThan(55)));
    }

    private List<String> retrieveWordsNTimes(Questionnaire questionnaire, int timesToExecute) {
        final List<String> retrievedWords = new ArrayList<>();
        for (int i = 0; i < timesToExecute; i++) {
            retrievedWords.add(questionnaire.getRandomTranslation().getOriginalWord());
        }
        return retrievedWords;
    }

    private int countPercentageOfExpectedWordsRetrieved(String expectedPattern, List<String> retrievedWords) {
        int timesInterested = 0;
        for (String w: retrievedWords) {
            if (w.contains(expectedPattern)) {
                timesInterested++;
            }
        }
        int timesTotal = retrievedWords.size();
        return calculatePercentage(timesInterested, timesTotal);
    }

    private int calculatePercentage(int timesInterested, int timesTotal) {
        if (timesTotal > 100) {
            int relationTo100 = timesTotal / 100;
            return timesInterested / relationTo100;
        } else {
            int relationTo100 = 100 / timesTotal;
            return  timesInterested * relationTo100;
        }
    }

    public LinkedHashMap<Translation, Difficulty> get200QuestionsOutOfWhichNewestNStartsWith(int n, String prefixForFirst100Questions) {
        LinkedHashMap<Translation, Difficulty> translations = new LinkedHashMap<>();
        //order is omportant
        translations.putAll(getNQuestionsStartingWith(200-n, "Other"));
        translations.putAll(getNQuestionsStartingWith(n, prefixForFirst100Questions));
        return translations;
    }

    public LinkedHashMap<Translation, Difficulty> getNQuestionsStartingWith(int n, String prefix) {
        return getNQuestionsStartingWith(n, prefix, Difficulty.EASY);
    }

    public LinkedHashMap<Translation, Difficulty> getNQuestionsStartingWith(int n, String prefix, Difficulty difficulty) {
        LinkedHashMap<Translation, Difficulty> translations = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) {
            translations.put(new Translation(prefix + getUniqueInt(), "t" + getUniqueInt()), difficulty);
        }
        return translations;
    }

    private int getUniqueInt() {
        return uniqueSequence++;
    }
}
