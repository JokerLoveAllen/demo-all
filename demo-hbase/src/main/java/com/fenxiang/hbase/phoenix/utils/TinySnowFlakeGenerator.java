package com.fenxiang.hbase.phoenix.utils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;

/**
 * @ClassName TinySnowFlakeGenerator
 * @Author lqs
 * @Date 2020/6/6 10:50
 */
public class TinySnowFlakeGenerator {
    private final long START_STMP;

    private final long SEQUENCE_BIT = 12; //序列号占用的位数
    private final long MACHINE_BIT = 3;  //机器标识占用的位数


    /** 用位运算计算出最大支持的机器数量：31 */
    private final long MAX_MACHINE_NUM = -1L ^ (-1L << MACHINE_BIT);

    /** 用位运算计算出12位能存储的最大正整数：4095 */
    private final long MAX_SEQUENCE = -1L ^ (-1L << SEQUENCE_BIT);


    /** 机器标志较序列号的偏移量 */
    private final long MACHINE_LEFT = SEQUENCE_BIT;

    /** 时间戳较数据中心的偏移量 */
    private final long TIMESTMP_LEFT = SEQUENCE_BIT + MACHINE_BIT;

    private long machineId;    //机器标识
    private long sequence = 0L; //序列号
    private long lastStmp = -1L;//上一次时间戳

    public TinySnowFlakeGenerator(long startStamp, long machineId){
        this.START_STMP = startStamp;
        this.machineId = machineId;
    }

    /**
     * 产生下一个ID
     * @return
     */
    public synchronized long nextId() {
        long offset = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).toInstant(ZoneOffset.of("+8")).toEpochMilli();

        /** 获取当前时间戳 */
        long currStmp = getNewstmp();
        /** 如果当前时间戳小于上次时间戳则抛出异常 */
        if (currStmp < lastStmp) {
            throw new RuntimeException("Clock moved backwards.  Refusing to generate id");
        }
        /** 相同毫秒内 */
        if (currStmp == lastStmp) {
            //相同毫秒内，序列号自增
            sequence = (sequence + 1) & MAX_SEQUENCE;
            //同一毫秒的序列数已经达到最大
            if (sequence == 0L) {
                /** 获取下一时间的时间戳并赋值给当前时间戳 */
                currStmp = getNextMill();
            }
        } else {
            //不同毫秒内，序列号置为0
            sequence = 0L;
        }
        /** 当前时间戳存档记录，用于下次产生id时对比是否为相同时间戳 */
        lastStmp = currStmp;

        System.out.println(new SimpleDateFormat("yyyyMMdd HH mm ss").format(offset));
        return (currStmp - offset) << TIMESTMP_LEFT //时间戳部分
                | machineId << MACHINE_LEFT            //机器标识部分
                | sequence;                            //序列号部分
    }

    private long getNextMill() {
        long mill = getNewstmp();
        while (mill <= lastStmp) {
            mill = getNewstmp();
        }
        return mill;
    }

    private long getNewstmp() {
        return LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
    }
}
