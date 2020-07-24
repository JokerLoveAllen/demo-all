package com.fenxiang.rabbit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * RabbitMq
 * 1 在Rabbit GUI Admin子条目下 新建一个 Virtual host,
 * 2 用第一步的 host 在Rabbit GUI新建一个 Exchanges ,绑定相关账户
 * 3 用第一步的 host 在创建一个Queues, 并绑定第二步的 Exchange
 * @ClassName App
 * @Author lqs
 * @Date 2020/4/16 17:39
 */
@SpringBootApplication
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class,args);
    }
}
