package com.qianlima.demo.commandline;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;

/**
 * @Description 命令行参数, 在spring boot 容器中享有最高优先级, 其次是bootstarp.*
 * @Author Lun_qs
 * @Date 2019/7/15 15:35
 * @Version 0.0.1
 */
@Slf4j
@SpringBootApplication
public class Application implements CommandLineRunner {
    public static void main(String[] args) {
        log.warn("参数集合:{}", Arrays.toString(args));
        SpringApplication.run(Application.class,args);
    }

    /**
     * 实现CommandLineRunner可以在启动时获取命令行参数
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        if(args != null&&args.length>0){
            for(int i =0;i<args.length;i++){
                log.warn("args[{}] = {}",i, args[i]);
            }
        }
    }
}
