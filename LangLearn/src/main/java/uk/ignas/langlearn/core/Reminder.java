package uk.ignas.langlearn.core;

import com.google.common.collect.ImmutableMap;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Iterables.getLast;

/**
 *
 *In order not to waste user's time each translation should be reminded less often, as user learns it.
 *Hence, there is concept of levels introduced. The higher the level, the less often the translation will be asked.
 *
 *Translation is needed to be answered correctly certain amount of times
 *during fixed length period (a.k.a. promotion period) to be promoted to the next level.
 *Once required number of correct answers is collected in less time than lasts promotion period,
 *translation is no longer asked until promotion period is finished.
 *
 *Example diagram:
 *                                        from there points translation will not be asked for the user
 *                                        until promotion period will be finished
 *                                        A.K.A. restricted by promotion
 *                                        :                :
 *                                        :                :........................
 *                                        :                                        :
 *                                        :      promoted                          :      promoted
 *                                        :      to level 1                        :      to level 2
 *                                        :      :                                 :      :
 *                                        :      :                                 :      :
 *                 -------------------------------------------------------------------------------------------
 *correct answers  :    * *          *  * *              *                  *      *
 *promotion periods:                 |___________|                          |_____________|
 *                //------------------------------------------------------------------------------------------
 *                //    : :           level 0.           :                   level 1.
 *                //    : :         3 answers required   :                 2 answers required
 *                //    : :       to collect during 4 h  :               to collect during 20 h
 *                //    : :     to promote               :             to promote
 *                //    : :                              :
 *                //    promotion period did not restricted
 *                //    translation from being asked the user
 *                //    as there were not collected enough
 *                //    correct answers during promotion
 *                //    period. Hence word was not promoted
 */

public class Reminder {
    private final Clock clock;
    private final PromotionDuration promotionDuration = new PromotionDuration();

    public Reminder(Clock clock) {
        this.clock = clock;
    }

    public boolean shouldBeReminded(TranslationMetadata metadata) {
        return !restrictedByPromotionPeriod(metadata);
    }

    private boolean restrictedByPromotionPeriod(TranslationMetadata metadata) {
        List<List<DifficultyAtTime>> promotionPeriodJumpers = getPromotionPeriodsJumpingGroups(metadata);
        if (!promotionPeriodJumpers.isEmpty()) {
            int promotionDurationInHours = findCurrentPromotionDurationInHours(promotionPeriodJumpers);
            List<DifficultyAtTime> promotionPeriodsJumper = getLast(promotionPeriodJumpers);
            return groupStillRestricted(promotionDurationInHours, promotionPeriodsJumper);
        } else {
            return false;
        }
    }

    private boolean groupStillRestricted(int promotionDurationInHours, List<DifficultyAtTime> promotionPeriodsJumper) {
        return !promotionPeriodsJumper.isEmpty() && isMessagesNewerThanNHours(promotionPeriodsJumper.get(0), promotionDurationInHours);
    }

    private int findCurrentPromotionDurationInHours(List<List<DifficultyAtTime>> promotionPeriodsJumpers) {
        int promotionDurationInHours = promotionDuration.getHoursByLevel(0);
        for (int promotionLevel = 0; promotionLevel < promotionPeriodsJumpers.size(); promotionLevel++) {
            promotionDurationInHours = promotionDuration.getHoursByLevel(promotionLevel);
        }
        return promotionDurationInHours;
    }

    private List<List<DifficultyAtTime>> getPromotionPeriodsJumpingGroups(TranslationMetadata metadata) {
        List<List<DifficultyAtTime>> groups = getFirstTwoPromotionJumpingGroups(metadata);
        if (groups.size() == 2) {
            List<DifficultyAtTime> lastGroup = getLast(groups);
            DifficultyAtTime lastMessage = getLast(lastGroup);
            int indexOfLastANalysedMessage = metadata.getRecentDifficulty().indexOf(lastMessage);
            int firstNotYetAnalysedMessage = indexOfLastANalysedMessage + 1;
            for (int i = firstNotYetAnalysedMessage; i < metadata.getRecentDifficulty().size(); i++) {
                if (i < metadata.getRecentDifficulty().size()) {
                    groups.add(Collections.singletonList(metadata.getRecentDifficulty().get(i)));
                }
            }
        }
        return groups;
    }

    private List<List<DifficultyAtTime>> getFirstTwoPromotionJumpingGroups(TranslationMetadata metadata) {
        List<DifficultyAtTime> successAfterLastFailure = getSuccessLogAfterLastFailure(metadata);
        List<List<DifficultyAtTime>> groups;
        boolean wasJustFailed = successAfterLastFailure.size() != metadata.getRecentDifficulty().size();
        int requiredMsgCountForLevel0 = wasJustFailed ? 3 : 2;
        int level0PromotionPeriodDuration = 4;
        int level1PromotionPeriodDuration = 20;
        int requiredMsgCountForLevel1 = 2;
        groups = findMsgGroupsFittingPeriodsInOrder(successAfterLastFailure,
                new MsgCountAndNumOfHours(requiredMsgCountForLevel0, level0PromotionPeriodDuration),
                new MsgCountAndNumOfHours(requiredMsgCountForLevel1, level1PromotionPeriodDuration)
        );
        return groups;
    }

    private boolean isMessagesNewerThanNHours(DifficultyAtTime difficultyAtTime, int hours) {
        return TimeUnit.MILLISECONDS.toHours(clock.getTime().getTime() - difficultyAtTime.getTimepoint().getTime()) < hours;
    }

    private List<List<DifficultyAtTime>> findMsgGroupsFittingPeriodsInOrder(List<DifficultyAtTime> messages,
                                                                            MsgCountAndNumOfHours countInPeriod0,
                                                                            MsgCountAndNumOfHours countInPeriod1) {
        List<List<DifficultyAtTime>> groups = new ArrayList<>();

        ArrayList<DifficultyAtTime> group = findMessagesWithinPeriod(messages, countInPeriod0);
        if (!group.isEmpty()) {
            groups.add(group);
        }

        if (!groups.isEmpty()) {
            DifficultyAtTime endOfGroupZero = getLast(groups.get(0));
            List<DifficultyAtTime> unsearchedTail = findUnsearchedTail(messages, endOfGroupZero);
            ArrayList<DifficultyAtTime> group1 = findMessagesWithinPeriod(unsearchedTail, countInPeriod1);
            if (!group1.isEmpty()) {
                groups.add(group1);
            }
        }
        return groups;
    }

    private List<DifficultyAtTime> findUnsearchedTail(List<DifficultyAtTime> messages, DifficultyAtTime endOfGroupZero) {
        int indexJustAfterGroupZero = messages.indexOf(endOfGroupZero) + 1;
        return messages.subList(indexJustAfterGroupZero, messages.size());
    }

    private ArrayList<DifficultyAtTime> findMessagesWithinPeriod(List<DifficultyAtTime> messages, MsgCountAndNumOfHours requiredCountDuringPeriod) {
        List<Integer> indexes = findIndexesForSubsequentRecordsWithinPeriod(messages, requiredCountDuringPeriod);
        ArrayList<DifficultyAtTime> messagesWithinPeriod = new ArrayList<>();
        if (!indexes.isEmpty()) {
            for (Integer index : indexes) {
                messagesWithinPeriod.add(messages.get(index));
            }
        }
        return messagesWithinPeriod;
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

    private List<Integer> findIndexesForSubsequentRecordsWithinPeriod(List<DifficultyAtTime> logOrderedByTime, MsgCountAndNumOfHours msgCountAndNumOfHours) {
        int recordsCount = msgCountAndNumOfHours.count;
        int hours = msgCountAndNumOfHours.numOfHours;
        List<Integer> foundMessages = new ArrayList<>();
        for (int i = recordsCount - 1; i < logOrderedByTime.size(); i++) {
            long timeDifference = getTimeDifferenceBetweenRecords(logOrderedByTime, i - recordsCount + 1, i);
            if (timeDifference < hours) {
                foundMessages.addAll(getClosedRange(i - recordsCount + 1, i));
                break;
            }
        }
        return foundMessages;
    }

    private ArrayList<Integer> getClosedRange(int start, int end) {
        ArrayList<Integer> records = new ArrayList<>();
        for (int j = start; j <= end; j++) {
            records.add(j);
        }
        return records;
    }

    private long getTimeDifferenceBetweenRecords(List<DifficultyAtTime> logOrderedByTime, int beginIndex, int endIndex) {
        Date currentLatest = logOrderedByTime.get(endIndex).getTimepoint();
        Date currentEarlest = logOrderedByTime.get(beginIndex).getTimepoint();
        return TimeUnit.MILLISECONDS.toHours(currentLatest.getTime() - currentEarlest.getTime());
    }

    class MsgCountAndNumOfHours {
        private final int count;
        private final int numOfHours;

        public MsgCountAndNumOfHours(int count, int numOfHours) {
            this.count = count;
            this.numOfHours = numOfHours;
        }
    }

    private static class PromotionDuration {
        private Map<Integer, Integer> periodsByLevel = ImmutableMap.<Integer, Integer>builder()
                .put(0, 4)
                .put(1, 20)
                .put(2, 1 * 24)
                .put(3, 2 * 24)
                .put(4, 4 * 24)
                .put(5, 8 * 24)
                .put(6, 16 * 24)
                .put(7, 32 * 24)
                //for higher than 7 - use value for 7
                .build();

        public Integer getHoursByLevel(int level) {
            if (level > 6) {
                return periodsByLevel.get(7);
            } else if (level >= 0) {
                return periodsByLevel.get(level);
            } else {
                throw new RuntimeException("promotion period level should never be less than 1. got " + level);
            }
        }
    }
}
