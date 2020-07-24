package com.fenxiang.hbase.phoenix.common_test;

import com.fenxiang.hbase.phoenix.utils.IdUtils;
import com.fenxiang.hbase.phoenix.utils.SnowFlakeGeneratorBatch;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.stream.Stream;

@Slf4j
public class SnowFlakeGenTest {
    public static void main(String[] args) throws Exception{
//        final SnowFlakeGenerator snowFlakeGenerator = new SnowFlakeGenerator(System.currentTimeMillis(),1,1);
//        ThreadPoolExecutor executorService = new ThreadPoolExecutor(12,12,0, TimeUnit.MICROSECONDS,new LinkedBlockingQueue<>());
//        ConcurrentSet<Long> concurrentSet = new ConcurrentSet<>();
//        StopWatch stopWatch = new StopWatch();
//        stopWatch.start();
//        for(int i=0; i<1000000; i++){
//            executorService.execute(()->{
//                concurrentSet.add(snowFlakeGenerator.nextId());
//            });
//        }
//
//        while(concurrentSet.size() < 1000000){
////            try {
////                log.info("当前条数:{}",concurrentSet.size());
////                TimeUnit.SECONDS.sleep(1);
////            } catch (InterruptedException e) {
////                e.printStackTrace();
////            }
//        }
//        stopWatch.stop();
//        log.info("提交耗时 {} ms",stopWatch.getTime());
//        executorService.shutdown();
//        log.info("当前条数:{}",concurrentSet.size());

//        SnowFlakeGeneratorBatch generator = new SnowFlakeGeneratorBatch(1577116800000L, 1584460800000L,1, 1);
//        for(int i =0; i < 10; i++){
//            TimeUnit.MILLISECONDS.sleep(500);
//            generator.nextId();
//            TimeUnit.MILLISECONDS.sleep(500);
//            generator.nextId();
//        }
//        System.out.println(new SimpleDateFormat("yyyy-MM-dd").parse("2019-12-24").getTime());
//        System.out.println(new SimpleDateFormat("yyyyMMdd").format(new SimpleDateFormat("yyyy-MM-dd").parse("1970-01-01").getTime()+2199023255551L));
//
//        String dateString = "20200101", startTime = "20191224";
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
//        long currStamp = sdf.parse(dateString).getTime();
//        long startStamp = sdf.parse(startTime).getTime();
//        SnowFlakeGeneratorBatch generator = new SnowFlakeGeneratorBatch(startStamp, currStamp, 1, 1);
//
//        long nextId = generator.nextId();
//
//        long minItemInHbase = IdUtils.getMinItemInHbase(dateString, startStamp, 1, 1);
//
//        long maxItemInHbase = IdUtils.getMaxItemInHbase(dateString, startStamp, 1, 1);
//
//        log.info("{} , {} ,{} ",nextId,minItemInHbase,maxItemInHbase);
//
//        log.info("{}", IdUtils.getFormatDate(maxItemInHbase ,startStamp));
//
//        log.info("{}", IdUtils.getDataCenterId(nextId));
//
//        log.info("{} ", IdUtils.getMachineId(nextId));

//        log.info("{}" ,Long.toBinaryString(nextId));

        // Divide and conquer 分治法
        int res = Stream.of("a1", "b2", "c3", "d5", "e6")
                .parallel() // 使用ForkJoinPool切割成更小的Task
                .map(e -> Integer.valueOf(String.valueOf(e.charAt(1))))// 从输入字符中提取需要的整数
                .reduce(0, Integer::sum); //将所有的结果进行聚合
        System.out.println(res);

    }
}
