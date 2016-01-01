package uk.ignas.langlearn.testutils;

import java.util.Calendar;
import java.util.Date;

public enum PromotionPeriod {
    LEVEL_1(now(),
            createDateDifferingBy(now(), PromotionPeriod.Start.LEVEL_2 *60-1, Calendar.MINUTE)),
    LEVEL_2(createDateDifferingBy(now(), PromotionPeriod.Start.LEVEL_2, Calendar.HOUR),
            createDateDifferingBy(now(), PromotionPeriod.Start.LEVEL_3 *60-1, Calendar.MINUTE)),
    LEVEL_3(createDateDifferingBy(now(), PromotionPeriod.Start.LEVEL_3, Calendar.HOUR),
            createDateDifferingBy(now(), PromotionPeriod.Start.LEVEL_4 *60-1, Calendar.MINUTE)),
    LEVEL_4(createDateDifferingBy(now(), PromotionPeriod.Start.LEVEL_4, Calendar.HOUR),
            createDateDifferingBy(now(), PromotionPeriod.Start.LEVEL_5 *60-1, Calendar.MINUTE)),
    LEVEL_5(createDateDifferingBy(now(), PromotionPeriod.Start.LEVEL_5, Calendar.HOUR),
            createDateDifferingBy(now(), PromotionPeriod.Start.LEVEL_6 *60-1, Calendar.MINUTE)),
    LEVEL_6(createDateDifferingBy(now(), PromotionPeriod.Start.LEVEL_6, Calendar.HOUR),
            createDateDifferingBy(now(), PromotionPeriod.Start.LEVEL_7 *60-1, Calendar.MINUTE)),
    LEVEL_7(createDateDifferingBy(now(), PromotionPeriod.Start.LEVEL_7, Calendar.HOUR),
            createDateDifferingBy(now(), PromotionPeriod.Start.LEVEL_8 *60-1, Calendar.MINUTE)),
    LEVEL_8(createDateDifferingBy(now(), PromotionPeriod.Start.LEVEL_8, Calendar.HOUR),
            createDateDifferingBy(now(), PromotionPeriod.Start.LEVEL_8_END*60-1, Calendar.MINUTE));

    public static class Start {
        public static int LEVEL_2 = 4;
        public static int LEVEL_3 = 24;
        public static int LEVEL_4 = 2*24;
        public static int LEVEL_5 = 4*24;
        public static int LEVEL_6 = 8*24;
        public static int LEVEL_7 = 16*24;
        public static int LEVEL_8 = 32*24;
        public static int LEVEL_8_END = 32*24;
    }

    public static final Date NOW = now();

    private Date begin;
    private Date almostEnd;

    PromotionPeriod(Date begin, Date almostEnd) {
        this.begin = begin;
        this.almostEnd = almostEnd;
    }

    public Date begin() {
        return begin;
    }

    public Date almostEnd() {
        return almostEnd;
    }

    private static Date now(){
        Calendar c = Calendar.getInstance();
        c.set(2015, Calendar.JANUARY, 1, 12, 0);
        return c.getTime();
    }

    public static Date createDateDifferingBy(Date now, int amount, int calendarField) {
        Calendar c = Calendar.getInstance();
        c.setTime(now);
        c.add(calendarField, amount);
        return c.getTime();
    }
}
