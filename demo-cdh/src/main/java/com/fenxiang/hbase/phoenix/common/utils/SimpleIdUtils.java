package com.fenxiang.hbase.phoenix.common.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @ClassName SimpleIdUtils
 * @Author lqs
 * @Date 2020/4/28 16:54
 */
public class SimpleIdUtils {
    private static final String ID_RULE = "%s000000";
    private static final int LIMIT = 20_0000;
    private static final DateTimeFormatter mmHHddMMyyyy = DateTimeFormatter.ofPattern("mmHHddMMyyyy", Locale.CHINA);

    public static List<Long[]> day(int year, int month, int day){
        List<Long[]> res = new ArrayList<>();
        for(int h = 0; h < 24; h++){
            res.addAll(hour(year, month, day, h));
        }
        return res;
    }

    public static List<Long[]> hour(int year, int month, int day, int hour){
        List<Long[]> res = new ArrayList<>();
        for(int m = 0; m < 60; m++){
            res.addAll(minute(year,month,day,hour,m));
        }
        return res;
    }

    /**
     *
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     * @return 1分钟内[开始,截止]
     */
    public static List<Long[]> minute(int year, int month, int day, int hour, int minute){
        String fmt;
        if(year==2020 && month < 6 && day < 10){
            String mmonth = month < 10 ? "0" + month: "" + month;
            String mhour = hour < 10 ? "0" + hour : "" + hour;
            fmt = minute + mhour + day + mmonth + year;
        } else {
            fmt = LocalDateTime.of(year, month, day, hour, minute).format(mmHHddMMyyyy);
        }
        long offset = Long.parseLong(String.format(ID_RULE, fmt));
        return new ArrayList<Long[]>(){{
            for(long i =0; i + LIMIT < 1000000; i += LIMIT){
                this.add(new Long[]{offset + i, offset + i + LIMIT - 1});
            }
        }};//new Long[]{offset,offset + LIMIT};
    }
}
