package uk.ignas.langlearn.core;

import com.google.common.collect.ImmutableMap;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class Reminder {
    private final Clock clock;
    private final PromotionDuration promotionDuration = new PromotionDuration();

    public Reminder(Clock clock) {
        this.clock = clock;
    }

    public boolean shouldBeReminded(TranslationMetadata metadata) {
        List<List<DifficultyAtTime>> promotionPeriodJumpers = getPromotionPeriodsJumpingGroups(metadata);
        int promotionDurationInHours = findCurrentPromotionDurationInHours(promotionPeriodJumpers);
        return !restrictedByPromotion(promotionPeriodJumpers, promotionDurationInHours);
    }

    private boolean restrictedByPromotion(List<List<DifficultyAtTime>> promotionPeriodsJumpers, int promotionDurationInHours) {
        List<DifficultyAtTime> promotionPeriodsJumper = promotionPeriodsJumpers.get(promotionPeriodsJumpers.size() - 1);
        return !promotionPeriodsJumper.isEmpty() && isMessagesNewerThanNHours(promotionPeriodsJumper.get(0), promotionDurationInHours);
    }

    private int findCurrentPromotionDurationInHours(List<List<DifficultyAtTime>> promotionPeriodsJumpers) {
        int promotionLevel = 1;
        int promotionDurationInHours = promotionDuration.getHoursByLevel(promotionLevel);
        for (List<DifficultyAtTime> promotionPeriodsJumper : promotionPeriodsJumpers) {
            promotionDurationInHours = promotionDuration.getHoursByLevel(promotionLevel);
            boolean isLevelPromoted = !promotionPeriodsJumper.isEmpty() && !isMessagesNewerThanNHours(promotionPeriodsJumper.get(0), promotionDurationInHours);
            if (isLevelPromoted) {
                promotionLevel++;
            }
        }
        return promotionDurationInHours;
    }

    private static class PromotionDuration {
        private Map<Integer, Integer> periodsByLevel = ImmutableMap.<Integer, Integer>builder()
                .put(1, 4)
                .put(2, 20)
                .put(3, 1 * 24)
                .put(4, 2 * 24)
                .put(5, 4 * 24)
                .put(6, 8 * 24)
                .put(7, 16 * 24)
                .put(8, 32 * 24)
                //for higher than 8 - use value for 8
                .build();

        public Integer getHoursByLevel(int level) {
            if (level > 7) {
                return periodsByLevel.get(8);
            } else if (level > 0) {
                return periodsByLevel.get(level);
            } else {
                throw new RuntimeException("promotion period level should never be less than 1. got " + level);
            }
        }
    }


    private List<List<DifficultyAtTime>> getPromotionPeriodsJumpingGroups(TranslationMetadata metadata) {
        List<DifficultyAtTime> successAfterLastFailure = getSuccessLogAfterLastFailure(metadata);
        List<List<DifficultyAtTime>> groups;
        boolean wasJustFailed = successAfterLastFailure.size() != metadata.getRecentDifficulty().size();
        int requiredMsgCountForLevelOne = wasJustFailed ? 3 : 2;
        groups = findMsgGroupsFittingPeriodsInOrder(successAfterLastFailure,
                new MsgCountAndNumOfHours(requiredMsgCountForLevelOne, 4),
                new MsgCountAndNumOfHours(2, 20)
        );
        if (groups.size() == 2) {
            List<DifficultyAtTime> lastGroup = groups.get(groups.size() - 1);
            DifficultyAtTime lastMessage = lastGroup.get(lastGroup.size() - 1);
            int indexOfLastANalysedMessage = metadata.getRecentDifficulty().indexOf(lastMessage);
            for (int i = indexOfLastANalysedMessage + 1; i < metadata.getRecentDifficulty().size(); i++) {
                if (i < metadata.getRecentDifficulty().size()) {
                    groups.add(Collections.singletonList(metadata.getRecentDifficulty().get(i)));
                }
            }
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


        List<Integer> indiceForGroupZero = findIndexesForFirstNumOfMsgsSubmittedWithinNHours(messages, countInPeriod1);
        for (Integer index : indiceForGroupZero) {
            if (groups.size() == 0) {
                groups.add(new ArrayList<DifficultyAtTime>());
            }
            groups.get(0).add(messages.get(index));
        }

        if (groups.size() == 0) {
            groups.add(new ArrayList<DifficultyAtTime>());
        }
        if (!groups.get(0).isEmpty()) {
            List<DifficultyAtTime> sublist = messages.subList(indiceForGroupZero.get(indiceForGroupZero.size() - 1) + 1, messages.size());
            List<Integer> indiceForGroupOne = findIndexesForFirstNumOfMsgsSubmittedWithinNHours(sublist, countInPeriod2);

            for (Integer index : indiceForGroupOne) {
                if (groups.size() == 1) {
                    groups.add(new ArrayList<DifficultyAtTime>());
                }
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
