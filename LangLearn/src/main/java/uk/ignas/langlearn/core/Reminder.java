package uk.ignas.langlearn.core;

import com.google.common.collect.Iterables;

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
        List<List<DifficultyAtTime>> promotionPeriodJumpers = getPromotionPeriodsJumpingGroups(metadata);
        return !isBeingPromoted(promotionPeriodJumpers);
    }

    private boolean isBeingPromoted(List<List<DifficultyAtTime>> promotionPeriodJumpers) {
        boolean isBeingPromoted = false;
        int promotionLevel = 1;
        if (promotionLevel == 1) {
            List<DifficultyAtTime> messages = promotionPeriodJumpers.get(0);
            boolean isLevelPromoted = !messages.isEmpty() && !isMessagesNewerThanNHours(messages.get(0), 4);
            if (isLevelPromoted) {
                promotionLevel++;
            }
            isBeingPromoted = !messages.isEmpty() && isMessagesNewerThanNHours(messages.get(0), 4);
        }
        if (promotionLevel == 2) {
            List<DifficultyAtTime> messages = promotionPeriodJumpers.get(1);
            boolean isLevelPromoted = !messages.isEmpty() && !isMessagesNewerThanNHours(messages.get(0), 20);
            if (isLevelPromoted) {
                promotionLevel++;
            }
            isBeingPromoted = !messages.isEmpty() && isMessagesNewerThanNHours(messages.get(0), 20);
        }
        if (promotionLevel == 3) {
            DifficultyAtTime message = Iterables.getOnlyElement(promotionPeriodJumpers.get(2), null);
            boolean isLevelPromoted = message != null && !isMessagesNewerThanNHours(message, 24);
            if (isLevelPromoted) {
                promotionLevel++;
            }
            isBeingPromoted = message != null && isMessagesNewerThanNHours(message, 24);
        }
        if (promotionLevel == 4) {
            DifficultyAtTime message = Iterables.getOnlyElement(promotionPeriodJumpers.get(3), null);

            isBeingPromoted = message != null && isMessagesNewerThanNHours(message, 48);
        }

        return isBeingPromoted;
    }

    private List<List<DifficultyAtTime>> getPromotionPeriodsJumpingGroups(TranslationMetadata metadata) {
        List<DifficultyAtTime> successAfterLastFailure = getSuccessLogAfterLastFailure(metadata);
        List<List<DifficultyAtTime>> groups;
        boolean wasEverFailed = successAfterLastFailure.size() != metadata.getRecentDifficulty().size();
        if (wasEverFailed) {
            groups = findMsgGroupsFittingPeriodsInOrder(successAfterLastFailure,
                    new MsgCountAndNumOfHours(3, 4),
                    new MsgCountAndNumOfHours(2, 20)
            );
        } else {
            groups = findMsgGroupsFittingPeriodsInOrder(successAfterLastFailure,
                    new MsgCountAndNumOfHours(2, 4),
                    new MsgCountAndNumOfHours(2, 20)
            );
        }
        List<DifficultyAtTime> lastGroup = groups.get(groups.size() - 1);
        if (!lastGroup.isEmpty()) {
            DifficultyAtTime lastMessage = lastGroup.get(lastGroup.size() - 1);
            int indexOfLastANalysedMessage = metadata.getRecentDifficulty().indexOf(lastMessage);
            if (indexOfLastANalysedMessage + 1 < metadata.getRecentDifficulty().size()) {
                groups.add(Collections.singletonList(metadata.getRecentDifficulty().get(indexOfLastANalysedMessage + 1)));
            } else {
                groups.add(new ArrayList<DifficultyAtTime>());
            }
            if (indexOfLastANalysedMessage + 2 < metadata.getRecentDifficulty().size()) {
                groups.add(Collections.singletonList(metadata.getRecentDifficulty().get(indexOfLastANalysedMessage + 2)));
            } else {
                groups.add(new ArrayList<DifficultyAtTime>());
            }
        } else {
            groups.add(new ArrayList<DifficultyAtTime>());
            groups.add(new ArrayList<DifficultyAtTime>());
        }
        return groups;
    }

    private boolean isMessagesNewerThanNHours(DifficultyAtTime difficultyAtTime, int hours) {
        return TimeUnit.MILLISECONDS.toHours(clock.getTime().getTime() - difficultyAtTime.getTimepoint().getTime()) < hours;
    }

    private List<List<DifficultyAtTime>> findMsgGroupsFittingPeriodsInOrder(List<DifficultyAtTime> messages,
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
            List<DifficultyAtTime> sublist = messages.subList(indiceForGroupZero.get(indiceForGroupZero.size() - 1) + 1, messages.size());
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

    class MsgCountAndNumOfHours {
        private final int msgCount;
        private final int numOfHours;

        public MsgCountAndNumOfHours(int msgCount, int numOfHours) {
            this.msgCount = msgCount;
            this.numOfHours = numOfHours;
        }
    }

}
