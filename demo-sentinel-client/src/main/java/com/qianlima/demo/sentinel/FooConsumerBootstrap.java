/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qianlima.demo.sentinel;
import com.alibaba.csp.sentinel.slots.block.SentinelRpcException;
import com.qianlima.demo.sentinel.consumer.ConsumerConfiguration;
import com.qianlima.demo.sentinel.consumer.FooServiceConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Random;
import java.util.concurrent.*;

/**
 * Please add the following VM arguments:
 * <pre>
 * -Djava.net.preferIPv4Stack=true
 * -Dcsp.sentinel.dashboard.server=192.168.30.13:8803
 * -Dcsp.sentinel.api.port=8721
 * -Dproject.name=dubbo-consumer-demo
 * </pre>
 *
 * @author Eric Zhao
 */
@Slf4j
public class FooConsumerBootstrap {

    public static void main(String[] args) throws Exception{
        AnnotationConfigApplicationContext consumerContext = new AnnotationConfigApplicationContext();
        consumerContext.register(ConsumerConfiguration.class);
        consumerContext.refresh();

        FooServiceConsumer service = consumerContext.getBean(FooServiceConsumer.class);
        final int[] c = {0};
        int cnt = 15;
        final int sleepBound = 1200;
        final int taskCnt = 1000;
        CountDownLatch latch = new CountDownLatch(cnt);
        ThreadPoolExecutor es = new ThreadPoolExecutor(cnt, cnt, 5,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
                (r) -> new Thread(r, String.format("consume-thread-%d", c[0]++)),
                new ThreadPoolExecutor.CallerRunsPolicy());
        while(cnt-- > 0){
            es.execute(()->{
                Random randomInThread = new Random(System.currentTimeMillis());
                for(int i =0; i< taskCnt; i++){
                    try {
                        TimeUnit.MILLISECONDS.sleep(randomInThread.nextInt(sleepBound));
                        String message = service.sayHello("Joker");
//                        log.info("{}", message);
                        service.doAnother();
                    } catch (SentinelRpcException ex) {
                        log.warn("Block");
                    } catch (Exception ex) {
                        log.error("干,sentinel 拒绝服务");
                    }
                }
                latch.countDown();
            });
        }
        latch.await();
        log.error("完成!!!!!!");
        es.shutdown();
    }
}
