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

public class ReminderTest {
    private static final Difficulty ANY_DIFFICULTY = Difficulty.EASY;
    private static final Date NOW;
    static {
        Calendar c = Calendar.getInstance();
        c.set(2015, Calendar.JANUARY, 1, 12, 0);
        NOW = c.getTime();
    }
    public static final Date LEVEL_1_PERIOD_PASSED = createDateDifferingBy(NOW, 4, Calendar.HOUR);
    public static final Date LEVEL_1_PERIOD_PASSED_TWICE = createDateDifferingBy(NOW, 8, Calendar.HOUR);
    public static final Date LEVEL_1_PERIOD_PASSED_THREE_TIMES = createDateDifferingBy(NOW, 12, Calendar.HOUR);
    public static final Date LEVEL_1_PERIOD_NOT_YET_PASSED = createDateDifferingBy(NOW, 3*60+59, Calendar.MINUTE);
    public static final Date LEVEL_1_AND_2_PERIODS_PASSED = createDateDifferingBy(NOW, 24, Calendar.HOUR);
    public static final Date LEVEL_1_PASSED_AND_2_PERIOD_PASSED_TWICE = createDateDifferingBy(NOW, 4+20+20, Calendar.HOUR);
    public static final Date LEVEL_1_PASSED_BUT_2_PERIOD_NOT_YET_PASSED = createDateDifferingBy(NOW, 24*60-1, Calendar.MINUTE);
    public static final Date LEVEL_1_AND_2_PASSED_BUT_3_PERIOD_NOT_YET_PASSED = createDateDifferingBy(NOW, 48*60-1, Calendar.MINUTE);
    public static final Date LEVEL_1_2_AND_3_PASSED = createDateDifferingBy(NOW, 48, Calendar.HOUR);
    public static final Date LEVEL_1_2_AND_3_PASSED_BUT_4_PERIOD_NOT_YET_PASSED = createDateDifferingBy(NOW, 96*60-1, Calendar.MINUTE);
    public static final Date LEVEL_1_2_3_AND_4_PASSED = createDateDifferingBy(NOW, 96, Calendar.HOUR);

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
        when(clock.getTime()).thenReturn(PromotionPeriod.LEVEL_1.begin());
        Reminder reminder = new Reminder(clock);
        TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, asList(
                new DifficultyAtTime(PromotionPeriod.LEVEL_1.begin(), Difficulty.EASY)
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void mistakenTranslationShouldBeReminded() {
        Clock clock = mock(Clock.class);
        when(clock.getTime()).thenReturn(PromotionPeriod.LEVEL_1.begin());
        Reminder reminder = new Reminder(clock);
        TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, asList(
                new DifficultyAtTime(PromotionPeriod.LEVEL_1.begin(), Difficulty.DIFFICULT)
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void mistakenTranslationShouldBeRemindedUpTo3TimesInLeve1PromotionPeriod() {
        Clock clock = mock(Clock.class);
        when(clock.getTime()).thenReturn(NOW);
        Reminder reminder = new Reminder(clock);
        TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, asList(
                new DifficultyAtTime(NOW, Difficulty.DIFFICULT),
                new DifficultyAtTime(NOW, Difficulty.EASY),
                new DifficultyAtTime(NOW, Difficulty.EASY)
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void mistakenTranslationShouldNotBeAsked4thTimeInLevel1PromotionPeriod() {
        Clock clock = mock(Clock.class);
        when(clock.getTime()).thenReturn(NOW);
        Reminder reminder = new Reminder(clock);
        TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, asList(
                new DifficultyAtTime(NOW, Difficulty.DIFFICULT),
                new DifficultyAtTime(NOW, Difficulty.EASY),
                new DifficultyAtTime(NOW, Difficulty.EASY),
                new DifficultyAtTime(NOW, Difficulty.EASY)
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(false));
    }

    @Test
    public void mistakenTranslationShouldReturnWordBackToLevel1PromotionPeriod() {
        Clock clock = mock(Clock.class);
        when(clock.getTime()).thenReturn(LEVEL_1_PERIOD_PASSED);
        Reminder reminder = new Reminder(clock);
        TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, asList(
                new DifficultyAtTime(NOW, Difficulty.EASY),
                new DifficultyAtTime(NOW, Difficulty.EASY),
                new DifficultyAtTime(LEVEL_1_PERIOD_PASSED, Difficulty.EASY),
                new DifficultyAtTime(LEVEL_1_PERIOD_PASSED, Difficulty.EASY),
                new DifficultyAtTime(LEVEL_1_PERIOD_PASSED, Difficulty.DIFFICULT),
                new DifficultyAtTime(LEVEL_1_PERIOD_PASSED, Difficulty.EASY),
                new DifficultyAtTime(LEVEL_1_PERIOD_PASSED, Difficulty.EASY)

        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void mistakenTranslationShouldBeAskedAgainAfterLevel1PromotionPeriodHasPassedButLevel2NotYet() {
        Clock clock = mock(Clock.class);
        when(clock.getTime()).thenReturn(LEVEL_1_PERIOD_PASSED);
        Reminder reminder = new Reminder(clock);
        TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, asList(
                new DifficultyAtTime(NOW, Difficulty.DIFFICULT),
                new DifficultyAtTime(NOW, Difficulty.EASY),
                new DifficultyAtTime(NOW, Difficulty.EASY),
                new DifficultyAtTime(NOW, Difficulty.EASY)
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void mistakenTranslationShouldBeAskedUpToTwoTimesAfterLevel1PromotionPeriodHasPassedButLevel2NotYet() {
        Clock clock = mock(Clock.class);
        when(clock.getTime()).thenReturn(LEVEL_1_AND_2_PERIODS_PASSED);
        Reminder reminder = new Reminder(clock);
        TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, asList(
                new DifficultyAtTime(NOW, Difficulty.DIFFICULT),
                new DifficultyAtTime(NOW, Difficulty.EASY),
                new DifficultyAtTime(NOW, Difficulty.EASY),
                new DifficultyAtTime(NOW, Difficulty.EASY),
                new DifficultyAtTime(LEVEL_1_PERIOD_PASSED, Difficulty.EASY)
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void mistakenTranslationShouldNotBeAskedThirdTimeDuringLevel2PromotionPeriod() {
        Clock clock = mock(Clock.class);
        when(clock.getTime()).thenReturn(LEVEL_1_PASSED_BUT_2_PERIOD_NOT_YET_PASSED);
        Reminder reminder = new Reminder(clock);
        TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, asList(
                new DifficultyAtTime(NOW, Difficulty.DIFFICULT),
                new DifficultyAtTime(NOW, Difficulty.EASY),
                new DifficultyAtTime(NOW, Difficulty.EASY),
                new DifficultyAtTime(NOW, Difficulty.EASY),
                new DifficultyAtTime(LEVEL_1_PERIOD_PASSED, Difficulty.EASY),
                new DifficultyAtTime(LEVEL_1_PERIOD_PASSED, Difficulty.EASY)
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(false));
    }

    @Test
    public void brandNewTranslationShouldNotBeAskedMoreThanTwiceInLevel1PromotionPeriod() {
        Clock clock = mock(Clock.class);
        Reminder reminder = new Reminder(clock);
        when(clock.getTime()).thenReturn(LEVEL_1_PERIOD_NOT_YET_PASSED);
        TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, asList(
                new DifficultyAtTime(NOW, Difficulty.EASY),
                new DifficultyAtTime(NOW, Difficulty.EASY)
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(false));
    }

    @Test
    public void brandNewTranslationShouldBeAskedMoreThanTwiceIfAskedLessRarelyThanItIsPromotionPeriodDuringLevel1PromotionPeriod() {
        Clock clock = mock(Clock.class);
        Reminder reminder = new Reminder(clock);
        when(clock.getTime()).thenReturn(LEVEL_1_PERIOD_PASSED);
        TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, asList(
                new DifficultyAtTime(NOW, Difficulty.EASY),
                new DifficultyAtTime(LEVEL_1_PERIOD_PASSED, Difficulty.EASY)
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void brandNewTranslationShouldContinueToBeAskedMoreThanTwiceIfContinuesBeingAskedLessRarelyThanItIsPromotionPeriodDuringLevel1PromotionPeriod() {
        Clock clock = mock(Clock.class);
        Reminder reminder = new Reminder(clock);
        when(clock.getTime()).thenReturn(LEVEL_1_PERIOD_PASSED_THREE_TIMES);
        TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, asList(
                new DifficultyAtTime(NOW, Difficulty.EASY),
                new DifficultyAtTime(LEVEL_1_PERIOD_PASSED, Difficulty.EASY),
                new DifficultyAtTime(LEVEL_1_PERIOD_PASSED_TWICE, Difficulty.EASY),
                new DifficultyAtTime(LEVEL_1_PERIOD_PASSED_THREE_TIMES, Difficulty.EASY)
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void translationEvaluationDuringLevel2PromotionPeriodShouldNotAssumeThatLevel1PromotionPeriodPassedImmediately() {
        Clock clock = mock(Clock.class);
        Reminder reminder = new Reminder(clock);
        when(clock.getTime()).thenReturn(LEVEL_1_PASSED_AND_2_PERIOD_PASSED_TWICE);
        TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, asList(
                new DifficultyAtTime(NOW, Difficulty.EASY),
                new DifficultyAtTime(LEVEL_1_PERIOD_PASSED, Difficulty.EASY),
                new DifficultyAtTime(LEVEL_1_PERIOD_PASSED, Difficulty.EASY),
                new DifficultyAtTime(LEVEL_1_AND_2_PERIODS_PASSED, Difficulty.EASY),
                new DifficultyAtTime(LEVEL_1_PASSED_AND_2_PERIOD_PASSED_TWICE, Difficulty.EASY)
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void translationShouldContinueToBeAskedIfIsBeingAskedLessRarelyThanItIsPromotionPeriodDuringLevel2PromotionPeriod() {
        Clock clock = mock(Clock.class);
        Reminder reminder = new Reminder(clock);
        when(clock.getTime()).thenReturn(LEVEL_1_PASSED_AND_2_PERIOD_PASSED_TWICE);
        TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, asList(
                new DifficultyAtTime(NOW, Difficulty.EASY),
                new DifficultyAtTime(NOW, Difficulty.EASY),
                new DifficultyAtTime(LEVEL_1_PERIOD_PASSED, Difficulty.EASY),
                new DifficultyAtTime(LEVEL_1_AND_2_PERIODS_PASSED, Difficulty.EASY),
                new DifficultyAtTime(LEVEL_1_PASSED_AND_2_PERIOD_PASSED_TWICE, Difficulty.EASY)
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void mistakenTranslationShouldContinueToBeAskedIfIsBeingAskedLessRarelyThanItIsPromotionPeriodDuringLevel2PromotionPeriod() {
        Clock clock = mock(Clock.class);
        Reminder reminder = new Reminder(clock);
        when(clock.getTime()).thenReturn(LEVEL_1_PASSED_AND_2_PERIOD_PASSED_TWICE);
        TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, asList(
                new DifficultyAtTime(NOW, Difficulty.DIFFICULT),
                new DifficultyAtTime(NOW, Difficulty.EASY),
                new DifficultyAtTime(NOW, Difficulty.EASY),
                new DifficultyAtTime(NOW, Difficulty.EASY),
                new DifficultyAtTime(LEVEL_1_PERIOD_PASSED, Difficulty.EASY),
                new DifficultyAtTime(LEVEL_1_AND_2_PERIODS_PASSED, Difficulty.EASY),
                new DifficultyAtTime(LEVEL_1_PASSED_AND_2_PERIOD_PASSED_TWICE, Difficulty.EASY)
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void translationShouldBeRemindedAfterLevel1PromotionPeriodHasPassed() {
        Clock clock = mock(Clock.class);
        Reminder reminder = new Reminder(clock);
        when(clock.getTime()).thenReturn(LEVEL_1_PERIOD_PASSED);
        TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, asList(
                new DifficultyAtTime(NOW, Difficulty.EASY),
                new DifficultyAtTime(NOW, Difficulty.EASY)
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void translationShouldBeRemindedUpToTwoTimesInLevel2PromotionPeriod() {
        Clock clock = mock(Clock.class);
        Reminder reminder = new Reminder(clock);
        when(clock.getTime()).thenReturn(LEVEL_1_PASSED_BUT_2_PERIOD_NOT_YET_PASSED);
        TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, asList(
                new DifficultyAtTime(NOW, Difficulty.EASY),
                new DifficultyAtTime(NOW, Difficulty.EASY),
                new DifficultyAtTime(LEVEL_1_PERIOD_PASSED, Difficulty.EASY)
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void translationShouldNotBeRemindedThirdTimeDuringLevel2PromotionPeriod() {
        Clock clock = mock(Clock.class);
        Reminder reminder = new Reminder(clock);
        when(clock.getTime()).thenReturn(LEVEL_1_PASSED_BUT_2_PERIOD_NOT_YET_PASSED);
        TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, asList(
                new DifficultyAtTime(NOW, Difficulty.EASY),
                new DifficultyAtTime(NOW, Difficulty.EASY),
                new DifficultyAtTime(LEVEL_1_PERIOD_PASSED, Difficulty.EASY),
                new DifficultyAtTime(LEVEL_1_PERIOD_PASSED, Difficulty.EASY)
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(false));
    }

    @Test
    public void translationShouldNotBeRemindedSecondTimeDuringLevel3PromotionPeriod() {
        Clock clock = mock(Clock.class);
        Reminder reminder = new Reminder(clock);
        when(clock.getTime()).thenReturn(LEVEL_1_AND_2_PASSED_BUT_3_PERIOD_NOT_YET_PASSED);
        TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, asList(
                new DifficultyAtTime(NOW, Difficulty.EASY),
                new DifficultyAtTime(NOW, Difficulty.EASY),
                new DifficultyAtTime(LEVEL_1_PERIOD_PASSED, Difficulty.EASY),
                new DifficultyAtTime(LEVEL_1_PERIOD_PASSED, Difficulty.EASY),
                new DifficultyAtTime(LEVEL_1_AND_2_PERIODS_PASSED, Difficulty.EASY)
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(false));
    }

    @Test
    public void translationShouldBeRemindedAfterLevel3PromotionPeriodPasses() {
        Clock clock = mock(Clock.class);
        Reminder reminder = new Reminder(clock);
        when(clock.getTime()).thenReturn(LEVEL_1_2_AND_3_PASSED);
        TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, asList(
                new DifficultyAtTime(NOW, Difficulty.EASY),
                new DifficultyAtTime(NOW, Difficulty.EASY),
                new DifficultyAtTime(LEVEL_1_PERIOD_PASSED, Difficulty.EASY),
                new DifficultyAtTime(LEVEL_1_PERIOD_PASSED, Difficulty.EASY),
                new DifficultyAtTime(LEVEL_1_AND_2_PERIODS_PASSED, Difficulty.EASY)
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(true));
    }

    @Test
    public void translationShouldNotBeRemindedSecondTimeDuringLevel4PromotionPeriod() {
        Clock clock = mock(Clock.class);
        Reminder reminder = new Reminder(clock);
        when(clock.getTime()).thenReturn(LEVEL_1_2_AND_3_PASSED_BUT_4_PERIOD_NOT_YET_PASSED);
        TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, asList(
                new DifficultyAtTime(NOW, Difficulty.EASY),
                new DifficultyAtTime(NOW, Difficulty.EASY),
                new DifficultyAtTime(LEVEL_1_PERIOD_PASSED, Difficulty.EASY),
                new DifficultyAtTime(LEVEL_1_PERIOD_PASSED, Difficulty.EASY),
                new DifficultyAtTime(LEVEL_1_AND_2_PERIODS_PASSED, Difficulty.EASY),
                new DifficultyAtTime(LEVEL_1_2_AND_3_PASSED, Difficulty.EASY)
        ));

        boolean shouldRemind = reminder.shouldBeReminded(metadata);

        assertThat(shouldRemind, is(false));
    }

    @Test
    public void translationShouldBeRemindedAfterLevel4PromotionPeriodPasses() {
        Clock clock = mock(Clock.class);
        Reminder reminder = new Reminder(clock);
        when(clock.getTime()).thenReturn(LEVEL_1_2_3_AND_4_PASSED);
        TranslationMetadata metadata = new TranslationMetadata(ANY_DIFFICULTY, asList(
                new DifficultyAtTime(NOW, Difficulty.EASY),
                new DifficultyAtTime(NOW, Difficulty.EASY),
                new DifficultyAtTime(LEVEL_1_PERIOD_PASSED, Difficulty.EASY),
                new DifficultyAtTime(LEVEL_1_PERIOD_PASSED, Difficulty.EASY),
                new DifficultyAtTime(LEVEL_1_AND_2_PERIODS_PASSED, Difficulty.EASY),
                new DifficultyAtTime(LEVEL_1_2_AND_3_PASSED, Difficulty.EASY)
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
//                    new DifficultyAtTime(NOW, Difficulty.EASY),
//                    new DifficultyAtTime(NOW, Difficulty.EASY),
//                    new DifficultyAtTime(LEVEL_1_PERIOD_PASSED, Difficulty.EASY),
//                    new DifficultyAtTime(LEVEL_1_PERIOD_PASSED, Difficulty.EASY),
//                    new DifficultyAtTime(LEVEL_1_AND_2_PERIODS_PASSED, Difficulty.EASY),
//                    new DifficultyAtTime(LEVEL_1_2_AND_3_PASSED, Difficulty.EASY)
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
//                        new DifficultyAtTime(createDateDifferingBy(NOW, promotionPeriodStart, Calendar.HOUR), Difficulty.EASY));
//            }
//            when(clock.getTime()).thenReturn(createDateDifferingBy(NOW, ));
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