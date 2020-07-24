package com.qianlima.demo.mongo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 搭建授权：https://docs.mongodb.com/manual/tutorial/enable-authentication/
 * @Description
 * @Author Lun_qs
 * @Date 2019/7/11 11:41
 * @Version 0.0.1
 */
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class,args).registerShutdownHook();
    }
}
