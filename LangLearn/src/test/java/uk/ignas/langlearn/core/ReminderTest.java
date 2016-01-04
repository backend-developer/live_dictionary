package uk.ignas.langlearn.core;

import org.junit.Test;
import uk.ignas.langlearn.testutils.PromotionPeriod;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ignas.langlearn.testutils.PromotionPeriod.*;

public class ReminderTest {
    private static final Answer ANY_ANSWER = Answer.CORRECT;

    @Test
    public void brandNewTranslationShouldBeReminded() {
        Clock clock = mock(Clock.class);
        Reminder reminder = new Reminder(clock);
        TranslationMetadata metadata = new TranslationMetadata(new ArrayList<AnswerAtTime>());

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void brandNewTranslationShouldBeRemindedOnceAgainInLevel0PromotionPeriod() {
        Clock clock = mock(Clock.class);
        when(clock.getTime()).thenReturn(LEVEL_0.begin());
        Reminder reminder = new Reminder(clock);
        TranslationMetadata metadata = new TranslationMetadata(asList(
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin())
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void mistakenTranslationShouldBeReminded() {
        Clock clock = mock(Clock.class);
        when(clock.getTime()).thenReturn(LEVEL_0.begin());
        Reminder reminder = new Reminder(clock);
        TranslationMetadata metadata = new TranslationMetadata(asList(
                new AnswerAtTime(Answer.INCORRECT, LEVEL_0.begin())
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void mistakenTranslationShouldBeRemindedUpTo3TimesInLeve1PromotionPeriod() {
        Clock clock = mock(Clock.class);
        when(clock.getTime()).thenReturn(LEVEL_0.begin());
        Reminder reminder = new Reminder(clock);
        TranslationMetadata metadata = new TranslationMetadata(asList(
                new AnswerAtTime(Answer.INCORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin())
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void mistakenTranslationShouldNotBeAsked4thTimeInLevel0PromotionPeriod() {
        Clock clock = mock(Clock.class);
        when(clock.getTime()).thenReturn(LEVEL_0.begin());
        Reminder reminder = new Reminder(clock);
        TranslationMetadata metadata = new TranslationMetadata(asList(
                new AnswerAtTime(Answer.INCORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin())
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(false));
    }



    @Test
    public void mistakenTranslationShouldBeAskedAgainAfterLevel0PromotionPeriodHasPassedButLevel1NotYet() {
        Clock clock = mock(Clock.class);
        when(clock.getTime()).thenReturn(LEVEL_1.begin());
        Reminder reminder = new Reminder(clock);
        TranslationMetadata metadata = new TranslationMetadata(asList(
                new AnswerAtTime(Answer.INCORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin())
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void mistakenTranslationShouldBeAskedUpToTwoTimesAfterLevel0PromotionPeriodHasPassedButLevel1NotYet() {
        Clock clock = mock(Clock.class);
        when(clock.getTime()).thenReturn(LEVEL_1.end());
        Reminder reminder = new Reminder(clock);
        TranslationMetadata metadata = new TranslationMetadata(asList(
                new AnswerAtTime(Answer.INCORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_1.begin())
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void mistakenTranslationShouldNotBeAskedThirdTimeDuringLevel1PromotionPeriod() {
        Clock clock = mock(Clock.class);
        when(clock.getTime()).thenReturn(LEVEL_1.almostEnd());
        Reminder reminder = new Reminder(clock);
        TranslationMetadata metadata = new TranslationMetadata(asList(
                new AnswerAtTime(Answer.INCORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_1.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_1.begin())
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(false));
    }

    @Test
    public void brandNewTranslationShouldNotBeAskedMoreThanTwiceInLevel0PromotionPeriod() {
        Clock clock = mock(Clock.class);
        Reminder reminder = new Reminder(clock);
        when(clock.getTime()).thenReturn(LEVEL_0.almostEnd());
        TranslationMetadata metadata = new TranslationMetadata(asList(
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin())
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(false));
    }

    @Test
    public void brandNewTranslationShouldBeAskedMoreThanTwiceIfAskedLessRarelyThanItIsPromotionPeriodDuringLevel0PromotionPeriod() {
        Clock clock = mock(Clock.class);
        Reminder reminder = new Reminder(clock);
        when(clock.getTime()).thenReturn(LEVEL_0.end());
        TranslationMetadata metadata = new TranslationMetadata(asList(
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.end())
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void brandNewTranslationShouldContinueToBeAskedMoreThanTwiceIfContinuesBeingAskedLessRarelyThanItIsPromotionPeriodDuringLevel0PromotionPeriod() {
        Clock clock = mock(Clock.class);
        Reminder reminder = new Reminder(clock);
        Date PromotionPeriodLevel0PassedTwice = createDateOffsetedByHours(End.LEVEL_0 * 2);
        Date promotionPeriodLevel0PasswedThreeTimes = createDateOffsetedByHours(End.LEVEL_0 * 3);
        when(clock.getTime()).thenReturn(promotionPeriodLevel0PasswedThreeTimes);
        TranslationMetadata metadata = new TranslationMetadata(asList(
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.end()),
                new AnswerAtTime(Answer.CORRECT, PromotionPeriodLevel0PassedTwice),
                new AnswerAtTime(Answer.CORRECT, promotionPeriodLevel0PasswedThreeTimes)
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void translationEvaluationDuringLevel1PromotionPeriodShouldNotAssumeThatLevel0PromotionPeriodPassedImmediately() {
        Clock clock = mock(Clock.class);
        Reminder reminder = new Reminder(clock);

        Date level0PassedAnd1PeriodPassedTwice = createDateOffsetedByHours(LEVEL_0.duraionHours() + LEVEL_1.duraionHours() * 2);
        when(clock.getTime()).thenReturn(level0PassedAnd1PeriodPassedTwice);
        TranslationMetadata metadata = new TranslationMetadata(asList(
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.end()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.end()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_1.end()),
                new AnswerAtTime(Answer.CORRECT, level0PassedAnd1PeriodPassedTwice)
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void translationShouldContinueToBeAskedIfIsBeingAskedLessRarelyThanItIsPromotionPeriodDuringLevel1PromotionPeriod() {
        Clock clock = mock(Clock.class);
        Reminder reminder = new Reminder(clock);
        Date level0PassedAnd1PeriodPassedTwice = createDateOffsetedByHours(LEVEL_0.duraionHours() + LEVEL_1.duraionHours() * 2);
        when(clock.getTime()).thenReturn(level0PassedAnd1PeriodPassedTwice);
        TranslationMetadata metadata = new TranslationMetadata(asList(
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_1.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_1.end()),
                new AnswerAtTime(Answer.CORRECT, level0PassedAnd1PeriodPassedTwice)
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void mistakenTranslationShouldContinueToBeAskedIfIsBeingAskedLessRarelyThanItIsPromotionPeriodDuringLevel1PromotionPeriod() {
        Clock clock = mock(Clock.class);
        Reminder reminder = new Reminder(clock);
        Date level0PassedAnd1PeriodPassedTwice = createDateOffsetedByHours(LEVEL_0.duraionHours() + LEVEL_1.duraionHours() * 2);
        when(clock.getTime()).thenReturn(level0PassedAnd1PeriodPassedTwice);
        TranslationMetadata metadata = new TranslationMetadata(asList(
                new AnswerAtTime(Answer.INCORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_1.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_1.end()),
                new AnswerAtTime(Answer.CORRECT, level0PassedAnd1PeriodPassedTwice)
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void translationShouldBeRemindedAfterLevel0PromotionPeriodHasPassed() {
        Clock clock = mock(Clock.class);
        Reminder reminder = new Reminder(clock);
        when(clock.getTime()).thenReturn(LEVEL_0.end());
        TranslationMetadata metadata = new TranslationMetadata(asList(
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin())
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void translationShouldBeRemindedUpToTwoTimesInLevel1PromotionPeriod() {
        Clock clock = mock(Clock.class);
        Reminder reminder = new Reminder(clock);
        when(clock.getTime()).thenReturn(LEVEL_1.almostEnd());
        TranslationMetadata metadata = new TranslationMetadata(asList(
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_1.begin())
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void translationShouldNotBeRemindedThirdTimeDuringLevel1PromotionPeriod() {
        Clock clock = mock(Clock.class);
        Reminder reminder = new Reminder(clock);
        when(clock.getTime()).thenReturn(LEVEL_1.almostEnd());
        TranslationMetadata metadata = new TranslationMetadata(asList(
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_1.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_1.begin())
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(false));
    }

    @Test
    public void translationShouldNotBeRemindedSecondTimeDuringLevel2PromotionPeriod() {
        Clock clock = mock(Clock.class);
        Reminder reminder = new Reminder(clock);
        when(clock.getTime()).thenReturn(LEVEL_2.almostEnd());
        TranslationMetadata metadata = new TranslationMetadata(asList(
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_1.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_1.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_2.begin())
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(false));
    }

    @Test
    public void translationShouldBeRemindedAfterLevel2PromotionPeriodPasses() {
        Clock clock = mock(Clock.class);
        Reminder reminder = new Reminder(clock);
        when(clock.getTime()).thenReturn(LEVEL_2.end());
        TranslationMetadata metadata = new TranslationMetadata(asList(
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_1.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_1.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_2.begin())
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void translationShouldNotBeRemindedSecondTimeDuringLevel3PromotionPeriod() {
        Clock clock = mock(Clock.class);
        Reminder reminder = new Reminder(clock);
        when(clock.getTime()).thenReturn(LEVEL_3.almostEnd());
        TranslationMetadata metadata = new TranslationMetadata(asList(
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_1.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_1.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_2.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_3.begin())
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(false));
    }

    @Test
    public void translationShouldBeRemindedAfterLevel3PromotionPeriodPasses() {
        Clock clock = mock(Clock.class);
        Reminder reminder = new Reminder(clock);
        when(clock.getTime()).thenReturn(LEVEL_3.end());
        TranslationMetadata metadata = new TranslationMetadata(asList(
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_1.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_1.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_2.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_3.begin())
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void translationShouldNotBeRemindedSecondTimeDuringPromotionPeriodOfLevel2To7() {
        List<PromotionPeriod> levelsRequiringSingleAnswer = asList(LEVEL_2, LEVEL_3, LEVEL_4, LEVEL_5, LEVEL_6, LEVEL_7);
        for (int i = 0; i < levelsRequiringSingleAnswer.size(); i++) {
            PromotionPeriod levelToTest = levelsRequiringSingleAnswer.get(i);
            List<PromotionPeriod> levelsUpToTest = levelsRequiringSingleAnswer.subList(0, i+1);

            Clock clock = mock(Clock.class);
            Reminder reminder = new Reminder(clock);
            TranslationMetadata metadata = new TranslationMetadata(newArrayList(
                    new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                    new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                    new AnswerAtTime(Answer.CORRECT, LEVEL_1.begin()),
                    new AnswerAtTime(Answer.CORRECT, LEVEL_1.begin())
            ));
            for (PromotionPeriod levelUpToTest : levelsUpToTest) {
                metadata.getRecentAnswers().add(
                        new AnswerAtTime(Answer.CORRECT, levelUpToTest.begin())
                );
            }

            when(clock.getTime()).thenReturn(levelToTest.almostEnd());

            boolean shouldRemind = reminder.shouldBeReminded(metadata);

            assertThat("failed at level: " + levelToTest, shouldRemind, is(false));
        }
    }

    @Test
    public void translationShouldBeRemindedAfterLevel2To7PromotionPeriodPasses() {
        List<PromotionPeriod> levelsRequiringSingleAnswer = asList(LEVEL_2, LEVEL_3, LEVEL_4, LEVEL_5, LEVEL_6, LEVEL_7);
        for (int i = 0; i < levelsRequiringSingleAnswer.size(); i++) {
            PromotionPeriod levelToTest = levelsRequiringSingleAnswer.get(i);
            List<PromotionPeriod> levelsUpToTest = levelsRequiringSingleAnswer.subList(0, i+1);

            Clock clock = mock(Clock.class);
            Reminder reminder = new Reminder(clock);
            when(clock.getTime()).thenReturn(levelToTest.end());
            TranslationMetadata metadata = new TranslationMetadata(newArrayList(
                    new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                    new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                    new AnswerAtTime(Answer.CORRECT, LEVEL_1.begin()),
                    new AnswerAtTime(Answer.CORRECT, LEVEL_1.begin())
            ));

            for (PromotionPeriod levelUpToTest : levelsUpToTest) {
                metadata.getRecentAnswers().add(
                        new AnswerAtTime(Answer.CORRECT, levelUpToTest.begin())
                );
            }

            boolean shouldRemind = reminder.shouldBeReminded(metadata);

            assertThat("test failed on level: " + levelToTest, shouldRemind, is(true));
        }
    }

    @Test
    public void translationShouldNotBeRemindedSecondTimeDuringPromotionPeriodOfLevelRightAfterEight() {
        Clock clock = mock(Clock.class);
        Reminder reminder = new Reminder(clock);
        when(clock.getTime()).thenReturn(LEVEL_OVER_7.by(1).almostEnd());
        TranslationMetadata metadata = new TranslationMetadata(asList(
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_1.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_1.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_2.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_3.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_4.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_5.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_6.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_7.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_OVER_7.by(1).begin())
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(false));
    }

    @Test
    public void translationShouldBeRemindedAfterLevelPromotionPeriodOfLevelRightAfter7Passes() {
        Clock clock = mock(Clock.class);
        Reminder reminder = new Reminder(clock);
        when(clock.getTime()).thenReturn(LEVEL_OVER_7.by(1).end());
        TranslationMetadata metadata = new TranslationMetadata(asList(
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_1.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_1.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_2.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_3.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_4.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_5.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_6.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_7.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_OVER_7.by(1).begin())
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void translationShouldNotBeRemindedSecondTimeDuringPromotionPeriodOfLevelsHigherThan7() {
        Clock clock = mock(Clock.class);
        Reminder reminder = new Reminder(clock);
        when(clock.getTime()).thenReturn(LEVEL_OVER_7.by(2).almostEnd());
        TranslationMetadata metadata = new TranslationMetadata(asList(
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_1.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_1.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_2.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_3.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_4.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_5.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_6.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_7.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_OVER_7.by(1).begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_OVER_7.by(2).begin())
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(false));
    }

    @Test
    public void translationShouldBeRemindedAfterLevelPromotionPeriodOfLevelHigherThan7Passes() {
        Clock clock = mock(Clock.class);
        Reminder reminder = new Reminder(clock);
        when(clock.getTime()).thenReturn(LEVEL_OVER_7.by(2).end());
        TranslationMetadata metadata = new TranslationMetadata(asList(
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_1.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_1.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_2.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_3.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_4.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_5.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_6.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_7.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_OVER_7.by(1).begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_OVER_7.by(2).begin())
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void mistakenTranslationShouldReturnWordBackToLevel0PromotionPeriod() {
        Clock clock = mock(Clock.class);
        when(clock.getTime()).thenReturn(LEVEL_4.begin());
        Reminder reminder = new Reminder(clock);
        TranslationMetadata metadata = new TranslationMetadata(asList(
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_0.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_1.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_1.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_2.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_3.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_4.begin()),
                new AnswerAtTime(Answer.INCORRECT, LEVEL_4.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_4.begin()),
                new AnswerAtTime(Answer.CORRECT, LEVEL_4.begin())
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }
}