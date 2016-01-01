package uk.ignas.langlearn.core;

import org.junit.Test;
import uk.ignas.langlearn.testutils.PromotionPeriod;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

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

//    @Test
//    public void translationShouldNotBeRemindedSecondTimeDuringPromotionPeriodOfLevel5To7() {
//        for (int level = 3; level < 8; level++) {
//            Clock clock = mock(Clock.class);
//            Reminder reminder = new Reminder(clock);
//            TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, asList(
//                    new DifficultyAtTime(LEVEL_1.begin(), Difficulty.EASY),
//                    new DifficultyAtTime(LEVEL_1.begin(), Difficulty.EASY),
//                    new DifficultyAtTime(LEVEL_1.end(), Difficulty.EASY),
//                    new DifficultyAtTime(LEVEL_1.end(), Difficulty.EASY),
//                    new DifficultyAtTime(LEVEL_2.end(), Difficulty.EASY),
//                    new DifficultyAtTime(LEVEL_3.end(), Difficulty.EASY)
//            ));
//
//            for (int i = 3; i < level; i++) {
//                int promotionPeriodStart = 0;
//                switch (i) {
//                    case 3: promotionPeriodStart = 2*24; break;
//                    case 4: promotionPeriodStart = 4*24; break;
//                    case 5: promotionPeriodStart = 8*24; break;
//                    case 6: promotionPeriodStart = 16*24; break;
//                    case 7: promotionPeriodStart = 32*24; break;
//                }
//                metadata.getRecentDifficulty().add(
//                        new DifficultyAtTime(createDateDifferingBy(LEVEL_1.begin(), promotionPeriodStart, Calendar.HOUR), Difficulty.EASY));
//            }
//            when(clock.getTime()).thenReturn(createDateDifferingBy(LEVEL_1.begin(), ));
//
//            boolean shouldRemind = reminder.shouldBeReminded(metadata);
//
//            assertThat(shouldRemind, is(false));
//        }
//    }

    private static Date createDateDifferingBy(Date now, int amount, int calendarField) {
        Calendar c = Calendar.getInstance();
        c.setTime(now);
        c.add(calendarField, amount);
        return c.getTime();
    }
}