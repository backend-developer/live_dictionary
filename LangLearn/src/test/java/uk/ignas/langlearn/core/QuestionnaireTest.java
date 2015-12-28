package uk.ignas.langlearn.core;

import org.junit.Test;
import uk.ignas.langlearn.testutils.LiveDictionaryDsl;
import uk.ignas.langlearn.testutils.TranslationDaoStub;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.google.common.collect.Iterables.getOnlyElement;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.*;
import static uk.ignas.langlearn.testutils.LiveDictionaryDsl.countPercentageOfRetrievedNativeWordsHadExpectedPattern;
import static uk.ignas.langlearn.testutils.LiveDictionaryDsl.retrieveTranslationsNTimes;

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
    public void shouldNotCrashWhenThereAreFewTranslations() {
        TranslationDao dao = new TranslationDaoStub();
        dao.insertSingle(createForeignToNativeTranslation("palabra", "word"));

        Questionnaire questionnaire = new Questionnaire(dao);
        Translation translation = questionnaire.getRandomTranslation();
        assertThat(translation.getForeignWord().get(), is(equalTo("palabra")));
    }

    @Test
    public void shouldSynchronizeWithDbOnDemand() {
        TranslationDao dao = new TranslationDaoStub();
        Questionnaire questionnaire = new Questionnaire(dao);
        dao.insertSingle(createForeignToNativeTranslation("la palabra", "word"));

        questionnaire.reloadData();

        Translation translation = questionnaire.getRandomTranslation();
        assertThat(translation.getForeignWord().get(), is(equalTo("la palabra")));
    }

    @Test
    public void shouldPersistDifficultTranslations() {
        TranslationDao dao = new TranslationDaoStub();
        dao.insertSingle(createForeignToNativeTranslation("palabra", "word"));
        Translation translation = getOnlyElement(dao.getAllTranslations().keySet());
        Questionnaire questionnaire = new Questionnaire(dao);

        questionnaire.mark(translation, Difficulty.DIFFICULT);

        assertThat(dao.getAllTranslations().get(translation), is(equalTo(Difficulty.DIFFICULT)));
    }

    @Test
    public void shouldGetNewest100QuestionsWith80PercentProbability() {
        TranslationDao dao = new TranslationDaoStub();
        dao.insert(getNTranslationsWithNativeWordStartingWith(100, "Other"));
        dao.insert(getNTranslationsWithNativeWordStartingWith(100, "LastQ"));
        Questionnaire questionnaire = new Questionnaire(dao);

        final List<Translation> retrievedTranslations = retrieveTranslationsNTimes(questionnaire, 1000);

        int percentage = countPercentageOfRetrievedNativeWordsHadExpectedPattern(retrievedTranslations, "LastQ");
        assertThat(percentage, allOf(greaterThan(75), lessThan(85)));
    }

    @Test
    public void shouldHandle100Questions() {
        for (int i = 0; i < 100; i++) {
            TranslationDao dao = new TranslationDaoStub();
            dao.insert(getNTranslationsWithNativeWordStartingWith(100, "Any"));
            Questionnaire questionnaire = new Questionnaire(dao);

            List<Translation> translations = LiveDictionaryDsl.retrieveTranslationsNTimes(questionnaire, 100);

            int percentage = LiveDictionaryDsl.countPercentageOfRetrievedNativeWordsHadExpectedPattern(translations, "Any");
            assertThat(percentage, is(100));
        }
    }

    @Test
    public void afterFinding20DifficultTranslationsShouldNeverAskForOthers() {
        TranslationDao dao = new TranslationDaoStub();
        dao.insert(getNTranslationsWithNativeWordStartingWith(100, "Other"));
        dao.insert(getNTranslationsWithNativeWordStartingWith(20, "DifficultWord"));

        Questionnaire questionnaire = new Questionnaire(dao);

        for (Translation t: new HashSet<>(dao.getAllTranslations().keySet())) {
            if (t.getNativeWord().get().contains("DifficultWord")) {
                questionnaire.mark(t, Difficulty.DIFFICULT);
            }
        }
        final List<Translation> retrievedTranslations = retrieveTranslationsNTimes(questionnaire, 100);

        int percentage = countPercentageOfRetrievedNativeWordsHadExpectedPattern(retrievedTranslations, "DifficultWord");
        assertThat(percentage, is(equalTo(100)));
    }

    @Test
    public void diffucultTranslationsWhichAreAlreadyBecameEasyShouldStopBeingAskedEvery20thTime() {
        TranslationDao dao = new TranslationDaoStub();
        dao.insert(getNTranslationsWithNativeWordStartingWith(80, "Other"));
        dao.insert(getNTranslationsWithNativeWordStartingWith(10, "DifficultWord"));
        dao.insert(getNTranslationsWithNativeWordStartingWith(10, "WasDifficultButNowEasyWord"));

        Questionnaire questionnaire = new Questionnaire(dao);

        for (Translation t: new HashSet<>(dao.getAllTranslations().keySet())) {
            if (t.getNativeWord().get().contains("DifficultWord")) {
                questionnaire.mark(t, Difficulty.DIFFICULT);
            }
            if (t.getNativeWord().get().contains("WasDifficultButNowEasyWord")) {
                questionnaire.mark(t, Difficulty.DIFFICULT);
                questionnaire.mark(t, Difficulty.EASY);
            }
        }
        final List<Translation> retrievedTranslations = retrieveTranslationsNTimes(questionnaire, 1000);

        int percentage = countPercentageOfRetrievedNativeWordsHadExpectedPattern(retrievedTranslations, "DifficultWord");
        assertThat(percentage, allOf(greaterThan(45), lessThan(55)));
    }

    @Test
    public void difficultTranslationsShouldBeAskedEvery20thTime() {
        TranslationDao dao = new TranslationDaoStub();
        dao.insert(getNTranslationsWithNativeWordStartingWith(100, "Other"));
        dao.insert(getNTranslationsWithNativeWordStartingWith(10, "DifficultWord"));
        Questionnaire questionnaire = new Questionnaire(dao);

        for (Translation t: new HashSet<>(dao.getAllTranslations().keySet())) {
            if (t.getNativeWord().get().contains("DifficultWord")) {
                questionnaire.mark(t, Difficulty.DIFFICULT);
            }
        }
        final List<Translation> retrievedTranslations = retrieveTranslationsNTimes(questionnaire, 1000);

        int percentage = countPercentageOfRetrievedNativeWordsHadExpectedPattern(retrievedTranslations, "DifficultWord");
        assertThat(percentage, allOf(greaterThan(45), lessThan(55)));
    }



    @Test
    public void difficultTranslationsShouldBeAskedEvery20thTimeEvenIfTheyWerePassedInitially() {
        TranslationDao dao = new TranslationDaoStub();
        dao.insert(getNTranslationsWithNativeWordStartingWith(100, "Other"));
        dao.insert(getNTranslationsWithNativeWordStartingWith(10, "DifficultWord"));
        for (Translation t: new HashSet<>(dao.getAllTranslations().keySet())) {
            if (t.getNativeWord().get().contains("DifficultWord")) {
                dao.update(t.getId(), t.getForeignWord(), t.getNativeWord(), Difficulty.DIFFICULT);
            }
        }
        Questionnaire questionnaire = new Questionnaire(dao);

        final List<Translation> retrievedTranslations = retrieveTranslationsNTimes(questionnaire, 1000);

        int percentage = countPercentageOfRetrievedNativeWordsHadExpectedPattern(retrievedTranslations, "DifficultWord");
        assertThat(percentage, allOf(greaterThan(45), lessThan(55)));
    }

    @Test
    public void shouldInsertTranslation() {
        TranslationDao dao = new TranslationDaoStub();
        Questionnaire questionnaire = new Questionnaire(dao);

        questionnaire.insert(createForeignToNativeTranslation("la palabra", "word"));

        assertThat(dao.getAllTranslations().size(), is(equalTo(1)));
        assertThat(questionnaire.getRandomTranslation().getForeignWord().get(), is("la palabra"));
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
    public void shouldDeleteTranslation() {
        TranslationDao dao = new TranslationDaoStub();
        Translation translation = createForeignToNativeTranslation("word", "la palabra");
        dao.insertSingle(translation);
        Questionnaire questionnaire = new Questionnaire(dao);

        questionnaire.delete(translation);

        assertThat(dao.getAllTranslations().keySet(), not(hasItem(translation)));
        try {
            questionnaire.getRandomTranslation();
            fail();
        } catch (QuestionnaireException e) {
            assertThat(e.getMessage(), containsString("no questions found")) ;
        }
    }

    @Test
    public void shouldNotMarkDifficultyForRecordsWithoutId() {
        TranslationDao dao = new TranslationDaoStub();
        Questionnaire questionnaire = new Questionnaire(dao);

        boolean isUpdated = questionnaire.mark(createForeignToNativeTranslation("duplicate", "dup_translation"), Difficulty.DIFFICULT);

        assertThat(isUpdated, is(false));
    }

    @Test
    public void shouldNotMarkDifficultyForRecordsNotInDb() {
        TranslationDao dao = new TranslationDaoStub();
        Questionnaire questionnaire = new Questionnaire(dao);
        int nonexistentId = 8949861;

        boolean isUpdated = questionnaire.mark(new Translation(nonexistentId, new ForeignWord("la duplicado"), new NativeWord("duplication")), Difficulty.DIFFICULT);

        assertThat(isUpdated, is(false));
    }

    @Test
    public void shouldMarkDifficulty() {
        TranslationDao dao = new TranslationDaoStub();
        Questionnaire questionnaire = new Questionnaire(dao);
        dao.insert(singletonList(createForeignToNativeTranslation("word", "la palabra")));
        Translation translation = dao.getAllTranslations().keySet().iterator().next();

        boolean isUpdated = questionnaire.mark(translation, Difficulty.DIFFICULT);

        assertThat(isUpdated, is(true));
    }

    @Test
    public void shouldUpdateQuestion() {
        TranslationDao dao = new TranslationDaoStub();
        dao.insert(singletonList(createForeignToNativeTranslation("la palabra", "word")));
        Questionnaire questionnaire = new Questionnaire(dao);
        Translation translation = dao.getAllTranslations().keySet().iterator().next();

        boolean isUpdated = questionnaire.update(new Translation(translation.getId(), new ForeignWord("la palabra cambiada"), new NativeWord("modified word")));

        assertThat(isUpdated, is(true));
        Translation modifiedWord = dao.getAllTranslations().keySet().iterator().next();
        assertThat(modifiedWord.getForeignWord().get(), is(equalTo("la palabra cambiada")));
        assertThat(modifiedWord.getNativeWord().get(), is(equalTo("modified word")));
        assertThat(questionnaire.getRandomTranslation().getNativeWord().get(), is("modified word"));
    }

    public List<Translation> getNTranslationsWithNativeWordStartingWith(int n, String prefix) {
        List<Translation> translations = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            translations.add(new Translation(new ForeignWord("desconocido" + getUniqueInt()), new NativeWord(prefix + getUniqueInt())));
        }
        return translations;
    }

    private int getUniqueInt() {
        return uniqueSequence++;
    }
}
