package com.fenxiang.hbase.phoenix.batch;

import com.fenxiang.hbase.phoenix.SpringBootPhoenixTest;
import com.fenxiang.hbase.phoenix.dao.mysql.goods.GoodsMapper;
import com.fenxiang.hbase.phoenix.dao.phoenix.PhoenixMapper;
import com.fenxiang.hbase.phoenix.domain.GoodsPushRecord;
import com.fenxiang.hbase.phoenix.domain.SearchKeyword;
import com.fenxiang.hbase.phoenix.service.KeywordSuggestEsRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

@Slf4j
public class PhoenixBatchTest extends SpringBootPhoenixTest {
//    @Autowired
//    GoodsMapper goodsMapper;
    @Autowired
    PhoenixMapper phoenixMapper;

//    @Test
//    public void upsertTest(){
//        System.setProperty("hadoop.home.dir", "D:/Code/Phoenix/");
//        List<GoodsPushRecord> lists = goodsMapper.list("goods_push_20200331", 1000, 11);
//        //System.out.println(lists);
//        lists.parallelStream().forEach(phoenixMapper::insertOne);
//    }
    @Autowired
    KeywordSuggestEsRepository keywordSuggestEsRepository;

    LinkedBlockingQueue<String> blockingQueue = new LinkedBlockingQueue<>(2048);

    CountDownLatch countDownLatch = new CountDownLatch(4);

    @Test
    public void getKey() throws Throwable{
        AtomicInteger atomicInteger = new AtomicInteger();
        for(int i = 0; i < 4; i++){
            new Thread(()->{
                List<String> tmp = new ArrayList<>();
                try {
                    TimeUnit.SECONDS.sleep(40);
                    int f = 0;
                    while(f < 3){
                        while (tmp.size() < 1024 && f < 3){
                            final String poll = blockingQueue.poll(10, TimeUnit.SECONDS);
                            if(poll == null){
                                f++;
                            }else{
                                tmp.add(poll);
                            }
                        }
                        log.info("{}提交({})条数据",Thread.currentThread().getName(), tmp.size());
                        keywordSuggestEsRepository.bulk_insert(tmp);
                        atomicInteger.addAndGet(tmp.size());
                        tmp.clear();
                    }
                }catch (Exception e){
                    log.error("{} exit:", Thread.currentThread().getName(),e);
                }finally {
                    countDownLatch.countDown();
                }
            },"worker" + i).start();
        }
        final List<SearchKeyword> list = phoenixMapper.keywordSearch(null);
        log.info("获取到数据...");
        list.parallelStream().forEach(
                e->{
                    for(long c = Long.parseLong(e.getCnt()); c-- >0;){
                        try {
                            String kw = StringUtils.trim(e.getKeyword());
                            int length = StringUtils.length(kw);
                            if(StringUtils.isNumericSpace(kw) || length == 0 || length > 10 ){
                                continue;
                            }
                            blockingQueue.put(kw);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
        );
        countDownLatch.await();
        log.info("程序结束! 抓取到 ({})条数据",atomicInteger.get());
    }
}
