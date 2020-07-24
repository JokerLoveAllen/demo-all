package com.qianlima.demo.es.cfg;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description
 * @Author Lun_qs
 * @Date 2019/9/11 15:57
 * @Version 0.0.1
 */
@Slf4j
@Configuration
public class EsJavaRestApiConfig {

    private RestHighLevelClient client;

    @Bean(destroyMethod = "close")
    public RestHighLevelClient restHighLevelClient(){
        if(client == null){
            client = new RestHighLevelClient(
                    RestClient.builder(new HttpHost("152.136.194.249", 9700)));
//            //TODO: 这个RestHighLevelClient 实体一直在Spring 容器中存活? 直到被kill
//            Runtime.getRuntime().addShutdownHook(new Thread(()->{
//                try{
//                    client.close();
//                }catch (Exception e){
//                    log.error("when close have some problem",e);
//                }
//            }));
        }
        return client;
    }
}
