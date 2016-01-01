package uk.ignas.langlearn.core;

import org.junit.Ignore;
import org.junit.Test;
import uk.ignas.langlearn.testutils.PromotionPeriod;

import java.util.ArrayList;
import java.util.Calendar;
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
    private static final Difficulty ANY_DIFFICULTY = Difficulty.EASY;

    @Test
    public void brandNewTranslationShouldBeReminded() {
        Clock clock = mock(Clock.class);
        Reminder reminder = new Reminder(clock);
        TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, new ArrayList<DifficultyAtTime>());

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void brandNewTranslationShouldBeRemindedOnceAgainInLevel1PromotionPeriod() {
        Clock clock = mock(Clock.class);
        when(clock.getTime()).thenReturn(LEVEL_1.begin());
        Reminder reminder = new Reminder(clock);
        TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, asList(
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.begin())
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void mistakenTranslationShouldBeReminded() {
        Clock clock = mock(Clock.class);
        when(clock.getTime()).thenReturn(LEVEL_1.begin());
        Reminder reminder = new Reminder(clock);
        TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, asList(
                new DifficultyAtTime(Difficulty.DIFFICULT, LEVEL_1.begin())
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void mistakenTranslationShouldBeRemindedUpTo3TimesInLeve1PromotionPeriod() {
        Clock clock = mock(Clock.class);
        when(clock.getTime()).thenReturn(LEVEL_1.begin());
        Reminder reminder = new Reminder(clock);
        TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, asList(
                new DifficultyAtTime(Difficulty.DIFFICULT, LEVEL_1.begin()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.begin()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.begin())
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void mistakenTranslationShouldNotBeAsked4thTimeInLevel1PromotionPeriod() {
        Clock clock = mock(Clock.class);
        when(clock.getTime()).thenReturn(LEVEL_1.begin());
        Reminder reminder = new Reminder(clock);
        TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, asList(
                new DifficultyAtTime(Difficulty.DIFFICULT, LEVEL_1.begin()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.begin()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.begin()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.begin())
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(false));
    }

    @Test
    public void mistakenTranslationShouldReturnWordBackToLevel1PromotionPeriod() {
        Clock clock = mock(Clock.class);
        when(clock.getTime()).thenReturn(LEVEL_2.begin());
        Reminder reminder = new Reminder(clock);
        TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, asList(
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.begin()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.begin()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_2.begin()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_2.begin()),
                new DifficultyAtTime(Difficulty.DIFFICULT, LEVEL_2.begin()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_2.begin()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_2.begin())

        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void mistakenTranslationShouldBeAskedAgainAfterLevel1PromotionPeriodHasPassedButLevel2NotYet() {
        Clock clock = mock(Clock.class);
        when(clock.getTime()).thenReturn(LEVEL_2.begin());
        Reminder reminder = new Reminder(clock);
        TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, asList(
                new DifficultyAtTime(Difficulty.DIFFICULT, LEVEL_1.begin()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.begin()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.begin()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.begin())
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void mistakenTranslationShouldBeAskedUpToTwoTimesAfterLevel1PromotionPeriodHasPassedButLevel2NotYet() {
        Clock clock = mock(Clock.class);
        when(clock.getTime()).thenReturn(LEVEL_2.end());
        Reminder reminder = new Reminder(clock);
        TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, asList(
                new DifficultyAtTime(Difficulty.DIFFICULT, LEVEL_1.begin()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.begin()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.begin()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.begin()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.end())
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void mistakenTranslationShouldNotBeAskedThirdTimeDuringLevel2PromotionPeriod() {
        Clock clock = mock(Clock.class);
        when(clock.getTime()).thenReturn(LEVEL_2.almostEnd());
        Reminder reminder = new Reminder(clock);
        TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, asList(
                new DifficultyAtTime(Difficulty.DIFFICULT, LEVEL_1.begin()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.begin()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.begin()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.begin()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.end()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.end())
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(false));
    }

    @Test
    public void brandNewTranslationShouldNotBeAskedMoreThanTwiceInLevel1PromotionPeriod() {
        Clock clock = mock(Clock.class);
        Reminder reminder = new Reminder(clock);
        when(clock.getTime()).thenReturn(LEVEL_1.almostEnd());
        TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, asList(
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.begin()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.begin())
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(false));
    }

    @Test
    public void brandNewTranslationShouldBeAskedMoreThanTwiceIfAskedLessRarelyThanItIsPromotionPeriodDuringLevel1PromotionPeriod() {
        Clock clock = mock(Clock.class);
        Reminder reminder = new Reminder(clock);
        when(clock.getTime()).thenReturn(LEVEL_1.end());
        TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, asList(
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.begin()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.end())
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void brandNewTranslationShouldContinueToBeAskedMoreThanTwiceIfContinuesBeingAskedLessRarelyThanItIsPromotionPeriodDuringLevel1PromotionPeriod() {
        Clock clock = mock(Clock.class);
        Reminder reminder = new Reminder(clock);
        Date PromotionPeriodLevel1PassedTwice = createDateOffsetedByHours(End.LEVEL_1 * 2);
        Date promotionPeriodLevel1PasswedThreeTimes = createDateOffsetedByHours(End.LEVEL_1 * 3);
        when(clock.getTime()).thenReturn(promotionPeriodLevel1PasswedThreeTimes);
        TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, asList(
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.begin()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.end()),
                new DifficultyAtTime(Difficulty.EASY, PromotionPeriodLevel1PassedTwice),
                new DifficultyAtTime(Difficulty.EASY, promotionPeriodLevel1PasswedThreeTimes)
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void translationEvaluationDuringLevel2PromotionPeriodShouldNotAssumeThatLevel1PromotionPeriodPassedImmediately() {
        Clock clock = mock(Clock.class);
        Reminder reminder = new Reminder(clock);

        Date level1PassedAnd2PeriodPassedTwice = createDateOffsetedByHours(LEVEL_1.duraionHours() + LEVEL_2.duraionHours() * 2);
        when(clock.getTime()).thenReturn(level1PassedAnd2PeriodPassedTwice);
        TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, asList(
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.begin()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.end()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.end()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_2.end()),
                new DifficultyAtTime(Difficulty.EASY, level1PassedAnd2PeriodPassedTwice)
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void translationShouldContinueToBeAskedIfIsBeingAskedLessRarelyThanItIsPromotionPeriodDuringLevel2PromotionPeriod() {
        Clock clock = mock(Clock.class);
        Reminder reminder = new Reminder(clock);
        Date level1PassedAnd2PeriodPassedTwice = createDateOffsetedByHours(LEVEL_1.duraionHours() + LEVEL_2.duraionHours() * 2);
        when(clock.getTime()).thenReturn(level1PassedAnd2PeriodPassedTwice);
        TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, asList(
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.begin()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.begin()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.end()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_2.end()),
                new DifficultyAtTime(Difficulty.EASY, level1PassedAnd2PeriodPassedTwice)
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void mistakenTranslationShouldContinueToBeAskedIfIsBeingAskedLessRarelyThanItIsPromotionPeriodDuringLevel2PromotionPeriod() {
        Clock clock = mock(Clock.class);
        Reminder reminder = new Reminder(clock);
        Date level1PassedAnd2PeriodPassedTwice = createDateOffsetedByHours(LEVEL_1.duraionHours() + LEVEL_2.duraionHours() * 2);
        when(clock.getTime()).thenReturn(level1PassedAnd2PeriodPassedTwice);
        TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, asList(
                new DifficultyAtTime(Difficulty.DIFFICULT, LEVEL_1.begin()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.begin()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.begin()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.begin()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.end()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_2.end()),
                new DifficultyAtTime(Difficulty.EASY, level1PassedAnd2PeriodPassedTwice)
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void translationShouldBeRemindedAfterLevel1PromotionPeriodHasPassed() {
        Clock clock = mock(Clock.class);
        Reminder reminder = new Reminder(clock);
        when(clock.getTime()).thenReturn(LEVEL_1.end());
        TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, asList(
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.begin()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.begin())
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void translationShouldBeRemindedUpToTwoTimesInLevel2PromotionPeriod() {
        Clock clock = mock(Clock.class);
        Reminder reminder = new Reminder(clock);
        when(clock.getTime()).thenReturn(LEVEL_2.almostEnd());
        TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, asList(
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.begin()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.begin()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.end())
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void translationShouldNotBeRemindedThirdTimeDuringLevel2PromotionPeriod() {
        Clock clock = mock(Clock.class);
        Reminder reminder = new Reminder(clock);
        when(clock.getTime()).thenReturn(LEVEL_2.almostEnd());
        TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, asList(
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.begin()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.begin()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.end()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.end())
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(false));
    }

    @Test
    public void translationShouldNotBeRemindedSecondTimeDuringLevel3PromotionPeriod() {
        Clock clock = mock(Clock.class);
        Reminder reminder = new Reminder(clock);
        when(clock.getTime()).thenReturn(LEVEL_3.almostEnd());
        TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, asList(
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.begin()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.begin()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.end()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.end()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_2.end())
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(false));
    }

    @Test
    public void translationShouldBeRemindedAfterLevel3PromotionPeriodPasses() {
        Clock clock = mock(Clock.class);
        Reminder reminder = new Reminder(clock);
        when(clock.getTime()).thenReturn(LEVEL_3.end());
        TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, asList(
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.begin()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.begin()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.end()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.end()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_2.end())
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void translationShouldNotBeRemindedSecondTimeDuringLevel4PromotionPeriod() {
        Clock clock = mock(Clock.class);
        Reminder reminder = new Reminder(clock);
        when(clock.getTime()).thenReturn(LEVEL_4.almostEnd());
        TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, asList(
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.begin()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.begin()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.end()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.end()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_2.end()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_3.end())
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(false));
    }

    @Test
    public void translationShouldBeRemindedAfterLevel4PromotionPeriodPasses() {
        Clock clock = mock(Clock.class);
        Reminder reminder = new Reminder(clock);
        when(clock.getTime()).thenReturn(LEVEL_4.end());
        TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, asList(
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.begin()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.begin()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.end()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_1.end()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_2.end()),
                new DifficultyAtTime(Difficulty.EASY, LEVEL_3.end())
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    @Ignore
    public void translationShouldNotBeRemindedSecondTimeDuringPromotionPeriodOfLevel3To8() {
        List<PromotionPeriod> levelsRequiringSingleAnswer = asList(LEVEL_3, LEVEL_4, LEVEL_5, LEVEL_6, LEVEL_7, LEVEL_8);
        for (int i = 0; i < levelsRequiringSingleAnswer.size(); i++) {
            PromotionPeriod levelToTest = levelsRequiringSingleAnswer.get(i);
            List<PromotionPeriod> levelsUpToTest = levelsRequiringSingleAnswer.subList(0, i);

            Clock clock = mock(Clock.class);
            Reminder reminder = new Reminder(clock);
            TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, newArrayList(
                    new DifficultyAtTime(Difficulty.EASY, LEVEL_1.begin()),
                    new DifficultyAtTime(Difficulty.EASY, LEVEL_1.begin()),
                    new DifficultyAtTime(Difficulty.EASY, LEVEL_1.end()),
                    new DifficultyAtTime(Difficulty.EASY, LEVEL_1.end()),
                    new DifficultyAtTime(Difficulty.EASY, LEVEL_2.end())
            ));
            for (PromotionPeriod levelUpToTest : levelsUpToTest) {
                metadata.getRecentDifficulty().add(
                        new DifficultyAtTime(Difficulty.EASY, levelUpToTest.end())
                );
            }

            when(clock.getTime()).thenReturn(levelToTest.almostEnd());

            boolean shouldRemind = reminder.shouldBeReminded(metadata);

            assertThat("failed at level: " + levelToTest, shouldRemind, is(false));
        }
    }

    @Test
    public void translationShouldBeRemindedAfterLevel4To8PromotionPeriodPasses() {
        List<PromotionPeriod> levelsRequiringSingleAnswer = asList(LEVEL_3, LEVEL_4, LEVEL_5, LEVEL_6, LEVEL_7, LEVEL_8);
        for (int i = 0; i < levelsRequiringSingleAnswer.size(); i++) {
            PromotionPeriod levelToTest = levelsRequiringSingleAnswer.get(i);
            List<PromotionPeriod> levelsUpToTest = levelsRequiringSingleAnswer.subList(0, i);

            Clock clock = mock(Clock.class);
            Reminder reminder = new Reminder(clock);
            when(clock.getTime()).thenReturn(levelToTest.end());
            TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, newArrayList(
                    new DifficultyAtTime(Difficulty.EASY, LEVEL_1.begin()),
                    new DifficultyAtTime(Difficulty.EASY, LEVEL_1.begin()),
                    new DifficultyAtTime(Difficulty.EASY, LEVEL_1.end()),
                    new DifficultyAtTime(Difficulty.EASY, LEVEL_1.end()),
                    new DifficultyAtTime(Difficulty.EASY, LEVEL_2.end())
            ));

            for (PromotionPeriod levelUpToTest : levelsUpToTest) {
                metadata.getRecentDifficulty().add(
                        new DifficultyAtTime(Difficulty.EASY, levelUpToTest.end())
                );
            }

            boolean shouldRemind = reminder.shouldBeReminded(metadata);

            assertThat("test failed on level: " + levelToTest, shouldRemind, is(true));
        }
    }

}