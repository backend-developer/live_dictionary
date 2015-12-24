package uk.ignas.langlearn.core;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import static com.google.common.collect.Iterables.getOnlyElement;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.*;
import static uk.ignas.langlearn.core.LiveDictionaryDsl.countPercentageOfRetrievedWordsHadExpectedPattern;
import static uk.ignas.langlearn.core.LiveDictionaryDsl.retrieveWordsNTimes;

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

    private Translation createForeignToNativeTranslation(String foreignWord, String nativeWord) {
        return new Translation(new ForeignWord(foreignWord), new NativeWord(nativeWord));
    }

    @Test
    public void shouldNotCrashWhenThereAreFewWords() {
        TranslationDao dao = new TranslationDaoStub();
        dao.insertSingle(createForeignToNativeTranslation("word", "translation"));

        Questionnaire questionnaire = new Questionnaire(dao);
        Translation translation = questionnaire.getRandomTranslation();
        assertThat(translation.getForeignWord().get(), is(equalTo("word")));
    }

    @Test
    public void shouldPersistUnknownWords() {
        TranslationDao dao = new TranslationDaoStub();
        dao.insertSingle(createForeignToNativeTranslation("word", "translation"));
        Translation translation = getOnlyElement(dao.getAllTranslations().keySet());
        Questionnaire questionnaire = new Questionnaire(dao);

        questionnaire.markUnknown(translation);

        assertThat(dao.getAllTranslations().get(translation), is(equalTo(Difficulty.HARD)));
    }

    @Test
    public void shouldGetNewest100QuestionsWith80PercentProbability() {
        TranslationDao dao = new TranslationDaoStub();
        dao.insert(getNTranslationsStartingWith(100, "Other"));
        dao.insert(getNTranslationsStartingWith(100, "LastQ"));
        Questionnaire questionnaire = new Questionnaire(dao);

        final List<Translation> retrievedWords = retrieveWordsNTimes(questionnaire, 1000);

        int percentage = countPercentageOfRetrievedWordsHadExpectedPattern(retrievedWords, "LastQ");
        assertThat(percentage, allOf(greaterThan(75), lessThan(85)));
    }

    @Test
    public void shouldHandle100Questions() {
        for (int i = 0; i < 100; i++) {
            TranslationDao dao = new TranslationDaoStub();
            dao.insert(getNTranslationsStartingWith(100, "Any"));
            Questionnaire questionnaire = new Questionnaire(dao);

            String retrievedWord = questionnaire.getRandomTranslation().getForeignWord().get();

            assertThat(retrievedWord, containsString("Any"));
        }
    }

    @Test
    public void afterFinding20UnknownWordsShouldNeverAskForOthers() {
        TranslationDao dao = new TranslationDaoStub();
        dao.insert(getNTranslationsStartingWith(100, "Other"));
        dao.insert(getNTranslationsStartingWith(20, "UnknownWord"));

        Questionnaire questionnaire = new Questionnaire(dao);

        for (Translation t: new HashSet<>(dao.getAllTranslations().keySet())) {
            if (t.getForeignWord().get().contains("UnknownWord")) {
                questionnaire.markUnknown(t);
            }
        }
        final List<Translation> retrievedWords = retrieveWordsNTimes(questionnaire, 100);

        int percentage = countPercentageOfRetrievedWordsHadExpectedPattern(retrievedWords, "UnknownWord");
        assertThat(percentage, is(equalTo(100)));
    }

    @Test
    public void unknownWordsShouldBeAskedEvery20thTime() {
        TranslationDao dao = new TranslationDaoStub();
        dao.insert(getNTranslationsStartingWith(100, "Other"));
        dao.insert(getNTranslationsStartingWith(10, "UnknownWord"));
        Questionnaire questionnaire = new Questionnaire(dao);

        for (Translation t: new HashSet<>(dao.getAllTranslations().keySet())) {
            if (t.getForeignWord().get().contains("UnknownWord")) {
                questionnaire.markUnknown(t);
            }
        }
        final List<Translation> retrievedWords = retrieveWordsNTimes(questionnaire, 1000);

        int percentage = countPercentageOfRetrievedWordsHadExpectedPattern(retrievedWords, "UnknownWord");
        assertThat(percentage, allOf(greaterThan(45), lessThan(55)));
    }

    @Test
    public void unknownWordsShouldBeAskedEvery20thTimeEvenIfTheyWerePassedInitially() {
        TranslationDao dao = new TranslationDaoStub();
        dao.insert(getNTranslationsStartingWith(100, "Other"));
        dao.insert(getNTranslationsStartingWith(10, "UnknownWord"));
        for (Translation t: new HashSet<>(dao.getAllTranslations().keySet())) {
            if (t.getForeignWord().get().contains("UnknownWord")) {
                dao.update(t.getId(), t.getForeignWord(), t.getNativeWord(), Difficulty.HARD);
            }
        }
        Questionnaire questionnaire = new Questionnaire(dao);

        final List<Translation> retrievedWords = retrieveWordsNTimes(questionnaire, 1000);

        int percentage = countPercentageOfRetrievedWordsHadExpectedPattern(retrievedWords, "UnknownWord");
        assertThat(percentage, allOf(greaterThan(45), lessThan(55)));
    }

    @Test
    public void shouldInsertWord() {
        TranslationDao dao = new TranslationDaoStub();
        Questionnaire questionnaire = new Questionnaire(dao);

        questionnaire.insert(createForeignToNativeTranslation("duplicate", "dup_translation"));

        assertThat(dao.getAllTranslations().size(), is(equalTo(1)));
    }

    @Test
    public void shouldNotInsertDuplicates() {
        TranslationDao dao = new TranslationDaoStub();
        Questionnaire questionnaire = new Questionnaire(dao);

        questionnaire.insert(createForeignToNativeTranslation("duplicate", "dup_translation"));
        questionnaire.insert(createForeignToNativeTranslation("duplicate", "dup_translation"));

        assertThat(dao.getAllTranslations().size(), is(equalTo(1)));
    }

    @Test
    public void shouldDeleteWord() {
        TranslationDao dao = new TranslationDaoStub();
        Translation word = createForeignToNativeTranslation("word", "la palabra");
        dao.insertSingle(word);
        Questionnaire questionnaire = new Questionnaire(dao);

        questionnaire.delete(word);

        assertThat(dao.getAllTranslations().keySet(), not(hasItem(word)));
    }

    @Test
    public void shouldNotMarkDifficultyForRecordsWithoutId() {
        TranslationDao dao = new TranslationDaoStub();
        Questionnaire questionnaire = new Questionnaire(dao);

        boolean isUpdated = questionnaire.markUnknown(createForeignToNativeTranslation("duplicate", "dup_translation"));

        assertThat(isUpdated, is(false));
    }

    @Test
    public void shouldNotMarkDifficultyForRecordsNotInDb() {
        TranslationDao dao = new TranslationDaoStub();
        Questionnaire questionnaire = new Questionnaire(dao);
        int nonexistentId = 8949861;

        boolean isUpdated = questionnaire.markUnknown(new Translation(nonexistentId, new ForeignWord("duplicate"), new NativeWord("dup_translation")));

        assertThat(isUpdated, is(false));
    }

    @Test
    public void shouldMarkDifficulty() {
        TranslationDao dao = new TranslationDaoStub();
        Questionnaire questionnaire = new Questionnaire(dao);
        dao.insert(singletonList(createForeignToNativeTranslation("word", "la palabra")));
        Translation translation = dao.getAllTranslations().keySet().iterator().next();

        boolean isUpdated = questionnaire.markUnknown(translation);

        assertThat(isUpdated, is(true));
    }

    @Test
    public void shouldUpdateQuestion() {
        TranslationDao dao = new TranslationDaoStub();
        dao.insert(singletonList(createForeignToNativeTranslation("word", "la palabra")));
        Questionnaire questionnaire = new Questionnaire(dao);
        Translation translation = dao.getAllTranslations().keySet().iterator().next();

        boolean isUpdated = questionnaire.update(new Translation(translation.getId(), new ForeignWord("modified word"), new NativeWord("la palabra cambiada")));

        assertThat(isUpdated, is(true));
        Translation modifiedWord = dao.getAllTranslations().keySet().iterator().next();
        assertThat(modifiedWord.getForeignWord().get(), is(equalTo("modified word")));
        assertThat(modifiedWord.getNativeWord().get(), is(equalTo("la palabra cambiada")));
    }

    public List<Translation> getNTranslationsStartingWith(int n, String prefix) {
        List<Translation> translations = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            translations.add(new Translation(new ForeignWord(prefix + getUniqueInt()), new NativeWord("t" + getUniqueInt())));
        }
        return translations;
    }

    private int getUniqueInt() {
        return uniqueSequence++;
    }
}
