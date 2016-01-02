package uk.ignas.langlearn.core;

import com.google.common.collect.ImmutableMap;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Iterables.getLast;

/**
 *
 *In order not to waste user's time each translation should be reminded less often, as user learns it.
 *Hence, there are levels introduces, the higher the level, the less often the translation will be asked.
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
        int promotionDurationInHours = findCurrentPromotionDurationInHours(promotionPeriodJumpers);
        List<DifficultyAtTime> promotionPeriodsJumper = getLast(promotionPeriodJumpers);
        boolean isRestricted = !promotionPeriodsJumper.isEmpty() && isMessagesNewerThanNHours(promotionPeriodsJumper.get(0), promotionDurationInHours);
        return isRestricted;
    }

    private int findCurrentPromotionDurationInHours(List<List<DifficultyAtTime>> promotionPeriodsJumpers) {
        int promotionDurationInHours = promotionDuration.getHoursByLevel(0);
        for (int promotionLevel = 0; promotionLevel < promotionPeriodsJumpers.size(); promotionLevel++) {
            promotionDurationInHours = promotionDuration.getHoursByLevel(promotionLevel);
        }
        return promotionDurationInHours;
    }

    private List<List<DifficultyAtTime>> getPromotionPeriodsJumpingGroups(TranslationMetadata metadata) {
        List<DifficultyAtTime> successAfterLastFailure = getSuccessLogAfterLastFailure(metadata);
        boolean wasJustFailed = successAfterLastFailure.size() != metadata.getRecentDifficulty().size();
        List<List<DifficultyAtTime>> groups;
        int requiredMsgCountForLevel0 = wasJustFailed ? 3 : 2;
        int level0PromotionPeriodDuration = 4;
        int level1PromotionPeriodDuration = 20;
        int requiredMsgCountForLevel1 = 2;
        groups = findMsgGroupsFittingPeriodsInOrder(successAfterLastFailure,
                new MsgCountAndNumOfHours(requiredMsgCountForLevel0, level0PromotionPeriodDuration),
                new MsgCountAndNumOfHours(requiredMsgCountForLevel1, level1PromotionPeriodDuration)
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
