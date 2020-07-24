package com.fenxiang.hbase.phoenix.common.utils;

import java.util.Calendar;

/**
 * Rule: (mmHHddMMyyyy + 0 - 999999)
 * @ClassName SimpleIdGenerator
 * @Author lqs
 * @Date 2020/4/21 19:33
 */
public class SimpleIdGeneratorBatch {

    /**
     * 每一部分占用的位数
     */
    private final long SEQUENCE_BIT = 20; //序列号占用的位数

    /**
     * 每一部分的最大值：先进行左移运算，再同-1进行异或运算；异或：相同位置相同结果为0，不同结果为1
     */
    private final long MAX_SEQUENCE = -1L ^ (-1L << SEQUENCE_BIT);
    
    private final long MINUTE_IN_MILLS = 60 * 1000;
    
    private long sequence = 0L; //序列号
    private long lastMin = -1L;//上一次时间戳

    private final long currDay;

    public SimpleIdGeneratorBatch(String cd){
        //20200421
        cd = cd.substring(6) + cd.substring(4,6) + cd.substring(0,4);
        currDay = Long.parseLong(cd);
    }
    /**
     * 产生下一个ID
     *
     * @return
     */
    public synchronized long nextId() {
        /** 获取当前时间戳 */
        long currMin = getNewMin();
        /** 如果当前时间戳小于上次时间戳则抛出异常 */
        if (currMin < lastMin) {
            throw new RuntimeException("Clock moved backwards.  Refusing to generate id");
        }
        /** 相同分钟内 */
        if (currMin == lastMin) {
            //相同分钟内，序列号自增
            sequence = (sequence + 1) & MAX_SEQUENCE;
            //同一毫秒的序列数已经达到最大
            if (sequence == 0L) {
                /** 获取下一时间的时间戳并赋值给当前时间戳 */
                currMin = getNextMin();
            }
        } else {
            //不同分钟内，序列号置为0
            sequence = 0L;
        }
        /** 当前时间戳存档记录，用于下次产生id时对比是否为相同时间戳 */
        lastMin = currMin;
        return (getFmtLong()) + sequence;    //序列号部分
    }

    private long getNextMin() {
        long mill = getNewMin();
        while (mill <= lastMin) {
            mill = getNewMin();
        }
        return mill;
    }

    private long getNewMin() {
        return (System.currentTimeMillis() / MINUTE_IN_MILLS);
    }

    private final String FMT = "%d%s%d000000";

    private long getFmtLong(){
        final Calendar calendar = Calendar.getInstance();
        //%d%d%d000000%d
        int tmpHour = calendar.get(Calendar.HOUR_OF_DAY);
        String hour = tmpHour <10 ? "0" + tmpHour : tmpHour+"";
        return Long.parseLong(String.format(FMT,calendar.get(Calendar.MINUTE), hour, currDay));
    }

}
