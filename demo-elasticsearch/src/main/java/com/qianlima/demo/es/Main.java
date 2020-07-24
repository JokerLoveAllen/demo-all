package com.qianlima.demo.es;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Java 高级Rest API使用
 * @See https://www.elastic.co/guide/en/elasticsearch/client/java-rest/master/index.html
 * @Author Lun_qs
 * @Date 2019/9/4 15:19
 * @Version 0.0.1
 */
@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class).registerShutdownHook();
    }
}
