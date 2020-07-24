package com.qianlima.demo.dubbo;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Description:
 * dubbo 多协议: http://dubbo.apache.org/zh-cn/blog/dubbo-rest.html
 * dubbo 注册中心为nacos: http://dubbo.apache.org/zh-cn/docs/user/references/registry/nacos.html
 * 坑的点: 官方demo使用 dependencyManagement 解决依赖的resteasy版本号，这可能与本地的boot|cloud 父依赖冲突!! 废弃一个ManageMent并使用指定的版本号 solve!!
 * @Author Lun_qs
 * @Date 2019/12/26 9:57
 * @Version 0.0.1
 */
@EnableDubbo
@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class,args).registerShutdownHook();
    }
}