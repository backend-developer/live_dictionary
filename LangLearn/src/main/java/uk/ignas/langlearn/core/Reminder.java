package uk.ignas.langlearn.core;

import com.google.common.collect.Ordering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Reminder {
    private final Clock clock;

    public Reminder(Clock clock) {
        this.clock = clock;
    }

    public boolean shouldBeReminded(TranslationMetadata metadata) {
        List<DifficultyAtTime> successAfterLastFailure = getSuccessLogAfterLastFailure(metadata);
        boolean wasEverFailed = successAfterLastFailure.size() != metadata.getRecentDifficulty().size();
        int counterForLastCorrectSequence = successAfterLastFailure.size();
        boolean canRemind = false;
        if (wasEverFailed) {
            if (counterForLastCorrectSequence <= 3) {
                if (counterForLastCorrectSequence <= 2 || countInNRecentHours(successAfterLastFailure.subList(0, 3), 4) <= 2) {
                    canRemind = true;
                }
            } else if (counterForLastCorrectSequence <= 5) {
                if (counterForLastCorrectSequence <= 4 || countInNRecentHours(successAfterLastFailure.subList(3, 5), 20) <= 1) {
                    canRemind = true;
                }
            }
        } else {
            List<Integer> foundMessageIndexes = findIndexesForFirstTwoMessagesSubmittedWithinNHours(successAfterLastFailure, 4);
            if (foundMessageIndexes.isEmpty()) {
                canRemind = true;
            }
            if (!foundMessageIndexes.isEmpty()) {
                if (TimeUnit.MILLISECONDS.toHours(clock.getTime().getTime() - successAfterLastFailure.get(0).getTimepoint().getTime()) < 4) {

                } else if (counterForLastCorrectSequence <= 4) {
                    if (counterForLastCorrectSequence <= 3 || countInNRecentHours(successAfterLastFailure.subList(2, 4), 20) <= 1) {
                        canRemind = true;
                    } else {
                        canRemind = false;
                    }
                }
            }
        }

        return canRemind;
    }

    private List<DifficultyAtTime> getSuccessLogAfterLastFailure(TranslationMetadata metadata) {
        List<DifficultyAtTime> successLogAfterLastFailure = new ArrayList<>();
        for (DifficultyAtTime difficultyAtTime : metadata.getRecentDifficulty()) {
            if (difficultyAtTime.getDifficulty() == Difficulty.DIFFICULT) {
                successLogAfterLastFailure.clear();
            } else {
                successLogAfterLastFailure.add(difficultyAtTime);
            }
        }
        return successLogAfterLastFailure;
    }

    private List<Integer> findIndexesForFirstTwoMessagesSubmittedWithinNHours(List<DifficultyAtTime> difficultyAtTimes, int hours) {
        List<Integer> foundMessages = new ArrayList<>();
        for (int i = 1; i < difficultyAtTimes.size(); i++) {
            Date iDate = difficultyAtTimes.get(i).getTimepoint();
            Date iMinus1Date = difficultyAtTimes.get(i-1).getTimepoint();
            if (TimeUnit.MILLISECONDS.toHours(iDate.getTime() - iMinus1Date.getTime()) < hours) {
                foundMessages.add(i);
                foundMessages.add(i-1);
            } else {
                foundMessages.clear();
            }
            if (foundMessages.size() == 2) {
                break;
            }
        }
        return foundMessages;
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
