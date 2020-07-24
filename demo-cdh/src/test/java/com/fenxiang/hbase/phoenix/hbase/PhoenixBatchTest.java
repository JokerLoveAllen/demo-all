package com.fenxiang.hbase.phoenix.hbase;

import com.fenxiang.hbase.phoenix.SpringBootPhoenixTest;
import com.fenxiang.hbase.phoenix.service.GoodsPushService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class PhoenixBatchTest extends SpringBootPhoenixTest {
    private @Autowired
    GoodsPushService goodsPushService;

    @Test
    public void test(){
        log.error("20200102 count:({})",goodsPushService.count(2020,1,2));
    }
}
