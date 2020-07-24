package com.fenxiang.hbase.phoenix.mysql;

import com.alibaba.fastjson.JSON;
import com.fenxiang.hbase.phoenix.batch.MysqlJdbc;
import com.fenxiang.hbase.phoenix.domain.GoodsPushRecord;
import com.fenxiang.hbase.phoenix.utils.SnowFlakeGeneratorBatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;

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
 * @ClassName GenCsvFileTest
 * @Author lqs
 * @Date 2020/4/20 9:52
 */
@Slf4j
public class GenCsvFileTest {
    static final String filePathPrefix = "D:\\Hbase_Release\\batch_file\\";
    static final MysqlJdbc mysqlJdbc = new MysqlJdbc(30_000);
    static final String fileSplitor = ",";
    private static ThreadPoolExecutor sqlTasks;
    private static String fileNameFmt = "%s_%d.csv";
    private static CountDownLatch countDownLatch;
    public static void main(String[] args) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            int cnt = Runtime.getRuntime().availableProcessors()*2-1;
            sqlTasks = new ThreadPoolExecutor(cnt, cnt, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
            final List<String> strings = mysqlJdbc.batchTable(0, 3);
            countDownLatch = new CountDownLatch(strings.size());
            for (String item : strings) {
                sqlTasks.execute(()-> {
                    try {
                        genRange("20191224",item,0,500_000,0);
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

    private static void genRange(String startTime,String tablename,int push_goods_id, int total, int fileIdx) throws Exception{
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
                    /**
                     * 	unique_id BIGINT not null
                     * 	, goods_push_id integer
                     *     , message_id VARCHAR(64)
                     * 	, user_id integer
                     * 	, robot_id integer
                     * 	, group_id integer
                     * 	, sku_id CHAR(32)
                     * 	, sku_name VARCHAR(512)
                     * 	, material_url VARCHAR(1024)
                     * 	, jd_price VARCHAR(255)
                     * 	, image_url VARCHAR(1024)
                     * 	, pin_gou_price VARCHAR(255)
                     * 	, coupon_price VARCHAR(255)
                     * 	, coupon_url VARCHAR(1024)
                     * 	, coupon_discount CHAR(255)
                     * 	, commission VARCHAR(255)
                     * 	, reasons VARCHAR(1024)
                     * 	, promotion_text VARCHAR(2000)
                     * 	, source VARCHAR(255)
                     * 	, error_msg VARCHAR(255)
                     * 	, img_url_List VARCHAR(1024)
                     * 	, commission_share VARCHAR(255)
                     * 	, type	TINYINT
                     * 	, status	TINYINT
                     * 	, created	DATE
                     * 	, modified	DATE
                     */
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
    private static String value(Object s){
        if(Objects.isNull(s)){
            return "";
        }
        String ss = s.toString();
        return StringUtils.isBlank(ss) ? "" : ss.trim();
    }
    private static String date(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if(date == null){
            return "";
        }
        return sdf.format(date);
    }
}
