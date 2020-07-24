package com.fenxiang.demo.es.repoistory;
import cn.hutool.core.bean.BeanUtil;

import com.fenxiang.demo.es.domain.RecommendGoods;
import com.fenxiang.demo.es.domain.RecommendGoodsQuery;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName EsService
 * @Author lqs
 * @Date 2020/5/6 16:46
 */
@Slf4j
@Repository
public class RecommendGoodsEsRepository {
    @Autowired
    @Qualifier("httpClient")
    private RestHighLevelClient httpClient;
    @Value("${elasticsearch.goods.index:goods_recommend}")
    private String index;
    @Value("${elasticsearch.goods.type:goods}")
    private String type;

    public  Map<String, Object> find(RecommendGoodsQuery goods, boolean fetchSource) {
        Map<String,Object> map = new HashMap<>();
        map.put("code", -1);
        try {
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.CHINA);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ",Locale.CHINA);

            // 1 构建搜索的 source  build SearchSourceBuilder  ，布尔查询构建 query builder
            BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
            {
                //基本搜索字段
                searchSourceBuilder
                        .timeout(new TimeValue(5, TimeUnit.SECONDS))
                        //default get source
                        .fetchSource(fetchSource)
                        .query(boolQueryBuilder);
                if(fetchSource){
                    searchSourceBuilder
                            // order by top desc, ac_top desc ,${sortDate} DESC
                            .sort(new FieldSortBuilder("top").order(SortOrder.DESC))
                            .sort(new FieldSortBuilder("ac_top").order(SortOrder.DESC));
                    if(StringUtils.isNotBlank(goods.getSortDate())){
                        searchSourceBuilder.sort(new FieldSortBuilder(goods.getSortDate()).order(SortOrder.DESC));
                    }
                    if (goods.getPageIndex() != null && goods.getPageSize() != null){
                        int offset = (goods.getPageIndex() - 1) * goods.getPageSize();
                        offset = Math.max(offset, 0);
                        searchSourceBuilder.from(offset).size(goods.getPageSize());
                    }
                }
            }

            // 2 构建搜索条件
            {
                // recommendGoodsId
                if(goods.getRecommendGoodsId()!=null){
                    boolQueryBuilder.must(QueryBuilders.termQuery("recommend_goods_id",goods.getRecommendGoodsId()));
                }
                //showInPage
                //                and show_in_page = #{showInPage}
                if(goods.getShowInPage() != null){
                    boolQueryBuilder.must(QueryBuilders.termQuery("show_in_page",goods.getShowInPage()));
                }
                //skuId
                //                and sku_id = #{skuId}
                if(StringUtils.isNumeric(goods.getSkuId()) && StringUtils.isBlank(goods.getKeyword())){
                    boolQueryBuilder.must(QueryBuilders.termQuery("sku_id",Long.parseLong(goods.getSkuId())));
                }

                //goodType
                //                and good_type = #{goodType}
                if(goods.getGoodType() != null){
                    boolQueryBuilder.must(QueryBuilders.termQuery("good_type",goods.getGoodType()));
                }

                //source
                //                and source = #{source}
                if(StringUtils.isNotBlank(goods.getSource())){
                    boolQueryBuilder.must(QueryBuilders.termQuery("source",goods.getSource()));
                }

                //skuName
                //                and sku_name LIKE CONCAT('%',#{skuName},'%')
                if(StringUtils.isNotBlank(goods.getSkuName())){
                    boolQueryBuilder.must(QueryBuilders.matchPhraseQuery("sku_name",goods.getSkuName()));
                }

                //keyword
                //                and ( sku_name LIKE CONCAT('%',#{keyword},'%') or sku_id=#{keyword} or brand_name LIKE CONCAT('%',#{keyword},'%'))
                if(StringUtils.isNotBlank(goods.getKeyword())){
                    final BoolQueryBuilder searchCondition = QueryBuilders.boolQuery();
                    searchCondition.should(QueryBuilders.matchPhraseQuery("sku_name",goods.getKeyword()))
                            .should(QueryBuilders.matchPhraseQuery("brand_name", goods.getKeyword()));
                    if(StringUtils.isNumeric(goods.getKeyword())){
                        searchCondition.should(QueryBuilders.termQuery("sku_id",Long.parseLong(goods.getKeyword())));
                    }
                    boolQueryBuilder.must(searchCondition);
                }

                //tags
                //                and FIND_IN_SET(#{tags},tags)
                if(StringUtils.isNotBlank(goods.getTags())){
                    final BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
                    for(String s : goods.getTags().split(",")){
                        queryBuilder.should(QueryBuilders.matchQuery("tags",s));
                    }
                    boolQueryBuilder.must(queryBuilder);
                }

                //status
                //                and status = #{status}
                if(goods.getStatus() != null){
                    boolQueryBuilder.must(QueryBuilders.termQuery("status",goods.getStatus()));
                }

                //adTags
                //                and FIND_IN_SET(#{adTags},ad_tags)
                if(StringUtils.isNotBlank(goods.getAdTags())){
                    final BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
                    for(String s : goods.getAdTags().split(",")){
                        queryBuilder.should(QueryBuilders.matchQuery("ad_tags",s));
                    }
                    boolQueryBuilder.must(queryBuilder);
                }

                //cid1
                //                and cid1 = #{cid1}
                if(goods.getCid1() != null){
                    boolQueryBuilder.must(QueryBuilders.termQuery("cid1",goods.getCid1()));
                }

                //cid2
                //                and cid2 = #{cid2}
                if(goods.getCid2() != null){
                    boolQueryBuilder.must(QueryBuilders.termQuery("cid2",goods.getCid2()));
                }

                //cid3
                //                and cid3 = #{cid3}
                if(goods.getCid3() != null){
                    boolQueryBuilder.must(QueryBuilders.termQuery("cid3",goods.getCid3()));
                }

                //pinGou
                //                 and (pingou_url is NULL OR pingou_url = '')
                if(goods.isPinGou()){
                    boolQueryBuilder.must(QueryBuilders.existsQuery("pingou_url"));
                }

                //notPinGou
                //                and (pingou_url is NOT NULL AND pingou_url &lt;&gt; '')
                if(goods.isNotPinGou()){
                    boolQueryBuilder.mustNot(QueryBuilders.existsQuery("pingou_url"));
                }

                //owner
                //                and owner = #{owner}
                if(StringUtils.isNotBlank(goods.getOwner())){
                    boolQueryBuilder.must(QueryBuilders.termQuery("owner",goods.getOwner()));
                }

                //now
                //                and #{now} BETWEEN coupon_take_begin_time and coupon_take_end_time
                if(goods.getNow()!=null){
                    String fmtNow = sdf.format(goods.getNow());
                    boolQueryBuilder.must(QueryBuilders.rangeQuery("coupon_take_begin_time").lte(fmtNow));
                    boolQueryBuilder.must(QueryBuilders.rangeQuery("coupon_take_end_time").gte(fmtNow));
                }

                //top
                if(goods.getTop() != null){
                    if(goods.getTop()==0){
                        boolQueryBuilder.must(QueryBuilders.termQuery("top",goods.getTop()));
                    }else if(goods.getTop()>0){
                        //                and top &gt; 0
                        boolQueryBuilder.must(QueryBuilders.rangeQuery("top").gt(0));
                    }
                }

                // skuIds
                // and sku_id in
                //                <foreach collection="skuIds" open="(" close=")" separator="," item="sku_id" index="i">
                //                    #{skuId}
                //                </foreach>
                if(!CollectionUtils.isEmpty(goods.getSkuIds())){
                    boolQueryBuilder.must(QueryBuilders.termsQuery("sku_id",goods.getSkuIds()));
                }

                //inputName
                //                and input_name = #{inputName}
                if(StringUtils.isNotBlank(goods.getInputName())){
                    boolQueryBuilder.must(QueryBuilders.termQuery("input_name",goods.getInputName()));
                }

                //tagsTime
                //                and tags_time = #{tagsTime}
                if(goods.getTagsTime()!=null){
                    boolQueryBuilder.must(QueryBuilders.termQuery("tags_time",goods.getTagsTime()));
                }

                //isInspection
                //                and is_inspection = #{isInspection}
                if(goods.getIsInspection()!=null){
                    boolQueryBuilder.must(QueryBuilders.termQuery("is_inspection",goods.getIsInspection()));
                }

                //inputBeginTime && inputEndTime
                //                and ${sortDate} BETWEEN #{inputBeginTime} and #{inputEndTime}
                if(StringUtils.isNotBlank(goods.getInputBeginTime()) && StringUtils.isNotBlank(goods.getInputEndTime())){
                    boolQueryBuilder.must(QueryBuilders.rangeQuery(goods.getSortDate()).gte(goods.getInputBeginTime()).lte(goods.getInputEndTime()));
                }
            }
            log.info("(bool query builder is ::: -> {}", boolQueryBuilder.toString(true));
            // 3 执行搜索,返回结果
            SearchRequest request = new SearchRequest();
            request.indices(index);
            request.types(type);
            request.source(searchSourceBuilder);
            SearchResponse response;

            response = httpClient.search(request, RequestOptions.DEFAULT);
            if(fetchSource){
                List<RecommendGoods> data = Lists.newArrayList();
                map.put("data", data);
                for (SearchHit hit : response.getHits()) {
                    Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                    RecommendGoods recommendGoods = BeanUtil.mapToBeanIgnoreCase(sourceAsMap, RecommendGoods.class, true);
                    Object tmp;
                    if((tmp = sourceAsMap.get("in_order_count_30days")) != null){
                        recommendGoods.setInOrderCount30Days(Long.parseLong(String.valueOf(tmp)));
                    }
                    if((tmp = sourceAsMap.get("img_url_list"))!=null && tmp.toString().length()>0){
                        recommendGoods.setImageUrlList(Arrays.asList(tmp.toString().split(",")));
                    }
                    data.add(recommendGoods);
                }
            } else {
                map.put("data", response.getHits().totalHits);
            }
            map.put("code",200);
        } catch (IOException e) {
            log.error("find error:", e);
            if(fetchSource){
                map.put("data",Collections.emptyList());
            }else{
                map.put("data",0);
            }
        }
        return map;
    }

    public  Map<String, Object> getBySkuId(RecommendGoodsQuery goods,boolean fetchSource) {
        RecommendGoodsQuery recommendGoodsQuery = new RecommendGoodsQuery();
        recommendGoodsQuery.setStatus(1);
        recommendGoodsQuery.setSkuId(goods.getSkuId());
        return find(recommendGoodsQuery ,fetchSource);
    }

    public  Map<String, Object> getBySkuIdAndGoodType(RecommendGoodsQuery goods,boolean fetchSource) {
        RecommendGoodsQuery recommendGoodsQuery = new RecommendGoodsQuery();
        recommendGoodsQuery.setGoodType(goods.getGoodType());
        recommendGoodsQuery.setSkuId(goods.getSkuId());
        return find(recommendGoodsQuery,fetchSource);
    }

    public  Map<String, Object> getByPrimaryKey(RecommendGoodsQuery goods,boolean fetchSource) {
        RecommendGoodsQuery recommendGoodsQuery = new RecommendGoodsQuery();
        recommendGoodsQuery.setRecommendGoodsId(goods.getRecommendGoodsId());
        return find(recommendGoodsQuery,fetchSource);
    }

    public  Map<String, Object> listBySkuIdList(RecommendGoodsQuery goods, boolean fetchSource ) {
        RecommendGoodsQuery recommendGoodsQuery = new RecommendGoodsQuery();
        recommendGoodsQuery.setSkuIds(goods.getSkuIds());
        return find(recommendGoodsQuery,fetchSource);
    }

    public  Map<String, Object> findInspectionGoodsNear24Hour(Map<String,Date> queryMap, boolean fetchSource){
        Map<String,Object> map = new HashMap<>();
        map.put("code", -1);
        try{
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

            // 1 构建搜索的 source  build SearchSourceBuilder  ，布尔查询构建 query builder
            BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
            {
                //基本搜索字段
                searchSourceBuilder
                        .timeout(new TimeValue(10, TimeUnit.SECONDS))
                        //default get source
                        .fetchSource(fetchSource)
                        .query(boolQueryBuilder);
            }

            // 2 构建搜索条件
            {
                //status
                boolQueryBuilder.must(QueryBuilders.termQuery("status",1));

                //isInspection
                boolQueryBuilder.must(QueryBuilders.termQuery("is_inspection",1));

                //now
                //        WHERE `shelf_time` BETWEEN #{yesterdayDate} AND #{nowDate}
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.CHINA);
                String yesterdayDate = sdf.format(queryMap.get("yesterdayDate"));
                String nowDate = sdf.format(queryMap.get("nowDate"));
                boolQueryBuilder.must(QueryBuilders.rangeQuery("shelf_time").lte(nowDate).gte(yesterdayDate));

            }

            // 3 执行搜索,返回结果
            SearchRequest request = new SearchRequest();
            request.indices(index);
            request.types(type);
            request.source(searchSourceBuilder);
            SearchResponse response;

            response = httpClient.search(request, RequestOptions.DEFAULT);


            if(fetchSource){
                List<RecommendGoods> data = Lists.newArrayList();
                map.put("data", data);
                for (SearchHit hit : response.getHits()) {
                    Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                    RecommendGoods recommendGoods = BeanUtil.mapToBeanIgnoreCase(sourceAsMap, RecommendGoods.class, true);
                    data.add(recommendGoods);
                }
            } else {
                map.put("data",response.getHits().totalHits);
            }
            map.put("code",200);
        }catch (Throwable e){
            log.error("查询失败:",e);
            if(fetchSource){
                map.put("data", Collections.emptyList());
            }else {
                map.put("data",0);
            }
        }
        return map;
    }

    public  Map<String, Object> board(RecommendGoodsQuery goods, boolean fetchSource) {
        Map<String,Object> map = new HashMap<>();
        map.put("code", -1);
        try {
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

            // 1 构建搜索的 source  build SearchSourceBuilder  ，布尔查询构建 query builder
            BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
            {
                //基本搜索字段
                searchSourceBuilder
                        .timeout(new TimeValue(10, TimeUnit.SECONDS))
                        //        order by ${sortField} desc
                        .sort(new FieldSortBuilder(goods.getSortField()).order(SortOrder.DESC))
                        //default get source
                        .fetchSource(fetchSource)
                        .query(boolQueryBuilder);
                if (goods.getPageIndex() != null && goods.getPageSize() != null){
                    int offset = (goods.getPageIndex() - 1) * goods.getPageSize();
                    offset = Math.max(offset, 0);
                    searchSourceBuilder.from(offset).size(goods.getPageSize());
                }
            }

            // 2 构建搜索条件
            {
                //status
                //                and status = #{status}
                if(goods.getStatus() != null){
                    boolQueryBuilder.must(QueryBuilders.termQuery("status",1));
                }

                //and comments >= 2000
                //            and good_comments_share >= 98
                if(goods.getIsPraise()!=null && goods.getIsPraise()==1){
                    boolQueryBuilder.must(QueryBuilders.rangeQuery("comments").gte(2000));
                    boolQueryBuilder.must(QueryBuilders.rangeQuery("good_comments_share").gte(98));
                }

                //             and commission > 5
                if(goods.getIsHighCommission()!=null && goods.getIsHighCommission()==1){
                    boolQueryBuilder.must(QueryBuilders.rangeQuery("commission").gt(5));
                }

                //skuName
                //                and sku_name LIKE CONCAT('%',#{skuName},'%')
                if(StringUtils.isNotBlank(goods.getSkuName())){
                    boolQueryBuilder.must(QueryBuilders.matchPhraseQuery("sku_name",goods.getSkuName()));
                }

                //goodType
                //                and good_type = #{goodType}
                if(goods.getGoodType() != null){
                    boolQueryBuilder.must(QueryBuilders.termQuery("good_type",goods.getGoodType()));
                }

                //skuId
                //                and sku_id = #{skuId}
                if(StringUtils.isNumeric(goods.getSkuId())){
                    boolQueryBuilder.must(QueryBuilders.termQuery("sku_id",Long.parseLong(goods.getSkuId())));
                }

                //source
                //                and source = #{source}
                if(StringUtils.isNotBlank(goods.getSource())){
                    boolQueryBuilder.must(QueryBuilders.termQuery("source",goods.getSource()));
                }

                //tags
                //                and FIND_IN_SET(#{tags},tags)
                if(StringUtils.isNotBlank(goods.getTags())){
                    boolQueryBuilder.must(QueryBuilders.existsQuery("tags"));
//                boolQueryBuilder.must(QueryBuilders.matchPhraseQuery("tags",goods.getTags()));
                }

            }

            // 3 执行搜索,返回结果
            SearchRequest request = new SearchRequest();
            request.indices(index);
            request.types(type);
            request.source(searchSourceBuilder);
            SearchResponse  response = httpClient.search(request, RequestOptions.DEFAULT);
            if(fetchSource){
                List<RecommendGoods> data = Lists.newArrayList();
                map.put("data", data);
                for (SearchHit hit : response.getHits()) {
                    Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                    RecommendGoods recommendGoods = BeanUtil.mapToBeanIgnoreCase(sourceAsMap, RecommendGoods.class, true);

                    boolean stepTag = true;
                    if(StringUtils.isNotBlank(goods.getTags())){
                        if(StringUtils.isNotBlank(recommendGoods.getTags())) tagFlag:{
                            for (String tag : recommendGoods.getTags().split(",")) {
                                if(tag.equals(goods.getTags())){
                                    break tagFlag;
                                }
                            }
                            stepTag = false;
                        }
                    }
                    if(stepTag){
                        data.add(recommendGoods);
                    }
                }
            } else {
                map.put("data",response.getHits().totalHits);
            }
            map.put("code",200);
        } catch (IOException e) {
            log.error("find error:", e);
            if(fetchSource){
                map.put("data",Collections.emptyList());
            }else{
                map.put("data",0);
            }
            return map;
        }

        return map;
    }

    public  Map<String, Object> findCouponExpire(RecommendGoodsQuery goods){
        Map<String,Object> map = new HashMap<>();
        map.put("code", -1);
        try{
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

            // 1 构建搜索的 source  build SearchSourceBuilder  ，布尔查询构建 query builder
            BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
            {
                //基本搜索字段
                searchSourceBuilder
                        .timeout(new TimeValue(10, TimeUnit.SECONDS))
                        // order by top desc, ac_top desc ,${sortDate} DESC
                        //default get source
                        .fetchSource(true)
                        .query(boolQueryBuilder);
            }

            // 2 构建搜索条件
            {
                //status
                boolQueryBuilder.must(QueryBuilders.termQuery("status",1));

                //now
                //        WHERE `shelf_time` BETWEEN #{yesterdayDate} AND #{nowDate}
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.CHINA);
                String now = sdf.format(goods.getNow());
                boolQueryBuilder.must(QueryBuilders.rangeQuery("coupon_take_end_time").lte(now));

            }

            // 3 执行搜索,返回结果
            SearchRequest request = new SearchRequest();
            request.indices(index);
            request.types(type);
            request.source(searchSourceBuilder);
            SearchResponse response;

            response = httpClient.search(request, RequestOptions.DEFAULT);

            List<RecommendGoods> data = Lists.newArrayList();

            map.put("data", data);
            for (SearchHit hit : response.getHits()) {
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                RecommendGoods recommendGoods = BeanUtil.mapToBeanIgnoreCase(sourceAsMap, RecommendGoods.class, true);
                data.add(recommendGoods);
            }
            map.put("code",200);
        }catch (Throwable e){
            log.error("查询失败:", e);
            map.put("data",Collections.emptyList());
        }
        return map;
    }

    public  Map<String, Object> findOwnerGoods(RecommendGoodsQuery goods){
        Map<String,Object> map = new HashMap<>();
        map.put("code", -1);
        try{
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

            // 1 构建搜索的 source  build SearchSourceBuilder  ，布尔查询构建 query builder
            BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
            {
                //基本搜索字段
                searchSourceBuilder
                        .timeout(new TimeValue(10, TimeUnit.SECONDS))
                        .fetchSource(true)
                        .query(boolQueryBuilder);
                if (goods.getPageIndex() != null && goods.getPageSize() != null){
                    int offset = (goods.getPageIndex() - 1) * goods.getPageSize();
                    offset = Math.max(offset, 0);
                    searchSourceBuilder.from(offset).size(goods.getPageSize());
                }
                if(StringUtils.isNotBlank(goods.getSortName()) && StringUtils.isNotBlank(goods.getSort())){
                    boolean isDesc = "DESC".equalsIgnoreCase(goods.getSort());
                    searchSourceBuilder.sort(goods.getSortName(),isDesc ? SortOrder.DESC : SortOrder.ASC);
                }
            }

            // 2 构建搜索条件
            {
                //status
                //                and status = #{status}
                if(goods.getStatus() != null){
                    boolQueryBuilder.must(QueryBuilders.termQuery("status",goods.getStatus()));
                }

                //skuId
                //                and sku_id = #{skuId}
                if(StringUtils.isNumeric(goods.getSkuId()) && StringUtils.isBlank(goods.getKeyword())){
                    boolQueryBuilder.must(QueryBuilders.termQuery("sku_id",Long.parseLong(goods.getSkuId())));
                }

                //skuName
                //                and sku_name LIKE CONCAT('%',#{skuName},'%')
                if(StringUtils.isNotBlank(goods.getSkuName())){
                    boolQueryBuilder.must(QueryBuilders.matchPhraseQuery("sku_name",goods.getSkuName()));
                }

                //goodType
                //                and good_type = #{goodType}
                if(goods.getGoodType() != null){
                    boolQueryBuilder.must(QueryBuilders.termQuery("good_type",goods.getGoodType()));
                }

                //now
                //                and #{now} BETWEEN coupon_take_begin_time and coupon_take_end_time
                if(goods.getNow()!=null){
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.CHINA);
                    String fmtNow = sdf.format(goods.getNow());
                    boolQueryBuilder.must(QueryBuilders.rangeQuery("coupon_take_begin_time").lte(fmtNow));
                    boolQueryBuilder.must(QueryBuilders.rangeQuery("coupon_take_end_time").gte(fmtNow));
                }

            }

            // 3 执行搜索,返回结果
            SearchRequest request = new SearchRequest();
            request.indices(index);
            request.types(type);
            request.source(searchSourceBuilder);
            SearchResponse response;

            response = httpClient.search(request, RequestOptions.DEFAULT);

            List<RecommendGoods> data = Lists.newArrayList();

            map.put("data", data);
            for (SearchHit hit : response.getHits()) {
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                RecommendGoods recommendGoods = BeanUtil.mapToBeanIgnoreCase(sourceAsMap, RecommendGoods.class, true);
                data.add(recommendGoods);
            }
            map.put("code",200);
        }catch (Throwable e){
            log.error("查询失败:", e);
            map.put("data",Collections.emptyList());
        }
        return map;
    }

    public  Map<String, Object> findForSyncByPage(RecommendGoodsQuery goods, boolean fetchSource){
        Map<String,Object> map = new HashMap<>();
        map.put("code", -1);
        try{
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

            // 1 构建搜索的 source  build SearchSourceBuilder  ，布尔查询构建 query builder
            BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
            {
                //基本搜索字段
                searchSourceBuilder
                        .timeout(new TimeValue(10, TimeUnit.SECONDS))
                        .fetchSource(fetchSource)
                        .query(boolQueryBuilder);
                if (goods.getPageIndex() != null && goods.getPageSize() != null){
                    int offset = (goods.getPageIndex() - 1) * goods.getPageSize();
                    offset = Math.max(offset, 0);
                    searchSourceBuilder.from(offset).size(goods.getPageSize());
                }
            }

            // 2 构建搜索条件
            {
                //status
                //                and status = #{status}
                boolQueryBuilder.must(QueryBuilders.termQuery("status",1));

                //now
                //            and modified &gt;= #{beginTime} and modified &lt;= #{endTime}
                if(goods.getBeginTime()!=null && goods.getEndTime()!=null){
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.CHINA);
                    String beginTime = sdf.format(goods.getBeginTime());
                    String endTime = sdf.format(goods.getEndTime());
                    boolQueryBuilder.must(QueryBuilders.rangeQuery("modified").lte(endTime).gte(beginTime));
                }

            }

            // 3 执行搜索,返回结果
            SearchRequest request = new SearchRequest();
            request.indices(index);
            request.types(type);
            request.source(searchSourceBuilder);
            SearchResponse response;

            response = httpClient.search(request, RequestOptions.DEFAULT);
            if(fetchSource){
                List<RecommendGoods> data = Lists.newArrayList();
                map.put("data", data);
                for (SearchHit hit : response.getHits()) {
                    Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                    RecommendGoods recommendGoods = BeanUtil.mapToBeanIgnoreCase(sourceAsMap, RecommendGoods.class, true);
                    data.add(recommendGoods);
                }
            }else{
                map.put("data",response.getHits().totalHits);
            }
            map.put("code",200);
        }catch (Throwable e){
            log.error("查询失败!",e);
            if(fetchSource){
                map.put("data",Collections.emptyList());
            }else{
                map.put("data",0);
            }
        }
        return map;
    }

    public  Map<String, Object> fenxiangInspectionGoodsByPage(RecommendGoodsQuery goods, boolean fetchSource) {
        Map<String,Object> map = new HashMap<>();
        map.put("code", -1);
        try{
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

            // 1 构建搜索的 source  build SearchSourceBuilder  ，布尔查询构建 query builder
            BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
            {
                //基本搜索字段
                searchSourceBuilder
                        .timeout(new TimeValue(10, TimeUnit.SECONDS))
                        //        ORDER BY `inspection_top` DESC , ${sortDate} DESC
                        .sort(new FieldSortBuilder(goods.getSortDate()).order(SortOrder.DESC))
                        .sort(new FieldSortBuilder("inspection_top").order(SortOrder.DESC))
                        //default get source
                        .fetchSource(fetchSource)
                        .query(boolQueryBuilder);
                if (goods.getPageIndex() != null && goods.getPageSize() != null){
                    int offset = (goods.getPageIndex() - 1) * goods.getPageSize();
                    offset = Math.max(offset, 0);
                    searchSourceBuilder.from(offset).size(goods.getPageSize());
                }
                if(fetchSource){
                    searchSourceBuilder
                            .sort("inspection_top",SortOrder.DESC)
                            .sort(goods.getSortDate(),SortOrder.DESC);
                }
            }

            // 2 构建搜索条件
            {
                //isInspection
                //                and is_inspection = #{isInspection}
                boolQueryBuilder.must(QueryBuilders.termQuery("is_inspection",1));

                //status
                //                and status = #{status}
                if(goods.getStatus() != null){
                    boolQueryBuilder.must(QueryBuilders.termQuery("status",goods.getStatus()));
                }

                //                AND NOW() BETWEEN coupon_take_begin_time AND coupon_take_end_time
                Integer isInspectioneffective = goods.getIsInspectioneffective();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.CHINA);
                if(isInspectioneffective != null && (isInspectioneffective==0 ||isInspectioneffective==1)){
                    String now = sdf.format(new Date());
                    boolQueryBuilder.must(QueryBuilders.rangeQuery("coupon_take_begin_time").lte(now));
                    boolQueryBuilder.must(QueryBuilders.rangeQuery("coupon_take_end_time").gte(now));
                    //                AND `shelf_time` &lt;= NOW()
                    if(isInspectioneffective==1){
                        boolQueryBuilder.must(QueryBuilders.rangeQuery("shelf_time").lte(now));
                        //                AND `shelf_time` &gt;= NOW()
                    }else {
                        boolQueryBuilder.must(QueryBuilders.rangeQuery("shelf_time").gte(now));
                    }
                }

                //                AND `shelf_time` BETWEEN #{inspectionStartTime} AND #{inspectionEndTime}
                if(goods.getInspectionStartTime()!=null && goods.getInspectionEndTime()!=null){
                    String inspectionStartTime = sdf.format(goods.getInspectionStartTime());
                    String inspectionEndTime = sdf.format(goods.getInspectionEndTime());
                    boolQueryBuilder.must(QueryBuilders.rangeQuery("shelf_time").lte(inspectionEndTime).gte(inspectionStartTime));
                }

                //goodType
                //                and good_type = #{goodType}
                if(goods.getGoodType() != null){
                    boolQueryBuilder.must(QueryBuilders.termQuery("good_type",goods.getGoodType()));
                }

                //skuId
                //                and sku_id = #{skuId}
                if(StringUtils.isNumeric(goods.getSkuId()) && StringUtils.isBlank(goods.getKeyword())){
                    boolQueryBuilder.must(QueryBuilders.termQuery("sku_id",Long.parseLong(goods.getSkuId())));
                }

                //skuName
                //                and sku_name LIKE CONCAT('%',#{skuName},'%')
                if(StringUtils.isNotBlank(goods.getSkuName())){
                    boolQueryBuilder.must(QueryBuilders.matchPhraseQuery("sku_name",goods.getSkuName()));
                }

            }

            // 3 执行搜索,返回结果
            SearchRequest request = new SearchRequest();
            request.indices(index);
            request.types(type);
            request.source(searchSourceBuilder);
            SearchResponse response;
            response = httpClient.search(request, RequestOptions.DEFAULT);
            if(fetchSource){
                List<RecommendGoods> data = Lists.newArrayList();
                map.put("data", data);
                for (SearchHit hit : response.getHits()) {
                    Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                    RecommendGoods recommendGoods = BeanUtil.mapToBeanIgnoreCase(sourceAsMap, RecommendGoods.class, true);
                    data.add(recommendGoods);
                }
            } else {
                map.put("data",response.getHits().totalHits);
            }
            map.put("code",200);
        }catch (Throwable e){
            log.error("查询错误:",e);
            if(fetchSource){
                map.put("data",Collections.emptyList());
            }else{
                map.put("data",0);
            }
        }
        return map;
    }

    public  Map<String, Object> findHotSale(RecommendGoodsQuery goods){
        Map<String,Object> map = new HashMap<>();
        map.put("code", -1);
        try {
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

            // 1 构建搜索的 source  build SearchSourceBuilder  ，布尔查询构建 query builder
            BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
            {
                //基本搜索字段
                searchSourceBuilder
//                    .from(queryVo.getFrom())
//                    .size(queryVo.getSize())
                        .timeout(new TimeValue(10, TimeUnit.SECONDS))
                        //        order by top desc
                        .sort(new FieldSortBuilder("top").order(SortOrder.DESC))
                        //default get source
                        .fetchSource(true)
                        .query(boolQueryBuilder);
            }

            // 2 构建搜索条件
            {
                //showInPage
                //                and show_in_page = #{showInPage}
                if(goods.getShowInPage() != null){
                    boolQueryBuilder.must(QueryBuilders.termQuery("show_in_page",goods.getShowInPage()));
                }

                //tags
                //                and FIND_IN_SET(#{tags},tags)
                if(StringUtils.isNotBlank(goods.getTags())){
                    boolQueryBuilder.must(QueryBuilders.existsQuery("tags"));
                }

                //status
                //                and status = #{status}
                if(goods.getStatus() != null){
                    boolQueryBuilder.must(QueryBuilders.termQuery("status",goods.getStatus()));
                }

                //now
                //                and (
                //                (#{now} BETWEEN coupon_take_begin_time and coupon_take_end_time and coupon_url IS NOT NULL and coupon_url != '')
                //                or
                //                (coupon_take_begin_time IS NULL AND coupon_take_end_time IS NULL)
                //                )
                if(goods.getNow()!=null){
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.CHINA);
                    String fmtNow = sdf.format(goods.getNow());

                    BoolQueryBuilder condition0 = QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery("coupon_take_begin_time").lte(fmtNow))
                            .must(QueryBuilders.rangeQuery("coupon_take_end_time").gte(fmtNow))
                            .must(QueryBuilders.existsQuery("coupon_url"));

                    BoolQueryBuilder condition1 = QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery("coupon_take_begin_time"))
                            .mustNot(QueryBuilders.existsQuery("coupon_take_end_time"));

                    boolQueryBuilder.should(condition0).should(condition1);
                }

                //top
                if(goods.getTop() != null){
                    if(goods.getTop() == 0){
                        boolQueryBuilder.must(QueryBuilders.termQuery("top",goods.getTop()));
                    }else if(goods.getTop() > 0){
                        //                and top &gt; 0
                        boolQueryBuilder.must(QueryBuilders.rangeQuery("top").gt(0));
                    }
                }

            }

            // 3 执行搜索,返回结果
            SearchRequest request = new SearchRequest();
            request.indices(index);
            request.types(type);
            request.source(searchSourceBuilder);
            SearchResponse response;

            response = httpClient.search(request, RequestOptions.DEFAULT);

            List<RecommendGoods> data = Lists.newArrayList();
            List<Object> list = Lists.newArrayList();
            map.put("data", list);
            for (SearchHit hit : response.getHits()) {
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                RecommendGoods recommendGoods = BeanUtil.mapToBeanIgnoreCase(sourceAsMap, RecommendGoods.class, true);

                boolean stepTag = true;
                if(StringUtils.isNotBlank(goods.getTags())){
                    if(StringUtils.isNotBlank(recommendGoods.getTags())) tagFlag:{
                        for (String tag : recommendGoods.getTags().split(",")) {
                            if(tag.equals(goods.getTags())){
                                break tagFlag;
                            }
                        }
                        stepTag = false;
                    }
                }
                if(stepTag){
                    data.add(recommendGoods);
                    list.add(recommendGoods.getRecommendGoodsId());
                }
            }
            map.put("code",200);
        }catch (Throwable e){
            log.error("查询失败:",e);
            map.put("data",Collections.emptyList());
        }
        return map;
    }

    public  Map<String, Object> findPastGoods(RecommendGoodsQuery goods, boolean fetchSource){
        Map<String,Object> map = new HashMap<>();
        map.put("code", -1);
        try{
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

            // 1 构建搜索的 source  build SearchSourceBuilder  ，布尔查询构建 query builder
            BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
            {
                //基本搜索字段
                searchSourceBuilder
//                    .from(queryVo.getFrom())
//                    .size(queryVo.getSize())
                        .timeout(new TimeValue(10, TimeUnit.SECONDS))
                        //         order by modified
                        .sort(new FieldSortBuilder("modified").order(SortOrder.ASC))
                        //default get source
                        .fetchSource(fetchSource)
                        .query(boolQueryBuilder);
            }

            // 2 构建搜索条件
            {

                //status
                //                and status = #{status}
                if(goods.getStatus() != null){
                    boolQueryBuilder.must(QueryBuilders.termQuery("status",goods.getStatus()));
                }

                //goodType
                //                and good_type = #{goodType}
                if(goods.getGoodType() != null){
                    boolQueryBuilder.must(QueryBuilders.termQuery("good_type",goods.getGoodType()));
                }

                //        and modified &lt; #{cleanDate}
                if(StringUtils.isNotBlank(goods.getCleanDate())){
                    boolQueryBuilder.must(QueryBuilders.rangeQuery("modified").lt(goods.getCleanDate()));
                }

            }

            // 3 执行搜索,返回结果
            SearchRequest request = new SearchRequest();
            request.indices(index);
            request.types(type);
            request.source(searchSourceBuilder);
            SearchResponse response;

            response = httpClient.search(request, RequestOptions.DEFAULT);

            if(fetchSource){
                List<RecommendGoods> data = Lists.newArrayList();
                map.put("data", data);
                for (SearchHit hit : response.getHits()) {
                    Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                    RecommendGoods recommendGoods = BeanUtil.mapToBeanIgnoreCase(sourceAsMap, RecommendGoods.class, true);
                    data.add(recommendGoods);
                }
            }else{
                map.put("data",response.getHits().totalHits);
            }
            map.put("code",200);
        }catch (Throwable e){
            log.error("查询失败:",e);
            if(fetchSource){
                map.put("data",Collections.emptyList());
            }else {
                map.put("data",0);
            }
        }
        return map;
    }

}
