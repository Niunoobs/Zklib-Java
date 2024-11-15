package zklibjs.java;

import java.util.Calendar;
import java.util.Date;

public class timeParser {

    public static Date decode(long time) {
        int second = (int) (time % 60);
        time = (time - second) / 60;

        int minute = (int) (time % 60);
        time = (time - minute) / 60;

        int hour = (int) (time % 24);
        time = (time - hour) / 24;

        int day = (int) (time % 31) + 1;
        time = (time - (day - 1)) / 31;

        int month = (int) (time % 12);
        time = (time - month) / 12;

        int year = (int) time + 2000;

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute, second);
        return calendar.getTime();
    }

    public static long encode(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return (((calendar.get(Calendar.YEAR) % 100) * 12 * 31 +
                calendar.get(Calendar.MONTH) * 31 +
                calendar.get(Calendar.DAY_OF_MONTH) - 1) *
                (24 * 60 * 60) +
                (calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)) * 60 +
                calendar.get(Calendar.SECOND));
    }
}

