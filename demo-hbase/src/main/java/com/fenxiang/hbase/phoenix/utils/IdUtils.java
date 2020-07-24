package com.fenxiang.hbase.phoenix.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @ClassName IdUtils
 * @Author lqs
 * @Date 2020/4/15 19:55
 */
public class IdUtils {
    public static long getMinItemInHbase(String yyyyMMdd, long startStamp, long datacenterId, long matchineId){
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            long time = sdf.parse(yyyyMMdd).getTime();
            return (time - startStamp) << 22
                    | datacenterId << 17
                    | matchineId << 10;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1L;
    }

    public static long getMaxItemInHbase(String yyyyMMdd, long startStamp, long datacenterId, long matchineId){
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sdf.parse(yyyyMMdd));
            calendar.add(Calendar.DAY_OF_YEAR,1);
            long time = calendar.getTime().getTime();
            return (time - startStamp) << 22
                    | datacenterId << 17
                    | matchineId << 10
                    | 1;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1L;
    }

    public static String getFormatDate(long id , long startStamp){
        return new SimpleDateFormat("yyyyMMdd HH:mm:ss").format((id >>> 22) + startStamp);
    }

    public static int getDataCenterId(long id){
        id <<= 42;
        return (int)(id >> 59);
    }

    public static int getMachineId(long id){
        id <<= 47;
        return (int)(id >> 59);
    }
}