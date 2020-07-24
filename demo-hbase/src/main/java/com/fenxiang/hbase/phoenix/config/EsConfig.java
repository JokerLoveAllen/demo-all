package com.fenxiang.hbase.phoenix.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class EsConfig {
    @Value("${elasticsearch.host:152.136.194.249}")
    public String host;
    @Value("${elasticsearch.httpPort:9200}")
    public int httpPort;
    @Value("${elasticsearch.cluster:fenxiang}")
    public String cluster;

    @Bean(destroyMethod = "close")
    public RestHighLevelClient restHighLevelClient() {
        RestHighLevelClient client = new RestHighLevelClient(
                    RestClient.builder(new HttpHost(host, httpPort)));
        return client;
    }
}