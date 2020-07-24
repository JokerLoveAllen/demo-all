package com.qianlima.demo.mongo.config;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;


//@Configuration
@RequiredArgsConstructor
//@EnableConfigurationProperties(MultipleMongoProperties.class)
public class MultipleMongoConfig {

    private final MultipleMongoProperties mongoProperties;

    @Primary
    @Bean(name = "primaryMongoTemplate")
    public MongoTemplate primaryMongoTemplate() throws Exception {
        return new MongoTemplate(primaryFactory(this.mongoProperties.getPrimary()));
    }

    @Bean(name = "secondaryMongoTemplate")
    public MongoTemplate secondaryMongoTemplate() throws Exception {
        return new MongoTemplate(secondaryFactory(this.mongoProperties.getSecondary()));
    }

    @Bean
    @Primary
    public MongoDbFactory primaryFactory(final MongoProperties mongo) throws Exception {
//        return new SimpleMongoDbFactory(new MongoClient(mongo.getHost(), mongo.getPort()), mongo.getDatabase());
        return new SimpleMongoDbFactory(new MongoClientURI(mongo.getUri()));
    }

    @Bean
    public MongoDbFactory secondaryFactory(final MongoProperties mongo) throws Exception {
        //简单访问 无账号密码
//        return new SimpleMongoDbFactory(new MongoClient(mongo.getHost(), mongo.getPort()), mongo.getDatabase());
        //复杂访问有集群信息 账号信息 乱八七糟的玩意使用 下面 uri 访问！
        return new SimpleMongoDbFactory(new MongoClientURI(mongo.getUri()));
    }

}