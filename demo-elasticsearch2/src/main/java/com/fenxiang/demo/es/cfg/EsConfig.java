package com.fenxiang.demo.es.cfg;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.*;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
@Slf4j
public class EsConfig {
    @Value("${elasticsearch.host:localhost}")
    public String host;
    @Value("${elasticsearch.tcpPort:9300}")
    public int tcpPort;
    @Value("${elasticsearch.httpPort:9200}")
    public int httpPort;
    @Value("${elasticsearch.cluster:fenxiang}")
    public String cluster;
    @Value("${elasticsearch.rest.username}")
    public String username;
    @Value("${elasticsearch.rest.password}")
    public String password;

    // 在 es 8以后废弃掉TCP方式的访问，使用基于Http的 HighLevelRestApi进行访问
//    @Bean("tcpClient")
    public Client client(){
        TransportClient client = null;
        try{
            log.info("host:"+ host + "port:"+tcpPort);
            Settings elasticsearchSettings = Settings.builder()
                    .put("client.transport.sniff", false)
                    .put("cluster.name", cluster)
                    .build();
            client = new PreBuiltTransportClient(elasticsearchSettings);
            client.addTransportAddress(new TransportAddress(InetAddress.getByName(host), tcpPort));
        } catch (UnknownHostException e) {
            log.error("elasticsearch 客户端连接异常:",e);
        }
        return client;
    }


    @Bean(name = "httpClient", destroyMethod = "close")
    public RestHighLevelClient restHighLevelClient() {
        boolean auth = StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password);
        RestClientBuilder restClientBuilder = RestClient.builder(new HttpHost(host, httpPort));
        if(auth){
            restClientBuilder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                @Override
                public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                    credentialsProvider.setCredentials(AuthScope.ANY,new UsernamePasswordCredentials(username,password));
                    return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                }
            });
        }
        return new RestHighLevelClient(restClientBuilder);
    }
}