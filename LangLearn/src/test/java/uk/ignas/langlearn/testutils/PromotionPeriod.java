package uk.ignas.langlearn.testutils;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public enum PromotionPeriod {
    LEVEL_8(createDateDifferingBy(now(), Start.LEVEL_8, Calendar.HOUR),
            createDateDifferingBy(now(), End.LEVEL_8*60-1, Calendar.MINUTE),
            createDateDifferingBy(now(), End.LEVEL_8, Calendar.HOUR)),
    LEVEL_7(createDateDifferingBy(now(), Start.LEVEL_7, Calendar.HOUR),
            createDateDifferingBy(now(), Start.LEVEL_8 *60-1, Calendar.MINUTE), LEVEL_8.begin),
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
    LEVEL_1(now(),
            createDateDifferingBy(now(), Start.LEVEL_2 *60-1, Calendar.MINUTE), LEVEL_2.begin);

    public static class Start {
        public static int LEVEL_2 = 4;
        public static int LEVEL_3 = 24;
        public static int LEVEL_4 = 2*24;
        public static int LEVEL_5 = 4*24;
        public static int LEVEL_6 = 8*24;
        public static int LEVEL_7 = 16*24;
        public static int LEVEL_8 = 32*24;
    }
    public static class End {
        public static int LEVEL_1 = Start.LEVEL_2;
        public static int LEVEL_8 = 64*24;
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
