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

    class MsgCountAndNumOfHours {
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
            List<List<DifficultyAtTime>> groups = findPairsFitting4And20HoursPeriodInOrder(successAfterLastFailure,
                    new MsgCountAndNumOfHours(2, 4),
                    new MsgCountAndNumOfHours(2, 20)
            );
            int promotionLevel = 1;
            if (promotionLevel == 1) {
                List<DifficultyAtTime> messages = groups.get(0);
                boolean isLevelPromoted = !messages.isEmpty() && !isMessagesNewerThanNHours(messages.get(0), 4);
                if (isLevelPromoted) {
                    promotionLevel++;
                }
                shouldRemind = messages.isEmpty() || !isMessagesNewerThanNHours(messages.get(0), 4);
            }
            if (promotionLevel == 2) {
                List<DifficultyAtTime> messages = groups.get(1);
                boolean isLevelPromoted = !messages.isEmpty() && !isMessagesNewerThanNHours(messages.get(0), 20);
                if (isLevelPromoted) {
                    promotionLevel++;
                }
                shouldRemind = messages.isEmpty() || !isMessagesNewerThanNHours(messages.get(0), 20);
            }
        }

        return shouldRemind;
    }

    private boolean isMessagesNewerThanNHours(DifficultyAtTime difficultyAtTime, int hours) {
        return TimeUnit.MILLISECONDS.toHours(clock.getTime().getTime() - difficultyAtTime.getTimepoint().getTime()) < hours;
    }

    private List<List<DifficultyAtTime>> findPairsFitting4And20HoursPeriodInOrder(List<DifficultyAtTime> messages,
                                                                                  MsgCountAndNumOfHours countInPeriod1,
                                                                                  MsgCountAndNumOfHours countInPeriod2) {
        List<List<DifficultyAtTime>> groups = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            groups.add(new ArrayList<DifficultyAtTime>());
        }

        List<Integer> indiceForGroupZero = findIndexesForFirstNumOfMsgsSubmittedWithinNHours(messages, countInPeriod1);
        for (Integer index : indiceForGroupZero) {
            groups.get(0).add(messages.get(index));
        }
        if (!groups.get(0).isEmpty()) {
            List<DifficultyAtTime> sublist = messages.subList(indiceForGroupZero.get(1) + 1, messages.size());
            List<Integer> indiceForGroupOne = findIndexesForFirstNumOfMsgsSubmittedWithinNHours(sublist, countInPeriod2);

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

    private List<Integer> findIndexesForFirstNumOfMsgsSubmittedWithinNHours(List<DifficultyAtTime> difficultyAtTimes, MsgCountAndNumOfHours msgCountAndNumOfHours) {
        int msgCount = msgCountAndNumOfHours.msgCount;
        int hours = msgCountAndNumOfHours.numOfHours;
        List<Integer> foundMessages = new ArrayList<>();
        for (int i = msgCount - 1; i < difficultyAtTimes.size(); i++) {
            Date currentLatest = difficultyAtTimes.get(i).getTimepoint();
            Date currentEarlest = difficultyAtTimes.get(i - msgCount + 1).getTimepoint();
            if (TimeUnit.MILLISECONDS.toHours(currentLatest.getTime() - currentEarlest.getTime()) < hours) {
                for (int j = i - msgCount + 1; j <= i; j++) {
                    foundMessages.add(j);
                }
                break;
            } else {
                foundMessages.clear();
            }
        }
        return foundMessages;
    }

    private int countInNRecentHours(List<DifficultyAtTime> difficultyAtTimes, int hours) {
        int counter = 0;
        for (DifficultyAtTime difficultyAtTime : difficultyAtTimes) {
            if (isMessagesNewerThanNHours(difficultyAtTime, hours)) {
                counter++;
            }
        }
        return counter;
    }
}
