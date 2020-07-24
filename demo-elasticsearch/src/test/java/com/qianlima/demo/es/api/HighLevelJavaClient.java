package com.qianlima.demo.es.api;

import com.alibaba.fastjson.JSON;
import com.qianlima.demo.es.SpringbootElasticTest;
import com.qianlima.demo.es.bean.EsPojo;
import com.qianlima.demo.es.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.math.BigDecimal;
import java.rmi.UnexpectedException;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * BASE ON elasticsearch JAVA HIGH LEVEL CLIENT API v7.3.1
 * due to es version change fast, this provide refer
 * @Description
 * @Author Lun_qs
 * @Date 2019/9/4 15:20
 * @Version 0.0.1
 */
@Slf4j
public class HighLevelJavaClient{

    public Result<String> get(String id){
        String res = "";
        int count = 0;
        try(RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("192.168.30.13", 9700))
        )) {
            GetRequest request = new GetRequest("qianlima", id);
            GetResponse response = client.get(request, RequestOptions.DEFAULT);
            res = response.getSourceAsString();
            count += response.isExists() ? 1 : 0;
//            EsPojo esPojo = new EsPojo();
//            esPojo = JSON.parseObject(response.getSourceAsString(), EsPojo.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        return Result.ok(res, count);
    }

    public String list(String index){
        try(RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("192.168.30.13", 9700))
        )){
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            //timeValue: 设置最多多长时间等待返回结果
            sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
            //pageNo 页码下标,从0开始 {from : int} => pageNo * limit = offset
            sourceBuilder.from(0);
            //pageSize 页面数量{size : int} => limit
            sourceBuilder.size(12);
            //SORT [{},{}]
            //可以按照得分顺序倒排
            sourceBuilder.sort(new FieldSortBuilder("id").order(SortOrder.DESC))
                    .sort(new ScoreSortBuilder().order(SortOrder.DESC));
            //返回的json中是否包含_source字段信息; 业务是否只关心_id
            sourceBuilder.fetchSource(true);

            //高亮一类字段
            //{
            //    "query" : { "match" : { "content" : "喜还" }},
            //    "highlight" : {
            //        "pre_tags" : ["<tag>"],
            //        "post_tags" : ["</tag>"],
            //        "fields" : {
            //            "content" : {}
            //        }
            //    }
            //}
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            // 搜索时使用高亮
            sourceBuilder.highlighter(highlightBuilder);
            //添加前缀和后缀
            highlightBuilder.preTags("<font color='red'>").postTags("</font>");
            //为高亮器添加某一个字段
            HighlightBuilder.Field fieldBuilder = new HighlightBuilder.Field("content");
            //@see https://www.elastic.co/guide/en/elasticsearch//reference/current/search-request-body.html#highlighting-examples
            //高亮类型在 v7.3.1 分为
            // `unified`默认使用 功能强大,复杂文档大量高亮字段适合此类
            // `plain`最适合在单个字段中突出显示简单查询匹配
            // `fvh` 快速矢量高亮
            fieldBuilder.highlighterType("unified");
            //搜索片段长度. 默认100
            fieldBuilder.fragmentSize(150);
            //返回的高亮最多显示的片段数
            fieldBuilder.numOfFragments(3);
            highlightBuilder.field(fieldBuilder);
            
            //后验过滤
            BoolQueryBuilder filter = new BoolQueryBuilder();
            filter.must(QueryBuilders.matchQuery("content","喜欢"));
            //etc...
            //在搜索完成后进行过滤
            sourceBuilder.postFilter(filter);

            //按不同的查询条件
            BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
            //查询范围,使用rangeQuery  must logic -> &&
            boolQueryBuilder.must(QueryBuilders.rangeQuery("sellTime").gte("2015-01-01").lte("2020-01-01"));
//            boolQueryBuilder.must(QueryBuilders.rangeQuery("sellTime").lte("2020-01-01"));
            //should logic -> ||
            BoolQueryBuilder contentQueryBuilder = new BoolQueryBuilder();
            contentQueryBuilder.should(QueryBuilders.matchQuery("content","茶叶"));
            contentQueryBuilder.should(QueryBuilders.matchQuery("content","鹅蛋"));
            boolQueryBuilder.must(contentQueryBuilder);
            //添加查询builder
            sourceBuilder.query(boolQueryBuilder);

            SearchRequest request = new SearchRequest();
            //es index  -> DBMS database
            request.indices(index);
            request.source(sourceBuilder);
            SearchResponse searchResponse = client.search(request, RequestOptions.DEFAULT);
            if(searchResponse.status() != RestStatus.OK){
                throw new RuntimeException(new UnexpectedException("yes, you dont expected it happened"));
            }
            SearchHits hits = searchResponse.getHits();
            for(SearchHit hitItem : hits){
                EsPojo esItem = JSON.parseObject(hitItem.getSourceAsString(), EsPojo.class);
                log.info("item: {}", esItem);
                Map<String, HighlightField> highlightFields = hitItem.getHighlightFields();
                if(highlightFields.size()>0){
                    log.info("hits.highlightFields.size:{}", highlightFields.size());
                    for(Map.Entry<String,HighlightField> entry : highlightFields.entrySet()){
                        log.info("HighlightField.key:{},HighlightField.value:{}", entry.getKey(), Arrays.toString(entry.getValue().fragments()));
                    }
                }

            }
            log.info("hits.count:{}", hits.getTotalHits().value);
            log.info("took.consume:{}", searchResponse.getTook());
        }catch (Exception e){
            e.printStackTrace();
        }
        return "bar";
    }

    /**
     * 1) XContentBuilder提供了(key,value) pair形式的api可以将map转化为内置的参数
     * 2) 或者使用json格式的String转化为 byte Array
     * 两者都会以byte array形式于 C--S 之间进行传输
     */
    public Result<String> upsert(String index , String id, EsPojo p){
        try(RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("192.168.30.13", 9700))
        )){
            // (k,v) pair
//            XContentBuilder builder = XContentFactory.jsonBuilder();
//            builder.startObject();
//            {
//                builder.field("name","zhangsan");
//                builder.field("level","superme");
//                builder.field("code",1);
//            }
//            builder.endObject();

            //ES java api 没有提供直接插入javabean 的Api,需要转为XContent 或者 json string or json byte
            // 仅供参考
            if(p == null){
                p = new EsPojo();
                p.setId("4");
                p.setContent("只是最好最坏的时代,max是916");
                p.setPrice(new BigDecimal("123.6"));
                p.setName("在那遥远的地方");
                p.setSellReason("不需要,需要找补一些");
                p.setSellTime("2019-02-06");
            }

            //1以UpdateRequest请求更新或插入
            UpdateRequest request = new UpdateRequest(index,id)
                    //意思是存在的时候执行更新,不存在执行创建 (update||insert) => upsert
                    .docAsUpsert(true)
                    //ES java客户端只接收Map, byte[], JSON String, XContentBuilder 形式的值
                    .doc(JSON.toJSONString(p), XContentType.JSON);
            UpdateResponse updateResponse = client.update(request, RequestOptions.DEFAULT);
            log.info(updateResponse.toString());

            //2以 IndexRequest 请求插入
            //以Map形式存放 XContentBuilder
//            Map<String,Object> map = JSON.parseObject(JSON.toJSONString(p));
//            map.put("sellTime","2019-06-07 15:05:06");
//            IndexRequest indexRequest = new IndexRequest("qianlimaer")
//                    //可以执行创建
//                    .create(false)
//                    .id("2")
//                    .source(map);
//            IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
//            log.info(indexResponse.toString());

            return Result.error(updateResponse.status().getStatus(),updateResponse.getResult().getLowercase());
        }catch (Exception e){
            e.printStackTrace();
            return Result.error(500,"服务器忙 :-( ");
        }
    }

    public Result delete(String index,  String id){
        try(RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("192.168.30.13", 9700))
        )){
            DeleteRequest s = new DeleteRequest().index(index).id(id);
            DeleteResponse delete = client.delete(s, RequestOptions.DEFAULT);
            log.info("delete.toString(): {}", delete.toString());
            return Result.error(delete.status().getStatus(), delete.getResult().getLowercase());
        }catch (Exception e){
            e.printStackTrace();
            return Result.error(500,"服务器忙 :-( ");
        }
    }

    public static void main(String[] args) {
//        HighLevelJavaClient client = new HighLevelJavaClient();
//        //client.list("qianlima");
//        System.out.println(client.get("8"));
        System.out.println("电梯,空调,果宝特攻和弄个".replaceAll(",","\\$@\\$"));
    }
}
