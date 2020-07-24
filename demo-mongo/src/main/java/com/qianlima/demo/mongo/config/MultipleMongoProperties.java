package com.qianlima.demo.mongo.config;

import lombok.Data;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Description
 * @Author Lun_qs
 * @Date 2019/12/24 17:13
 * @Version 0.0.1
 */
@Data
//@ConfigurationProperties(prefix = "spring.data.mongodb")
public class MultipleMongoProperties {
    private MongoProperties primary = new MongoProperties();
    private MongoProperties secondary = new MongoProperties();
}
