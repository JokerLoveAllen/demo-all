package com.qianlima.demo.commandline.cfg;

import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @Description
 * @Author Lun_qs
 * @Date 2019/7/15 15:40
 * @Version 0.0.1
 */
@Slf4j
@Component
@ConfigurationProperties(prefix="com.qianlima")
public class CommonCfg {
    @Getter
    private String info;

    public void setInfo(String info){
      log.warn("current info:{}", info);
    }
}
