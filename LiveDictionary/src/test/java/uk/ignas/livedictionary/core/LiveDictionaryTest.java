package uk.ignas.livedictionary.core;

import com.google.common.collect.ArrayListMultimap;
import integration.SequentialSelectionStrategy;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import uk.ignas.livedictionary.core.answer.Answer;
import uk.ignas.livedictionary.core.answer.AnswerAtTime;
import uk.ignas.livedictionary.core.answer.AnswerDao;
import uk.ignas.livedictionary.core.answer.Feedback;
import uk.ignas.livedictionary.core.label.LabelDao;

import java.util.Date;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class LiveDictionaryTest {

    private Clock clock = new Clock();

    private TranslationDao translationDao = mock(TranslationDao.class);

    private LabelDao labelDao = mock(LabelDao.class);

    private AnswerDao answerDao = mock(AnswerDao.class);

    private DaoObjectsFetcher fetcher = new DaoObjectsFetcher(labelDao, answerDao);

    private Labeler labeler = new Labeler(translationDao, fetcher, labelDao);

    private Dictionary dictionary =
        new Dictionary(translationDao, answerDao, fetcher, labeler, clock, new SequentialSelectionStrategy());

    @Test
    public void shouldThrowIfFailedToLogAnswer() {
        int translationId = 8949861;
        when(answerDao.logAnswer(Mockito.anyInt(), Mockito.any(AnswerAtTime.class))).thenReturn(false);
        try {
            dictionary
                .mark(new Translation(translationId, new ForeignWord("la duplicado"), new NativeWord("duplication")),
                      Answer.INCORRECT);
            fail();
        } catch (IllegalArgumentException e) {

        }
    }

    @Test
    public void loggingAnswerShouldNotLogFeedback() {
        int translationId = 8949861;
        when(answerDao.logAnswer(Mockito.anyInt(), Mockito.any(AnswerAtTime.class))).thenReturn(true);
        ArgumentCaptor<AnswerAtTime> captor = ArgumentCaptor.forClass(AnswerAtTime.class);

        dictionary.mark(new Translation(translationId, new ForeignWord("la duplicado"), new NativeWord("duplication")),
                        Answer.INCORRECT);

        verify(answerDao).logAnswer(argThat(is(translationId)), captor.capture());
        assertThat(captor.getAllValues().size(), is(1));
        assertThat(captor.getAllValues().get(0).getFeedback(), nullValue());
    }

    @Test
    public void shouldThrowIfFailedToLogFeedback() {
        int translationId = 8949861;
        when(answerDao.logAnswer(Mockito.anyInt(), Mockito.any(AnswerAtTime.class))).thenReturn(false);
        try {
            dictionary.markAsAskedTooOften(
                new Translation(translationId, new ForeignWord("la duplicado"), new NativeWord("duplication")));
            fail();
        } catch (IllegalArgumentException e) {

        }
    }

    @Test
    public void givingFeedbackOnTranslationAsBeingGivenTooOftenShouldMarkTranslationCorrect() {
        int translationId = 8949861;
        when(answerDao.logAnswer(Mockito.anyInt(), Mockito.any(AnswerAtTime.class))).thenReturn(true);
        ArgumentCaptor<AnswerAtTime> captor = ArgumentCaptor.forClass(AnswerAtTime.class);

        dictionary.markAsAskedTooOften(
            new Translation(translationId, new ForeignWord("la duplicado"), new NativeWord("duplication")));

        verify(answerDao).logAnswer(argThat(is(translationId)), captor.capture());
        assertThat(captor.getAllValues().size(), is(1));
        assertThat(captor.getAllValues().get(0).getAnswer(), is(Answer.CORRECT));
    }

    @Test
    public void shouldAcceptFeedbackOnTranslationAsBeingGivenTooOften() {
        int translationId = 8949861;
        when(answerDao.logAnswer(Mockito.anyInt(), Mockito.any(AnswerAtTime.class))).thenReturn(true);
        ArgumentCaptor<AnswerAtTime> captor = ArgumentCaptor.forClass(AnswerAtTime.class);

        dictionary.markAsAskedTooOften(
            new Translation(translationId, new ForeignWord("la duplicado"), new NativeWord("duplication")));

        verify(answerDao).logAnswer(argThat(is(translationId)), captor.capture());
        assertThat(captor.getAllValues().size(), is(1));
        assertThat(captor.getAllValues().get(0).getFeedback(), is(Feedback.ASKED_TOO_OFTEN));
    }

    @Test
    public void shouldUpdate() {
        Translation translationWithoutId = new Translation(new ForeignWord("la palabra"), new NativeWord("a word"));
        int id = 5;
        Translation modifiedTranslation =
            new Translation(id, new ForeignWord("la palabra cambiado"), new NativeWord("a word"));
        when(translationDao.getAllTranslations()).thenReturn(newArrayList(new Translation(id, translationWithoutId)));
        when(answerDao.getAnswersLogByTranslationId()).thenReturn(ArrayListMultimap.<Integer, AnswerAtTime>create());

        boolean updated = dictionary.update(modifiedTranslation);

        assertThat(updated, is(true));
        verify(translationDao).updateAlongWithLabels(modifiedTranslation);
        verify(translationDao, never()).delete(Mockito.anyCollection());
    }

    @Test
    public void shouldNotDeleteOnIssues() {
        Translation translationWithoutId = new Translation(new ForeignWord("la palabra"), new NativeWord("a word"));
        int id = 5;
        Translation modifiedTranslation =
            new Translation(id, new ForeignWord("la palabra cambiado"), new NativeWord("a word"));
        when(translationDao.getAllTranslations()).thenReturn(newArrayList(new Translation(id, translationWithoutId)));
        when(translationDao.updateAlongWithLabels(any(Translation.class))).thenThrow(new RuntimeException("anyIssue"));
        when(answerDao.getAnswersLogByTranslationId()).thenReturn(ArrayListMultimap.<Integer, AnswerAtTime>create());

        try {
            dictionary.update(modifiedTranslation);
            fail();
        } catch (RuntimeException e) {

        }

        verify(translationDao, never()).delete(Mockito.anyCollection());
    }
}
