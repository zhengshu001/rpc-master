package com.hualala.core.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by xiangbin on 2016/8/29.
 */
public class DateUtils {
    public static String getCurrentDateTime() {
        return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    }


    public static long getCurrentDateTimeLong() {
        return Long.parseLong(getCurrentDateTime());
    }

    public static String formatCurrnetDate(String pattern) {
        return new SimpleDateFormat(pattern).format(new Date());
    }

    public static String getCurrentDateTime(int secord) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, secord);
        return new SimpleDateFormat("yyyyMMddHHmmss").format(calendar.getTime());
    }

}
