package me.forrest.commonlib.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtil {

    /*
     * 将时间戳转换为时间(MS) yyyy-MM-dd HH:mm:ss
     */
    public static String stampToString(String stamp){
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        long lt = Long.valueOf(stamp);
        Date date = new Date(lt);
        res = simpleDateFormat.format(date);
        return res;
    }

    /**
     * 将时间戳转换为时间(MS) yyyy-MM-dd HH:mm:ss
     * @param stamp (MS)
     * @return
     */
    public static String stampToString(long stamp) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date(stamp);
        return simpleDateFormat.format(date);
    }

    /**
     * 将消耗的毫秒数 格式化成：1:20:30
     * @param ms (MS)
     * @return
     */
    public static String msToHms(int ms) {
        int totalSeconds = ms / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        if (hours > 0) {
            return String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.US, "%d:%02d", minutes, seconds);
        }
    }

    public static String currTimeToString() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        Date date = new Date();
        return simpleDateFormat.format(date);
    }

}
