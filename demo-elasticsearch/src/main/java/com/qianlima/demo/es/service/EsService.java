package com.qianlima.demo.es.service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.qianlima.demo.es.bean.EsPojo;
import com.qianlima.demo.es.bean.vo.QueryVo;
import com.qianlima.demo.es.util.DateUtils;
import com.qianlima.demo.es.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Description
 * @Author Lun_qs
 * @Date 2019/9/18 9:41
 * @Version 0.0.1
 */
@Slf4j
@Service
public class EsService {

    private final RestHighLevelClient restClient;

    public EsService(RestHighLevelClient restClient){
        this.restClient = restClient;
    }

    /**
     * 根据id获取一条json记录
     * @param index db
     * @param esId key
     * @return json record
     */
    public Result get(String index, String esId){
        try {

            GetRequest request = new GetRequest(index, esId);
            GetResponse response = restClient.get(request, RequestOptions.DEFAULT);
            log.info("get response result: {}",response.getSourceAsString());
            if(response.isExists()){
                return Result.ok(response.getSourceAsMap(),1);
            }
        }catch (Exception except){
            except.printStackTrace();
            log.error("获取操作失败:", except);
        }
        return Result.error(404);
    }

    /**
     * 插入一条es记录
     * @param index db
     * @param esPojo key
     * @return insert result
     */
    public Result upsert(String index, EsPojo esPojo){
        try{
            Assert.notNull(esPojo,"插入值必须非空！");
            Assert.isTrue(!StringUtils.isBlank(esPojo.getId()),"插入主键不能为空");
            String jsonString = JSONObject.toJSONString(esPojo);
            JSONObject jsonObject = JSONObject.parseObject(jsonString);
            jsonObject.remove("id");
            UpdateRequest request = new UpdateRequest(index, esPojo.getId())
                    .docAsUpsert(true).doc(jsonObject);
                    //.doc(jsonString, XContentType.JSON); // jsonString
            UpdateResponse response = restClient.update(request, RequestOptions.DEFAULT);
            log.info("upsert response:{} - {}", response.status().getStatus(),  response.getResult().getLowercase());
            return Result.ok(response.status().getStatus(),  response.getResult().getLowercase());
        }catch (Exception except){
            log.error("新增操作失败:", except);
        }
        return Result.error(404);
    }

    /**
     * 删除一条记录
     * @param index db
     * @param esId key
     * @return delete one record
     */
    public Result delete(String index, String esId){
        try{
            DeleteRequest request = new DeleteRequest(index,esId);
            DeleteResponse response = restClient.delete(request, RequestOptions.DEFAULT);
            if(response.status() == RestStatus.OK){
                return Result.ok(200, request.getDescription());
            }
        }catch (Exception except){
            log.error("删除操作失败:",except);
        }
        return Result.error(404);
    }

    /**
     * es 根据source 获取列表
     * @param index db
     * @param queryVo query conditions
     * @return Result
     */
    public Result list(String index, QueryVo queryVo){
        try{
            //搜索日期校验
            validateQueryVo(queryVo);

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            // 构建搜索的source  build SearchSourceBuilder
            {
                //布尔查询构建 query builder
                BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

                //基本搜索字段
                searchSourceBuilder.from(queryVo.getFrom())
                        .size(queryVo.getSize())
                        .timeout(new TimeValue(30, TimeUnit.SECONDS))
                        .sort(new ScoreSortBuilder().order(SortOrder.DESC))
                        .sort(new FieldSortBuilder("id").order(SortOrder.DESC))
                        //default get source
                        .fetchSource(true)
                        .query(boolQueryBuilder);

                //日期过滤上限 date filter condition
                if(queryVo.getHigherSellTime() != null){
                    boolQueryBuilder.must(new RangeQueryBuilder("sellTime")
                            .lte(queryVo.getHigherSellTime())
                    );
                }
                //日期过滤下限
                if(queryVo.getLowerSellTime() != null){
                    boolQueryBuilder.must(new RangeQueryBuilder("sellTime")
                            .gte(queryVo.getLowerSellTime()));
                }

                //价格过滤
                boolQueryBuilder.must(QueryBuilders.rangeQuery("price")
                        .lte(queryVo.getHigherPrice())
                        .gte(queryVo.getLowerPrice()));

                //名称字段 names 高亮字段
                String[] namesArr;
                boolean isNameHighLight = false;
                if((namesArr = StringUtils.split(queryVo.getNames()," "))!=null){
                    BoolQueryBuilder nameQueryBuilder = new BoolQueryBuilder();
                    for(String name : namesArr){
                        if(StringUtils.isNotBlank(name)){
                            isNameHighLight = true;
                            nameQueryBuilder.should(QueryBuilders.matchQuery("name",name));
                        }
                    }
                    boolQueryBuilder.must(nameQueryBuilder);
                }

                //内容字段 contents 高亮字段
                String[] contentsArr;
                boolean isContentHighLight = false;
                //内部逻辑是 if(A&&(subA||subB)) 其中contentQueryBuilder是(subA||subB)
                if((contentsArr = StringUtils.split(queryVo.getContents()," ")) != null){
                    BoolQueryBuilder contentQueryBuilder = new BoolQueryBuilder();
                    for(String content : contentsArr){
                       if(StringUtils.isNotBlank(content)){
                           isContentHighLight = true;
                           contentQueryBuilder.should(QueryBuilders.matchQuery("content", content));
                       }
                    }
                    boolQueryBuilder.must(contentQueryBuilder);
                }

                //内容 ||名称 字段 是否使用高亮
                if(isContentHighLight || isNameHighLight){
                    //highlighter
                    HighlightBuilder highlightBuilder = new HighlightBuilder()
                            .preTags("<font color='red'>").postTags("</font>");
                    if(isContentHighLight){
                        highlightBuilder.field(
                                new HighlightBuilder.Field("content")
                                    .fragmentSize(150)
                                    .numOfFragments(3)
                                    .highlighterType("unified")
                        );
                    }
                    if(isNameHighLight){
                        highlightBuilder.field("name",150,3);
                    }
                    searchSourceBuilder.highlighter(highlightBuilder);
                }
            }
            SearchRequest request = new SearchRequest();
            request.indices(index);
            request.source(searchSourceBuilder);
            SearchResponse response = restClient.search(request, RequestOptions.DEFAULT);
            log.warn("花费 {}", response.getTook());
            if(response.status() == RestStatus.OK){
                SearchHits hits = response.getHits();
                List<Map<String, Object>> res = Lists.newArrayList();
                for(SearchHit hit : hits){
                    Map<String, HighlightField> highlightFields;
                    Map<String, Object> esMap = hit.getSourceAsMap();
                    //添加EsId 进入反序列化中
                    esMap.put("id", hit.getId());
                    //解析高亮文本
                    if(!CollectionUtils.isEmpty(highlightFields = hit.getHighlightFields())){
                        if(highlightFields.containsKey("name")){
                            HighlightField highlightField = highlightFields.get("name");
                            esMap.put("name",highlightField.fragments()[0].string());
                        }
                        if(highlightFields.containsKey("content")){
                            HighlightField highlightField = highlightFields.get("content");
                            esMap.put("content",highlightField.fragments()[0].string());
                        }
                    }
                    res.add(esMap);
                }
                return Result.ok(res, hits.getTotalHits().value);
            }
        }catch (Exception ex){
            log.error("获取列表失败", ex);
        }
        return Result.error(404);
    }

    /**
     * 校验查询参数
     * @param queryVo validate queryVo
     * @return [isSuccess?, case msg]
     */
    private void validateQueryVo(QueryVo queryVo){
        if(DateUtils.validDateString(queryVo.getLowerSellTime())){
            queryVo.setLowerSellTime(DateUtils.formatString(queryVo.getLowerSellTime()));
        }else{
            queryVo.setLowerSellTime(null);
        }
        if(DateUtils.validDateString(queryVo.getHigherSellTime())){
            queryVo.setHigherSellTime(DateUtils.formatString(queryVo.getHigherSellTime()));
        }else{
            queryVo.setHigherSellTime(null);
        }
    }
}
