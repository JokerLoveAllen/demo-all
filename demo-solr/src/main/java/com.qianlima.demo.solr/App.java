package com.qianlima.demo.solr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Description
 * @Author Lun_qs
 * @Date 2019/12/18 10:59
 * @Version 0.0.1
 */
@SpringBootApplication
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class,args).registerShutdownHook();
    }
}
