package com.fenxiang.hbase.phoenix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App {
    public static void main(String[] args) {
        System.setProperty("hadoop.home.dir", "D:/Code/Phoenix/");
        SpringApplication.run(App.class, args);
    }
}
