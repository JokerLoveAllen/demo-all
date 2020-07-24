package com.fenxiang.rabbit.testsender;

import com.fenxiang.rabbit.RootTest;
import com.fenxiang.rabbit.consume.RabbitSender;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName Sender
 * @Author zy
 * @Date 2020/4/16 20:17
 */
public class Sender extends RootTest {
    @Autowired
    RabbitSender rabbitSender;
    @Test
    public void test0() throws Exception {
        rabbitSender.send("i love you dont love me",new HashMap<>());
        TimeUnit.SECONDS.sleep(5);
    }
}
