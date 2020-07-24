package com.qianlima.demo.solr.conf;

import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description
 * @Author Lun_qs
 * @Date 2019/12/18 13:43
 * @Version 0.0.1
 */
@Slf4j
@Configuration
public class SolrClientConfig {
    @Value("${solr.zkClientTimeout:7000}")
    private int zkClientTimeout;
    @Value("${solr.zkConnectTimeout:7000}")
    private int zkConnectTimeout;
    @Value("${solr.defaultCollection:search_normal}")
    private String defaultCollection;
    @Value("${solr.zkHost:192.168.30.13:2181,192.168.30.16:2181,192.168.30.12:2181}")
    private String zkHost;

    @Bean(destroyMethod = "close")
    public CloudSolrClient getCloudSolrClient(){
        CloudSolrClient cloudSolrClient = new CloudSolrClient(zkHost);
        cloudSolrClient.setZkClientTimeout(zkClientTimeout);
        cloudSolrClient.setZkConnectTimeout(zkConnectTimeout);
        //solr 集群环境下 core 创建的集合名称
        cloudSolrClient.setDefaultCollection(defaultCollection);
        return cloudSolrClient;
    }
}
