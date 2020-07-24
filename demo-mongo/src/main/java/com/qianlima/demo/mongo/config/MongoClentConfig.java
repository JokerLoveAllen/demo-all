package com.qianlima.demo.mongo.config;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.MongoClientSettings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
 * @Description
 * @Author Lun_qs
 * @Date 2019/12/24 14:34
 * @Version 0.0.1
 */
@Slf4j
@Configuration
public class MongoClentConfig {
   @Bean(destroyMethod = "close")
   public static MongoClient getClient(){
       // mongodb://<username>:<password>@[host:port,]/test?w=majority
       ConnectionString connString = new ConnectionString("mongodb://192.168.30.12:27017/qlmdb");
       MongoClientSettings settings = MongoClientSettings.builder()
               .applyConnectionString(connString).retryWrites(true).build();
       MongoClient mongoClient = MongoClients.create(settings);
       return mongoClient;
   }
}
