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
            List<List<DifficultyAtTime>> groups = findPairsFitting4And20HoursPeriodInOrder(successAfterLastFailure
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

    private List<List<DifficultyAtTime>> findPairsFitting4And20HoursPeriodInOrder(List<DifficultyAtTime> difficulty) {
        List<List<DifficultyAtTime>> groups = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            groups.add(new ArrayList<DifficultyAtTime>());
        }

        List<Integer> indiceForGroupZero = findIndexesForFirstPairSubmittedWithinNHours(difficulty,  4);
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
            if (isMessagesNewerThanNHours(difficultyAtTime, hours)) {
                counter++;
            }
        }
        return counter;
    }
}
