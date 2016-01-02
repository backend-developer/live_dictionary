package uk.ignas.langlearn.testutils;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public enum PromotionPeriod {
    LEVEL_7(createDateDifferingBy(now(), Start.LEVEL_7, Calendar.HOUR),
            createDateDifferingBy(now(), End.LEVEL_7 *60-1, Calendar.MINUTE),
            createDateDifferingBy(now(), End.LEVEL_7, Calendar.HOUR)),
    LEVEL_6(createDateDifferingBy(now(), Start.LEVEL_6, Calendar.HOUR),
            createDateDifferingBy(now(), Start.LEVEL_7 *60-1, Calendar.MINUTE), LEVEL_7.begin),
    LEVEL_5(createDateDifferingBy(now(), Start.LEVEL_5, Calendar.HOUR),
            createDateDifferingBy(now(), Start.LEVEL_6 *60-1, Calendar.MINUTE), LEVEL_6.begin),
    LEVEL_4(createDateDifferingBy(now(), Start.LEVEL_4, Calendar.HOUR),
            createDateDifferingBy(now(), Start.LEVEL_5 *60-1, Calendar.MINUTE), LEVEL_5.begin),
    LEVEL_3(createDateDifferingBy(now(), Start.LEVEL_3, Calendar.HOUR),
            createDateDifferingBy(now(), Start.LEVEL_4 *60-1, Calendar.MINUTE), LEVEL_4.begin),
    LEVEL_2(createDateDifferingBy(now(), Start.LEVEL_2, Calendar.HOUR),
            createDateDifferingBy(now(), Start.LEVEL_3 *60-1, Calendar.MINUTE), LEVEL_3.begin),
    LEVEL_1(createDateDifferingBy(now(), Start.LEVEL_1, Calendar.HOUR),
            createDateDifferingBy(now(), Start.LEVEL_2 *60-1, Calendar.MINUTE), LEVEL_2.begin),
    LEVEL_0(now(),
            createDateDifferingBy(now(), Start.LEVEL_1 *60-1, Calendar.MINUTE), LEVEL_1.begin);

    public static class LEVEL_OVER_7 {
        private Date begin;
        private Date almostEnd;
        private Date end;

        LEVEL_OVER_7(Date begin, Date almostEnd, Date end) {
            this.begin = begin;
            this.almostEnd = almostEnd;
            this.end = end;
        }

        public static LEVEL_OVER_7 by(int levels) {
            return new LEVEL_OVER_7(
                    createDateDifferingBy(now(), Start.LEVEL_7 + levels * LEVEL_7.duraionHours(), Calendar.HOUR),
                    createDateDifferingBy(now(), Start.LEVEL_7 + (levels + 1)* 60* LEVEL_7.duraionHours() - 1, Calendar.MINUTE),
                    createDateDifferingBy(now(), Start.LEVEL_7 + (levels + 1) * LEVEL_7.duraionHours(), Calendar.HOUR)
                    );
        }

        public Date begin() {
            return begin;
        }

        public Date almostEnd() {
            return almostEnd;
        }

        public Date end() {
            return end;
        }

        public int duraionHours() {
            return (int)TimeUnit.MILLISECONDS.toHours(end.getTime() - begin.getTime());
        }

    }

    public static class Start {
        public static int LEVEL_1 = 4;
        public static int LEVEL_2 = 24;
        public static int LEVEL_3 = 2*24;
        public static int LEVEL_4 = 4*24;
        public static int LEVEL_5 = 8*24;
        public static int LEVEL_6 = 16*24;
        public static int LEVEL_7 = 32*24;
    }
    public static class End {
        public static int LEVEL_0 = Start.LEVEL_1;
        public static int LEVEL_7 = 64*24;
    }

    public static final Date NOW = now();

    private Date begin;
    private Date almostEnd;
    private Date end;

    PromotionPeriod(Date begin, Date almostEnd, Date end) {
        this.begin = begin;
        this.almostEnd = almostEnd;
        this.end = end;
    }

    public Date begin() {
        return begin;
    }

    public Date almostEnd() {
        return almostEnd;
    }

    public Date end() {
        return end;
    }

    public int duraionHours() {
        return (int)TimeUnit.MILLISECONDS.toHours(end.getTime() - begin.getTime());
    }

    private static Date now(){
        Calendar c = Calendar.getInstance();
        c.set(2015, Calendar.JANUARY, 1, 12, 0, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    public static Date createDateDifferingBy(Date now, int amount, int calendarField) {
        Calendar c = Calendar.getInstance();
        c.setTime(now);
        c.add(calendarField, amount);
        return c.getTime();
    }

    public static Date createDateOffsetedByHours(int amount) {
        Calendar c = Calendar.getInstance();
        c.setTime(NOW);
        c.add(Calendar.HOUR, amount);
        return c.getTime();
    }
}
