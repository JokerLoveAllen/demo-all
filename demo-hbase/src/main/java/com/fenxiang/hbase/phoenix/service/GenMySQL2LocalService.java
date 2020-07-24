package com.fenxiang.hbase.phoenix.service;

import com.alibaba.fastjson.JSON;
import com.fenxiang.hbase.phoenix.batch.MysqlJdbc;
import com.fenxiang.hbase.phoenix.domain.GoodsPushRecord;
import com.fenxiang.hbase.phoenix.utils.SnowFlakeGeneratorBatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName GenMySQL2LocalService
 * @Author lqs
 * @Date 2020/4/20 14:14
 */
@Slf4j
@Service
public class GenMySQL2LocalService {
    @Value("${msql.file.prefix:/data/hbase/batch_file/}")
    private String filePathPrefix;
     final MysqlJdbc mysqlJdbc = new MysqlJdbc(30_000);
     final String fileSplitor = ",";
    private  ThreadPoolExecutor sqlTasks;
    private  String fileNameFmt = "%s_%d.csv";
    private  CountDownLatch countDownLatch;
    public GenMySQL2LocalService(){
        int cnt = Runtime.getRuntime().availableProcessors() * 2 - 1;
        sqlTasks = new ThreadPoolExecutor(cnt, cnt, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    }
    public void run(int offset, int limit) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            final List<String> strings = mysqlJdbc.batchTable(offset, limit);
            countDownLatch = new CountDownLatch(strings.size());
            for (String item : strings) {
                sqlTasks.execute(()-> {
                    try {
                        genRange("20191224",item,1,500_000,0);
                    } catch (Exception e) {
                        log.error("{} 异常",item,e);
                    }
                });
            }
            countDownLatch.await();
        } catch (Throwable e) {
            log.error("have exp !!", e);
        }finally {
            sqlTasks.shutdown();
            stopWatch.stop();
            long second = stopWatch.getTime() / 1000; // second
            int minute = 60; //60 s
            int hour = 60 * minute; // 60 minute
            int day = 24 * hour; // 24 hour
            int runDay = (int)second/day;
            int runHour = (int)(second%day)/hour;
            int runMinute = (int)(second%hour)/ minute;
            log.warn("程序运行{}天{}时{}分", runDay, runHour, runMinute);
        }
    }

    private  void genRange(String startTime,String tablename,int push_goods_id, int total, int fileIdx) throws Exception{
        int writeCnt = 0;
        String dateString = tablename.substring(11);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        long currStamp = sdf.parse(dateString).getTime();
        long startStamp = sdf.parse(startTime).getTime();
        String fileName = String.format(fileNameFmt, dateString, fileIdx);
        SnowFlakeGeneratorBatch generator = new SnowFlakeGeneratorBatch(startStamp, currStamp, 1, 1);
        MysqlJdbc.ResultCursor<List<GoodsPushRecord>> resultCursor = mysqlJdbc.batchGoods(tablename,0,push_goods_id);
        while(CollectionUtils.isNotEmpty(resultCursor.getData())){
            log.info("tablename:({}) offset:({})",tablename, resultCursor.getOffset());
            List<GoodsPushRecord> cursorData = resultCursor.getData();
            List<String> iterable = new ArrayList<>();
            if(CollectionUtils.isNotEmpty(cursorData)){
                for (GoodsPushRecord item : cursorData) {
                    if(writeCnt >= total){break;}
                    writeCnt++;
                    StringBuilder sb = new StringBuilder();
                    item.setUnique_id(generator.nextId());
                    sb.append(item.getUnique_id()).append(fileSplitor)
                            .append(item.getGoods_push_id()).append(fileSplitor)
                            .append(item.getMessage_id()).append(fileSplitor)
                            .append(item.getUser_id()).append(fileSplitor)
                            .append(item.getRobot_id()).append(fileSplitor)
                            .append(item.getGroup_id()).append(fileSplitor)
                            .append(value(item.getSku_id())).append(fileSplitor)
                            .append(JSON.toJSONString(value(item.getSku_name()))).append(fileSplitor)
                            .append(JSON.toJSONString(value(item.getMaterial_url()))).append(fileSplitor)
                            .append(value(item.getJd_price())).append(fileSplitor)
                            .append(JSON.toJSONString(value(item.getImage_url()))).append(fileSplitor)
                            .append(value(item.getPin_gou_price())).append(fileSplitor)
                            .append(value(item.getCoupon_price())).append(fileSplitor)
                            .append(JSON.toJSONString(value(item.getCoupon_url()))).append(fileSplitor)
                            .append(value(item.getCoupon_discount())).append(fileSplitor)
                            .append(JSON.toJSONString(value(item.getCommission()))).append(fileSplitor)
                            .append(JSON.toJSONString(value(item.getReasons()))).append(fileSplitor)
                            .append(JSON.toJSONString(value(item.getPromotion_text()))).append(fileSplitor)
                            .append(value(item.getSource())).append(fileSplitor)
                            .append(value(item.getError_msg())).append(fileSplitor)
                            .append(JSON.toJSONString(value(item.getImg_url_List()))).append(fileSplitor)
                            .append(JSON.toJSONString(value(item.getCommission_share()))).append(fileSplitor)
                            .append(value(item.getType())).append(fileSplitor)
                            .append(value(item.getStatus())).append(fileSplitor)
                            .append(date(item.getCreated())).append(fileSplitor)
                            .append(date(item.getModified()));
                    iterable.add(sb.toString());
                }
                File f = new File(filePathPrefix + dateString);
                if(!f.exists()){
                    f.mkdir();
                }
                Files.write(Paths.get(filePathPrefix + dateString,fileName), iterable, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
            }
            if(resultCursor.getOffset() > 0 && writeCnt < total){
                resultCursor = mysqlJdbc.batchGoods(tablename,resultCursor.getOffset(),resultCursor.getGoods_push_id());
            } else {
                break;
            }
        }
        if(resultCursor.getOffset() > 0 && writeCnt >= total){
            genRange(startTime, tablename, push_goods_id + total + 1, total, fileIdx + 1);
        } else { // quit stack operation
            countDownLatch.countDown();
        }
    }
    private String value(Object s){
        if(Objects.isNull(s)){
            return "";
        }
        String ss = s.toString();
        return StringUtils.isBlank(ss) ? "" : ss.trim();
    }
    private String date(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if(date == null){
            return "";
        }
        return sdf.format(date);
    }
}
