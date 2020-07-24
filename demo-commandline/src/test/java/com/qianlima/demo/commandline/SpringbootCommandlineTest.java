package com.qianlima.demo.commandline;

import com.qianlima.demo.commandline.cfg.CommonCfg;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @Description
 * @Author Lun_qs
 * @Date 2019/7/15 15:43
 * @Version 0.0.1
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringbootCommandlineTest {

    @Autowired
    private CommonCfg commonCfg;

    @Test
    public void test(){
      log.warn("info: {} ",commonCfg.getInfo());
    }
}
