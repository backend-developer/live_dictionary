package uk.ignas.langlearn.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Reminder {
    private static final int TIMES_TO_SUCCEED_IN_0_LEVEL_FOR_STAGING = 2;
    private static final int TIMES_TO_SUCCEED_IN_1_LEVEL_FOR_STAGING = 3;

    private static final int PERIOD_IN_HOURS_TO_REACH_LEVEL_2 = 4;

    private final Clock clock;

    public Reminder(Clock clock) {
        this.clock = clock;
    }

    public boolean shouldBeReminded(TranslationMetadata metadata) {
        int level1 = 0;
        List<DifficultyAtTime> subset = new ArrayList<>();
        for (DifficultyAtTime difficultyAtTime : metadata.getRecentDifficulty()) {
            if (difficultyAtTime.getDifficulty() == Difficulty.DIFFICULT) {
                level1 = 1;

                subset.clear();
            } else {

                subset.add(difficultyAtTime);
            }
        }
        int counterForLastCorrectSequence = subset.size();
        boolean canRemind = false;
        if (level1 == 1) {
            if (counterForLastCorrectSequence <= 3) {
                if (counterForLastCorrectSequence <= 2 || countInNRecentHours(subset.subList(0, 3), 4) <= 2) {
                    canRemind = true;
                }
            } else if (counterForLastCorrectSequence <= 5) {
                if (counterForLastCorrectSequence <= 4 || countInNRecentHours(subset.subList(3, 5), 20) <= 1) {
                    canRemind = true;
                }
            }
        } else if (level1 == 0) {
            if (counterForLastCorrectSequence <= 2) {
                if (counterForLastCorrectSequence <= 1 || countInNRecentHours(subset.subList(0, 2), 4) <= 1) {
                    canRemind = true;
                }
            } else if (counterForLastCorrectSequence <= 4) {
                if (counterForLastCorrectSequence <= 3 || countInNRecentHours(subset.subList(2, 4), 20) <= 1) {
                    canRemind = true;
                }
            }
        }

        return canRemind;
    }

    private int countInNRecentHours(List<DifficultyAtTime> difficultyAtTimes, int hours) {
        int counter = 0;
        for (DifficultyAtTime difficultyAtTime : difficultyAtTimes) {
            if (difficultyAtTime.getDifficulty() == Difficulty.EASY) {
                if (TimeUnit.MILLISECONDS.toHours(clock.getTime().getTime() - difficultyAtTime.getTimepoint().getTime()) < hours) {
                    counter++;
                }
            } else {
                counter = 0;
            }
        }
        return counter;
    }
}
