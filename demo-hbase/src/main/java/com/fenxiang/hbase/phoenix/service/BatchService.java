package com.fenxiang.hbase.phoenix.service;

import com.fenxiang.hbase.phoenix.batch.MysqlJdbc;
import com.fenxiang.hbase.phoenix.batch.PhoenixJdbc;
import com.fenxiang.hbase.phoenix.domain.GoodsPushRecord;
import com.fenxiang.hbase.phoenix.utils.SnowFlakeGeneratorBatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
//@Service
public class BatchService {
    private  PhoenixJdbc[] POOLS;
    private  ThreadPoolExecutor sqlTasks, hbaseTasks;
    private  final String startTime = "20191224";
    private  final Integer commitSize = 1000, sqlLimit = 30000;
    private  final Logger batchLogger = LoggerFactory.getLogger("batchLogger");
    private  final Integer datacenterId = 1, machineId = 1;
    private  final MysqlJdbc mysqlJdbc = new MysqlJdbc(sqlLimit);
    private CountDownLatch countDownLatch;
    public BatchService() throws Throwable{
        int processors = Runtime.getRuntime().availableProcessors();
        {
            int hbaseCnt = processors * 2 - 1;
            POOLS = new PhoenixJdbc[hbaseCnt];
            int[] flags = {0};
            for(int i = 0;i<POOLS.length;i++){
                POOLS[i] = new PhoenixJdbc(commitSize);
            }
            hbaseTasks = new ThreadPoolExecutor(hbaseCnt, hbaseCnt, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "thread:" + (flags[0]++));
                }
            });
        }

        {
            sqlTasks = new ThreadPoolExecutor(processors/2, processors/2, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        }
    }

    public void run(int offset, int limit){
        countDownLatch = new CountDownLatch(limit);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            mysqlJdbc.batchTable(offset,limit).forEach(e->{
                sqlTasks.execute(()->{
                    try {
                        batchTask(startTime,e); // "goods_push_20200318"
                    } catch (Throwable throwable) {
                        log.error("执行({})失败", e, throwable);
                    }
                });
            });
            //等待一致性条件
            countDownLatch.await();
        }catch (Throwable e){
            e.printStackTrace();
        }finally {
            stopWatch.stop();
            long second = stopWatch.getTime() / 1000; // second
            int minute = 60; //60 s
            int hour = 60 * minute; // 60 minute
            int day = 24 * hour; // 24 hour
            int runDay = (int)second/day;
            int runHour = (int)(second%day)/hour;
            int runMinute = (int)(second%hour)/ minute;
            batchLogger.warn("程序运行{}天{}时{}分", runDay,runHour, runMinute);
        }
    }

    public void free(){
        try {
            if(sqlTasks.getActiveCount() > 0 && hbaseTasks.getActiveCount() > 0){
                return;
            }

            for(int i = 0; i<POOLS.length; i++){
                if(POOLS[i]!=null){
                    POOLS[i].close();
                }
            }
            sqlTasks.shutdown();
            hbaseTasks.shutdown();
        } catch (Throwable e) {
            log.error("释放异常:",e);
        }
    }

    private void batchTask(String startTime,String tablename) throws Throwable{

        String dateString = tablename.substring(11);
        batchLogger.info("(goods_push_{})执行开始",dateString);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        long currStamp = sdf.parse(dateString).getTime();
        long startStamp = sdf.parse(startTime).getTime();
        SnowFlakeGeneratorBatch generator = new SnowFlakeGeneratorBatch(startStamp, currStamp, datacenterId, machineId);

        MysqlJdbc.ResultCursor<List<GoodsPushRecord>> resultCursor = mysqlJdbc.batchGoods(tablename,0,0);
        while(CollectionUtils.isNotEmpty(resultCursor.getData())){
            log.info("tablename:({}) offset:({})",tablename, resultCursor.getOffset());
            List<GoodsPushRecord> cursorData = resultCursor.getData();
            if(CollectionUtils.isNotEmpty(cursorData)){
                int idx = 0;
                cursorData.forEach(e->e.setUnique_id(generator.nextId()));
                List<List<GoodsPushRecord>> tasks = new ArrayList<>();
                while(idx + 3000 < cursorData.size()){
                    tasks.add(cursorData.subList(idx, idx += 3000));
                }
                if(idx<cursorData.size()){
                    tasks.add(cursorData.subList(idx,cursorData.size()));
                }
                int resultCursorOffset = resultCursor.getOffset();
                int resultCursorGoods_push_id = resultCursor.getGoods_push_id();
                for(List<GoodsPushRecord> item : tasks){
                    hbaseTasks.execute(()->{
                        POOLS[Integer.valueOf(Thread.currentThread().getName().split(":")[1])].executeBatch(tablename,
                                resultCursorOffset,
                                resultCursorGoods_push_id,
                                item);
                    });
                }
            }
            if(resultCursor.getOffset() > 0){
                resultCursor = mysqlJdbc.batchGoods(tablename,resultCursor.getOffset(),resultCursor.getGoods_push_id());
            } else {
                break;
            }
        }
        batchLogger.info("(goods_push_{})执行完毕",dateString);
        countDownLatch.countDown();
    }
}