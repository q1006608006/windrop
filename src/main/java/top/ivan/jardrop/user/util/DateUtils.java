package top.ivan.jardrop.user.util;

/**
 * @author Ivan
 * @since 2024/03/20 15:11
 */
public class DateUtils {
    private static final int DAY_SECOND = 60 * 60 * 24;
    private static final int HOUR_SECOND = 60 * 60;
    private static final int MINUTE_SECOND = 60;

    public static String secondToDateDuration(int second) {
        int day = second / DAY_SECOND;
        int hour = (second % DAY_SECOND) / HOUR_SECOND;
        int min = (second % HOUR_SECOND) / MINUTE_SECOND;
        int rest = second % MINUTE_SECOND;

        StringBuilder builder = new StringBuilder();
        if (day != 0) {
            builder.append(day).append("天");
        }
        if (hour != 0) {
            builder.append(hour).append("小时");
        }
        if (min != 0) {
            builder.append(min).append("分钟");
        }
        if (rest != 0 || builder.length() == 0) {
            builder.append(rest).append("秒");
        }
        return builder.toString();
    }
}
