package com.qianlima.demo.solr.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.springframework.stereotype.Service;
import com.qianlima.demo.solr.bean.SolrBean.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @Description
 * @Author Lun_qs
 * @Date 2019/12/18 11:01
 * @Version 0.0.1
 */
@Slf4j
@Service
public class SolrSearchService {
    private static CloudSolrClient cloudSolrClient;
    private static final Set<Character> FORBIDDEN_CHAR = new HashSet<>(
            Arrays.asList('+','-','&','|','!','(',')','{','}','[',']','^','"','~','*','?',':','/'));

    // init solr connect config
    static {
        cloudSolrClient = new CloudSolrClient("192.168.30.13:2181,192.168.30.16:2181,192.168.30.12:2181");
        cloudSolrClient.setZkClientTimeout(7000);
        cloudSolrClient.setZkConnectTimeout(7000);
        //solr 集群环境下 core 创建的集合名称
        cloudSolrClient.setDefaultCollection("search_normal");
    }
    // business request bean => SolrQuery
    // QueryResponse => response
    public Response getResponse(Request request){
        QueryResponse response = null;
        try {
            SolrQuery solrQuery = new SolrQuery();
            //start offset
            solrQuery.setStart(10);
            //rows limit
            solrQuery.setRows(5);
            //fq filter
            solrQuery.setFilterQueries("yyyymmdd:[20181111 TO 20191111]");
            //q
            solrQuery.setQuery("title:\"山东省潍坊\"");
            //sort
            solrQuery.setSort("updatetime", SolrQuery.ORDER.desc);
            response = cloudSolrClient.query(solrQuery);
            System.out.println(response.getResponse());
            System.out.println(response.getResults());
        } catch (SolrServerException | IOException e) {
            e.printStackTrace();
        }
        return Response.EMPTY;
    }

    private boolean isChineses(char chr){
        Set<Character.UnicodeBlock> set = new HashSet<>(Arrays.asList(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS , Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                , Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A , Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                , Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION , Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                , Character.UnicodeBlock.GENERAL_PUNCTUATION));
        Character.UnicodeBlock ub = Character.UnicodeBlock.of('出');
        if (set.contains(ub)) {
            log.info("yeeees");
        }
        log.info("{}",ub.hashCode());
        set.forEach(e->log.info("{} -> {}", e.toString(), e.hashCode()));
        return set.contains(Character.UnicodeBlock.of(chr));
    }
    public static void main(String[] args) throws Exception {
//        QueryResponse response = cloudSolrClient.query(new SolrQuery("id:138952103"));
//        SolrDocumentList solrDocumentList = response.getResults();
//        for(SolrDocument solrDocument : solrDocumentList){
//            log.info("current:{}",solrDocument.getFieldValueMap());
//        }
//        new SolrSearchService().getResponse(null);
        cloudSolrClient.close();
        char c = 65535;
        log.info(" {} -> {}", (1L<<(c % 64)) == (1L << c),1L << c);
        log.info(" {} -> {}", 4294901760L >>> c, 16776960L >>> c);
    }
}
