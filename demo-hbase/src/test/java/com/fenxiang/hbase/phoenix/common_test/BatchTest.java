//package com.fenxiang.hbase.phoenix.common_test;
//
//import com.fenxiang.hbase.phoenix.batch.MysqlJdbc;
//import com.fenxiang.hbase.phoenix.batch.PhoenixJdbc;
//import com.fenxiang.hbase.phoenix.domain.GoodsPushRecord;
//import com.fenxiang.hbase.phoenix.utils.SnowFlakeGeneratorBatch;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.collections.CollectionUtils;
//import org.apache.commons.lang.time.StopWatch;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.LinkedBlockingDeque;
//import java.util.concurrent.ThreadFactory;
//import java.util.concurrent.ThreadPoolExecutor;
//import java.util.concurrent.TimeUnit;
//
//@Slf4j
//public class BatchTest {
//    private static PhoenixJdbc[] POOLS;
//    private static ThreadPoolExecutor sqlTasks, hbaseTasks;
//    private static final String startTime = "20191224";
//    private static final Integer commitSize = 1000, sqlLimit = 30000;
//    private static final Logger batchLogger = LoggerFactory.getLogger("batchLogger");
//    private static final Integer datacenterId = 1, machineId = 1;
//    private static final MysqlJdbc mysqlJdbc = new MysqlJdbc(sqlLimit);
//
//    static {
//        try {
//            int processors = Runtime.getRuntime().availableProcessors();
//            {
//                int hbaseCnt = processors * 2 - 1;
//                POOLS = new PhoenixJdbc[hbaseCnt];
//                int[] flags = {0};
//                for(int i = 0;i<POOLS.length;i++){
//                    POOLS[i] = new PhoenixJdbc(commitSize);
//                }
//                hbaseTasks = new ThreadPoolExecutor(hbaseCnt, hbaseCnt, 0, TimeUnit.MICROSECONDS, new LinkedBlockingDeque<>(), new ThreadFactory() {
//                    @Override
//                    public Thread newThread(Runnable r) {
//                        return new Thread(r, "thread:" + (flags[0]++));
//                    }
//                });
//            }
//
//            {
//                sqlTasks = new ThreadPoolExecutor(processors/2, processors/2, 0, TimeUnit.MICROSECONDS, new LinkedBlockingDeque<>());
//            }
//
//        }catch (Throwable e){
//            throw new RuntimeException(e);
//        }
//    }
//
//    public static void main(String[] args) {
//        StopWatch stopWatch = new StopWatch();
//        stopWatch.start();
//        try {
//            sqlTasks.execute(()->{
//                mysqlJdbc.batchTable(0,10).forEach(e->{
//                    try {
//                        batchTask(startTime,e); // "goods_push_20200318"
//                    } catch (Throwable throwable) {
//                        log.error("执行({})失败",e,throwable);
//                    }
//                });
//            });
//        }catch (Throwable e){
//
//        }finally {
//            while(sqlTasks.getActiveCount() > 0 && hbaseTasks.getActiveCount() > 0){
//                try {
//                    TimeUnit.SECONDS.sleep(200);
//                    //log.info("total({}), 写入长度结果:({})",results.stream().mapToInt(e->e).sum(), results);
//
//                } catch (InterruptedException e) {
//                    log.error("中断异常:",e);
//                }
//            }
//
//            //log.info("total({}), 写入长度结果:({})",results.stream().mapToInt(e->e).sum(), results);
//
//            for(int i = 0;i<POOLS.length;i++){
//                if(POOLS[i]!=null){
//                    POOLS[i].close();
//                }
//            }
//            sqlTasks.shutdown();
//            hbaseTasks.shutdown();
//            stopWatch.stop();
//            long second = stopWatch.getTime() / 1000; // second
//            int minute = 60; //60 s
//            int hour = 60 * minute; // 60 minute
//            int day = 24 * hour; // 24 hour
//            int runDay = (int)second/day;
//            int runHour = (int)(second%day)/hour;
//            int runMinute = (int)(second%hour)/ minute;
//            log.warn("程序运行{}天{}时{}分", runDay,runHour, runMinute);
//        }
//    }
//    private static void batchTask(String startTime,String tablename) throws Throwable{
//
//        String dateString = tablename.substring(11);
//        batchLogger.info("(goods_push_{})执行开始",dateString);
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
//        long currStamp = sdf.parse(dateString).getTime();
//        long startStamp = sdf.parse(startTime).getTime();
//        SnowFlakeGeneratorBatch generator = new SnowFlakeGeneratorBatch(startStamp, currStamp, datacenterId, machineId);
//
//        MysqlJdbc.ResultCursor<List<GoodsPushRecord>> resultCursor = mysqlJdbc.batchGoods(tablename,0,0);
//        while(CollectionUtils.isNotEmpty(resultCursor.getData())){
//            log.info("offset:{}",resultCursor.getOffset());
//            List<GoodsPushRecord> cursorData = resultCursor.getData();
//            log.warn("数据长度: ({})",cursorData.size());
//            if(CollectionUtils.isNotEmpty(cursorData)){
//                int idx = 0;
//                cursorData.forEach(e->e.setUnique_id(generator.nextId()));
//                List<List<GoodsPushRecord>> tasks = new ArrayList<>();
//                while(idx + 3000 < cursorData.size()){
//                    tasks.add(cursorData.subList(idx, idx += 3000));
//                }
//                if(idx<cursorData.size()){
//                    tasks.add(cursorData.subList(idx,cursorData.size()));
//                }
//                int resultCursorOffset = resultCursor.getOffset();
//                int resultCursorGoods_push_id = resultCursor.getGoods_push_id();
//                for(List<GoodsPushRecord> item : tasks){
//                    hbaseTasks.execute(()->{
//                        POOLS[Integer.valueOf(Thread.currentThread().getName().split(":")[1])].executeBatch(tablename,
//                                resultCursorOffset,
//                                resultCursorGoods_push_id,
//                                item);
//                    });
//                }
//            }
//            if(resultCursor.getOffset() > 0){
//                resultCursor = mysqlJdbc.batchGoods(tablename,resultCursor.getOffset(),resultCursor.getGoods_push_id());
//            } else {
//                break;
//            }
//        }
//        batchLogger.info("(goods_push_{})执行完毕",dateString);
//    }
//}