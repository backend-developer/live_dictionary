package uk.ignas.langlearn.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Reminder {
    private final Clock clock;

    public Reminder(Clock clock) {
        this.clock = clock;
    }

    private static class MsgCountAndNumOfHours {
        private final int msgCount;
        private final int numOfHours;

        public MsgCountAndNumOfHours(int msgCount, int numOfHours) {
            this.msgCount = msgCount;
            this.numOfHours = numOfHours;
        }
    }

    public boolean shouldBeReminded(TranslationMetadata metadata) {
        List<DifficultyAtTime> successAfterLastFailure = getSuccessLogAfterLastFailure(metadata);
        boolean wasEverFailed = successAfterLastFailure.size() != metadata.getRecentDifficulty().size();
        int counterForLastCorrectSequence = successAfterLastFailure.size();
        boolean shouldRemind = false;
        if (wasEverFailed) {
            if (counterForLastCorrectSequence <= 3) {
                if (counterForLastCorrectSequence <= 2 || countInNRecentHours(successAfterLastFailure.subList(0, 3), 4) <= 2) {
                    shouldRemind = true;
                }
            } else if (counterForLastCorrectSequence <= 5) {
                if (counterForLastCorrectSequence <= 4 || countInNRecentHours(successAfterLastFailure.subList(3, 5), 20) <= 1) {
                    shouldRemind = true;
                }
            }
        } else {
            List<List<DifficultyAtTime>> groups = findPairsFittingNHoursPeriodInOrder(successAfterLastFailure,
                    new MsgCountAndNumOfHours(2, 4),
                    new MsgCountAndNumOfHours(2, 20));
            int promotionLevel = 1;

            shouldRemind = groups.get(0).isEmpty() || !isMessageYoungerThanNHours(groups.get(0).get(0), 4);
            boolean isLevelPromoted = !groups.get(0).isEmpty() && !isMessageYoungerThanNHours(groups.get(0).get(0), 4);
            if (isLevelPromoted) {
                promotionLevel++;
            }
            if (promotionLevel == 2) {
                if (groups.get(1).isEmpty()) {
                    shouldRemind = true;
                } else if (isMessageYoungerThanNHours(groups.get(1).get(0), 20)) {
                    shouldRemind = false;
                } else {
                    shouldRemind = true;
                    promotionLevel++;
                }
            }
        }

        return shouldRemind;
    }

    private boolean isMessageYoungerThanNHours(DifficultyAtTime difficultyAtTime, int hours) {
        return TimeUnit.MILLISECONDS.toHours(clock.getTime().getTime() - difficultyAtTime.getTimepoint().getTime()) < hours;
    }

    private List<List<DifficultyAtTime>> findPairsFittingNHoursPeriodInOrder(List<DifficultyAtTime> difficulty, MsgCountAndNumOfHours msgCountAndNumOfHours, MsgCountAndNumOfHours msgCountAndNumOfHours1) {
        List<List<DifficultyAtTime>> groups = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            groups.add(new ArrayList<DifficultyAtTime>());
        }

        List<Integer> indiceForGroupZero = findIndexesForFirstPairSubmittedWithinNHours(difficulty, 4);
        for (Integer index : indiceForGroupZero) {
            groups.get(0).add(difficulty.get(index));
        }
        if (!groups.get(0).isEmpty()) {
            List<DifficultyAtTime> sublist = difficulty.subList(indiceForGroupZero.get(1) + 1, difficulty.size());
            List<Integer> indiceForGroupOne = findIndexesForFirstPairSubmittedWithinNHours(sublist, 20);

            for (Integer index : indiceForGroupOne) {
                groups.get(1).add(sublist.get(index));
            }
        }
        return groups;
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

    private List<Integer> findIndexesForFirstPairSubmittedWithinNHours(List<DifficultyAtTime> difficultyAtTimes, int hours) {
        List<Integer> foundMessages = new ArrayList<>();
        for (int i = 1; i < difficultyAtTimes.size(); i++) {
            Date iDate = difficultyAtTimes.get(i).getTimepoint();
            Date iMinus1Date = difficultyAtTimes.get(i - 1).getTimepoint();
            if (TimeUnit.MILLISECONDS.toHours(iDate.getTime() - iMinus1Date.getTime()) < hours) {
                foundMessages.add(i - 1);
                foundMessages.add(i);
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
                if (isMessageYoungerThanNHours(difficultyAtTime, hours)) {
                    counter++;
                }
            } else {
                counter = 0;
            }
        }
        return counter;
    }
}
