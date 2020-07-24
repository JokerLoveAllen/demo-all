package com.fenxiang.demo.es;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * @ClassName App
 * @Author lqs
 * @Date 2020/5/6 16:07
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class,args).registerShutdownHook();
    }
}
