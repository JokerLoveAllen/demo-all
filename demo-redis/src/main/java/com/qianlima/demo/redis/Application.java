package com.qianlima.demo.redis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * @Description
 * @Author Lun_qs
 * @Date 2019/7/11 11:41
 * @Version 0.0.1
 */
@ComponentScan(basePackages = {"com.qianlima.demo.redis.config"})
@EnableAutoConfiguration
@SpringBootConfiguration
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class,args).registerShutdownHook();
    }
}
