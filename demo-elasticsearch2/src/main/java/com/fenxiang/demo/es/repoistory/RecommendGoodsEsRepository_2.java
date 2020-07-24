package com.fenxiang.demo.es.repoistory;

import cn.hutool.core.bean.BeanUtil;
import com.fenxiang.demo.es.domain.RecommendGoods;
import com.fenxiang.demo.es.domain.RecommendGoodsQuery;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
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

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * @ClassName EsService
 * @Author lqs
 * @Date 2020/5/6 16:46
 */
@Slf4j
@Repository
public class RecommendGoodsEsRepository_2 {
    @Autowired
    @Qualifier("httpClient")
    private RestHighLevelClient httpClient;
    @Value("${elasticsearch.goods.index:recommend}")
    private String index;
    @Value("${elasticsearch.goods.type:goods}")
    private String type;

    private final String scriptFormat = "ctx._source['%s']=%s;";

    public Map<String,Object> create(RecommendGoods goods) {
        Map<String,Object> map = new HashMap<>();
        try {
            DocWriteResponse response;
            goods.setCreated(new Date());
            goods.setModified(new Date());
            final XContentBuilder content = buildContent(goods);
            if(content==null){
                return map;
            }
            UpdateRequest updateRequest = new UpdateRequest(index, type, String.valueOf(goods.getRecommendGoodsId()));
            updateRequest.docAsUpsert(true).doc(content);
            response = httpClient.update(updateRequest, RequestOptions.DEFAULT);

            log.info("response id:({}), Result:({})", response.getId(),response.getResult().toString());
            map.put("_id", response.getId());
            map.put("result",response.getResult().toString());
        }catch (Throwable e){
            log.error("创建失败:", e);
        }
        return map;
    }

    public Map<String,Object> bulkUpsert(List<RecommendGoods> datas) {
        Map<String,Object> map = new HashMap<>();
        try {
            BulkRequest bulkRequest = new BulkRequest();
            for (RecommendGoods goods : datas) {
                UpdateRequest updateRequest = new UpdateRequest(index, type, String.valueOf(goods.getRecommendGoodsId()));
                final XContentBuilder content = buildContent(goods);
                if(content==null){
                    log.error(" {} is null jump!",goods);
                    continue;
                }
                updateRequest.docAsUpsert(true).doc(content);
                bulkRequest.add(updateRequest);
            }
            BulkResponse response = httpClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            map.put("result",response.status().getStatus());
        }catch (Throwable e){
            log.error("批处理失败:",e);
        }
        return map;
    }

    public  Map<String,Object> updateByQuery(RecommendGoods goods) {
        Map<String,Object> res = new HashMap<>();
        try{
            // step1 构建 bool query builder
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            {
                int modifyFlag = 0;
                if(goods.getRecommendGoodsId()!=null){
                    modifyFlag++;
                    boolQueryBuilder.must(QueryBuilders.termQuery("recommend_goods_id",goods.getRecommendGoodsId()));
                }else if(goods.getSkuId()!=null){
                    modifyFlag++;
                    boolQueryBuilder.must(QueryBuilders.termQuery("sku_id",goods.getSkuId()));
                }else if(goods.getShopId()!=null){
                    modifyFlag++;
                    boolQueryBuilder.must(QueryBuilders.termQuery("shop_id",goods.getShopId()));
                }
                if(modifyFlag==0){
                    throw new UnsupportedOperationException("必修提供修改条件!!!!");
                }
            }

            // step2 构建 脚本查询参数
            goods.setModified(new Date());
            final String scriptString = buildScript(goods);
            log.info("script: {}", scriptString);

            // step3 执行操作
            updateByQueryAsyncPre(boolQueryBuilder,scriptString);
        }catch (Throwable e){
            log.error("更新失败:",e);
        }
        return res;
    }

    public  Map<String,Object> updateBySkuId(RecommendGoods goods) {
        Map<String,Object> res = new HashMap<>();
        try{
            // step1 构建 bool query builder
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            {
                boolQueryBuilder.must(QueryBuilders.termQuery("sku_id",goods.getSkuId()));
            }

            goods.setModified(new Date());
            // step2 构建 脚本查询参数
            String scriptString = buildScript(goods);
            if(StringUtils.isBlank(goods.getTags())){
                scriptString += String.format(scriptFormat,"tags", null);
            }
            if(StringUtils.isBlank(goods.getAdTags())){
                scriptString += String.format(scriptFormat,"ad_tags", null);
            }

            log.info("script: {}", scriptString);

            // step3 执行操作
            updateByQueryAsyncPre(boolQueryBuilder,scriptString);
        }catch (Throwable e){
            log.error("更新失败:",e);
        }
        return res;
    }

    public  Map<String,Object> clearAdTags(RecommendGoods goods) {
        Map<String,Object> res = new HashMap<>();
        try{
            // step1 构建 bool query builder
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            {
                boolQueryBuilder.must(QueryBuilders.termQuery("recommend_goods_id",goods.getRecommendGoodsId()));
            }
            // step2 构建 脚本查询参数
            String scriptString = String.format(scriptFormat,"ad_tags", null);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            goods.setModified(new Date());
            scriptString+=String.format(scriptFormat, "modified", "\""+sdf.format(goods.getModified())+"\"");
            log.info("script: {}", scriptString);
            // step3 执行操作
            updateByQueryAsyncPre(boolQueryBuilder,scriptString);
        }catch (Throwable e){
            log.error("更新失败:",e);
        }
        return res;
    }

    public  Map<String,Object> clearCouponUrl(RecommendGoods goods) {
        Map<String,Object> res = new HashMap<>();
        try{
            // step1 构建 bool query builder
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            {
                boolQueryBuilder.must(QueryBuilders.termQuery("recommend_goods_id",goods.getRecommendGoodsId()));
            }
            // step2 构建 脚本查询参数
            String scriptString = String.format(scriptFormat,"coupon_url", null);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            goods.setModified(new Date());
            scriptString+=String.format(scriptFormat, "modified", "\""+sdf.format(goods.getModified())+"\"");
            log.info("script: {}", scriptString);
            // step3 执行操作
            updateByQueryAsyncPre(boolQueryBuilder,scriptString);
        }catch (Throwable e){
            log.error("更新失败:",e);
        }
        return res;
    }

    public  Map<String,Object> updateInfoBySkuId(RecommendGoods goods) {
        Map<String,Object> res = new HashMap<>();
        try{
            // step1 构建 bool query builder
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            {
                boolQueryBuilder.must(QueryBuilders.termQuery("sku_id",goods.getSkuId()));
            }

            RecommendGoods tmp = new RecommendGoods();
            tmp.setJdPrice(goods.getJdPrice());
            tmp.setPinGouPrice(goods.getPinGouPrice());
            tmp.setCouponDiscount(goods.getCouponDiscount());
            tmp.setCommissionShare(goods.getCommissionShare());
            tmp.setCouponUrl(goods.getCouponUrl());
            tmp.setCouponTakeBeginTime(goods.getCouponTakeBeginTime());
            tmp.setCouponTakeEndTime(goods.getCouponTakeEndTime());
            tmp.setCouponUseBeginTime(goods.getCouponUseBeginTime());
            tmp.setCouponUseEndTime(goods.getCouponUseEndTime());
            tmp.setCouponQuota(goods.getCouponQuota());
            tmp.setGoodCommentsShare(goods.getGoodCommentsShare());
            tmp.setComments(goods.getComments());
            tmp.setImgUrlList(goods.getImgUrlList());
            tmp.setTags(goods.getTags());
            tmp.setAdTags(goods.getAdTags());
            goods.setModified(new Date());
            // step2 构建 脚本查询参数
            String scriptString = buildScript(tmp);
            log.info("script: {}", scriptString);

            // step3 执行操作
            updateByQueryAsyncPre(boolQueryBuilder,scriptString);
        }catch (Throwable e){
            log.error("更新失败:",e);
        }
        return res;
    }

    public  Map<String,Object> deleteByQuery(RecommendGoodsQuery goods){

        Map<String,Object> res = new HashMap<>();
        try{
            // step1 构建 bool query builder
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            {
                int modifyFlag=0;
                if(goods.getRecommendGoodsId()!=null){
                    modifyFlag++;
                    boolQueryBuilder.must(QueryBuilders.termQuery("recommend_goods_id",goods.getRecommendGoodsId()));
                }else if(goods.getShopId()!=null){
                    modifyFlag++;
                    boolQueryBuilder.must(QueryBuilders.termQuery("shop_id",goods.getShopId()));
                }else if(goods.getSkuId()!=null){
                    modifyFlag++;
                    boolQueryBuilder.must(QueryBuilders.termQuery("sku_id",goods.getSkuId()));
                }

                if(modifyFlag==0){
                    throw new UnsupportedOperationException("必修提供修改条件!!!!");
                }
            }
            deleteByQueryAsyncPre(boolQueryBuilder);
        }catch (Throwable e){
            log.error("删除失败:",e);
        }

        return res;
    }

    public  Map<String,Object> deleteByShopId(RecommendGoodsQuery goods){

        Map<String,Object> res = new HashMap<>();
        try {
            // step1 构建 bool query builder
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            {
                boolQueryBuilder.must(QueryBuilders.termQuery("shop_id",goods.getShopId()));
            }
            deleteByQueryAsyncPre(boolQueryBuilder);
        }catch (Throwable e){
            log.error("删除错误:",e);
        }
        return res;
    }

    public  Map<String,Object> deletePinGouPrice(RecommendGoods goods) {
        Map<String,Object> res = new HashMap<>();
        try {
            // step1 构建 bool query builder
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            {
                boolQueryBuilder.must(QueryBuilders.termQuery("sku_id", goods.getSkuId()));
            }

            // step2 构建 脚本查询参数
            String scriptString = String.format(scriptFormat,"pin_gou_price",null);
            log.info("script: {}", scriptString);

            // step3 执行操作
            updateByQueryAsyncPre(boolQueryBuilder,scriptString);
        }catch (Throwable e){
            log.error("更新error",e);
        }
        return res;
    }

    public  Map<String,Object> updateStatusBySkuId(RecommendGoods goods) {
        RecommendGoods tmp = new RecommendGoods();
        tmp.setStatus(goods.getStatus());
        tmp.setBelowTime(goods.getBelowTime());
        tmp.setBelowReason(goods.getBelowReason());
        tmp.setSkuId(goods.getSkuId());
        return updateByQuery(tmp);
    }

    public  Map<String,Object> updateRecommendsIsInspection(RecommendGoods goods) {
        RecommendGoods tmp = new RecommendGoods();
        tmp.setShelfTime(goods.getShelfTime());
        tmp.setIsInspection(goods.getIsInspection());
        tmp.setSkuId(goods.getSkuId());
        return updateByQuery(tmp);
    }

    public  Map<String,Object> updateById(RecommendGoods goods)  {
        RecommendGoods tmp = new RecommendGoods();
        tmp.setRecoChannel(goods.getRecoChannel());
        tmp.setRecommendGoodsId(goods.getRecommendGoodsId());
        return updateByQuery(tmp);
    }

    public  Map<String,Object> updateStatusByShopId(RecommendGoods goods) {
        RecommendGoods tmp = new RecommendGoods();
        tmp.setStatus(goods.getStatus());
        tmp.setShopId(goods.getShopId());
        return updateByQuery(tmp);
    }

    public  Map<String, Object> find(RecommendGoodsQuery goods, boolean fetchSource) {
        Map<String,Object> map = new HashMap<>();
        map.put("code", -1);
        try {
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.CHINA);

            // 1 构建搜索的 source  build SearchSourceBuilder  ，布尔查询构建 query builder
            BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
            {
                //基本搜索字段
                searchSourceBuilder
                        .timeout(new TimeValue(20, TimeUnit.SECONDS))
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
                    boolQueryBuilder.must(QueryBuilders.existsQuery("tags"));
                }

                //status
                //                and status = #{status}
                if(goods.getStatus() != null){
                    boolQueryBuilder.must(QueryBuilders.termQuery("status",goods.getStatus()));
                }

                //adTags
                //                and FIND_IN_SET(#{adTags},ad_tags)
                if(StringUtils.isNotBlank(goods.getAdTags())){
                    boolQueryBuilder.must(QueryBuilders.existsQuery("ad_tags"));
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
            log.info("(es es es es es es es es es es es es es)构建 bool query builder is ::: -> {}", boolQueryBuilder.toString(true));
            // 3 执行搜索,返回结果
            SearchRequest request = new SearchRequest();
            request.indices(index);
            request.types(type);
            request.source(searchSourceBuilder);
            SearchResponse response;


            response = httpClient.search(request, RequestOptions.DEFAULT);
            if(fetchSource){
                List<RecommendGoods> data = Lists.newArrayList();
                List<Object> list = new ArrayList<>();
                map.put("data", data);
                for (SearchHit hit : response.getHits()) {
                    Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                    RecommendGoods recommendGoods = BeanUtil.mapToBeanIgnoreCase(sourceAsMap, RecommendGoods.class, true);
            Object tmp;
//            if((tmp = sourceAsMap.get("coupon_take_begin_time"))!=null){
//                recommendGoods.setCouponTakeBeginTime(sdf.parse(tmp.toString()));
//            }
//            if((tmp = sourceAsMap.get("coupon_take_end_time"))!=null){
//                recommendGoods.setCouponTakeEndTime(sdf.parse(tmp.toString()));
//            }
//            if((tmp = sourceAsMap.get("coupon_use_begin_time"))!=null){
//                recommendGoods.setCouponUseBeginTime(sdf.parse(tmp.toString()));
//            }
//            if((tmp = sourceAsMap.get("coupon_use_end_time"))!=null){
//                recommendGoods.setCouponUseEndTime(sdf.parse(tmp.toString()));
//            }
//            if((tmp = sourceAsMap.get("shelf_time"))!=null){
//                recommendGoods.setShelfTime(sdf.parse(tmp.toString()));
//            }
//            if((tmp = sourceAsMap.get("below_time"))!=null){
//                recommendGoods.setBelowTime(sdf.parse(tmp.toString()));
//            }
//            if((tmp = sourceAsMap.get("created"))!=null){
//                recommendGoods.setCreated(sdf.parse(tmp.toString()));
//            }
//            if((tmp = sourceAsMap.get("modified"))!=null){
//                recommendGoods.setModified(sdf.parse(tmp.toString()));
//            }
            if((tmp = sourceAsMap.get("img_url_list"))!=null && tmp.toString().length()>0){
                recommendGoods.setImageUrlList(Arrays.asList(tmp.toString().split(",")));
            }
                    boolean stepTag = true;
                    boolean stepAdTag = true;
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

                    if(stepTag && StringUtils.isNotBlank(goods.getAdTags())){
                        if(StringUtils.isNotBlank(recommendGoods.getAdTags())) adTagFlag:{
                            for (String adTag : recommendGoods.getAdTags().split(",")) {
                                if(adTag.equals(goods.getAdTags())){
                                    break adTagFlag;
                                }
                            }
                            stepAdTag = false;
                        }
                    }

                    if(stepAdTag && stepTag){
                        data.add(recommendGoods);
                        list.add(recommendGoods.getRecommendGoodsId());
                    }
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

    public  Map<String, Object> queryAndDelete(RecommendGoodsQuery goods) {
        Map<String,Object> map = new HashMap<>();
        map.put("code", -1);
        try {
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.CHINA);

            // 1 构建搜索的 source  build SearchSourceBuilder  ，布尔查询构建 query builder
            BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
            {
                //基本搜索字段
                searchSourceBuilder
                        .size(goods.getPageSize())
                        .timeout(new TimeValue(10, TimeUnit.SECONDS))
                        .sort(new FieldSortBuilder("modified").order(SortOrder.ASC))
                        //default get source
                        .fetchSource(true)
                        .fetchSource(new String[]{"recommend_goods_id"}, new String[0])
                        .query(boolQueryBuilder);
            }

            // 2 构建搜索条件
            {
                //goodType
                //                and good_type = #{goodType}
                boolQueryBuilder.must(QueryBuilders.termQuery("good_type", goods.getGoodType()));

                //status
                //                and status = #{status}
                boolQueryBuilder.must(QueryBuilders.termQuery("status", goods.getStatus()));

                //modified
                //        and modified &lt; #{cleanDate}
                boolQueryBuilder.must(QueryBuilders.rangeQuery("modified").lt(goods.getCleanDate()));
            }
            // 3 执行搜索,返回结果
            SearchRequest request = new SearchRequest();
            request.indices(index);
            request.types(type);
            request.source(searchSourceBuilder);
            httpClient.searchAsync(request, RequestOptions.DEFAULT, new ActionListener<SearchResponse>() {
                @Override
                public void onResponse(SearchResponse searchResponse) {
                    List<String> ids = Lists.newArrayList();
                    final SearchHits hits = searchResponse.getHits();
                    for (SearchHit hit : hits.getHits()) {
                        ids.add(hit.getSourceAsMap().get("recommend_goods_id").toString());
                    }
                    deleteByIds(ids);
                }
                @Override
                public void onFailure(Exception e) {
                    log.error("delete by query have exp:",e);
                }
            });
        } catch (Exception e) {
            log.error("find error:", e);
            map.put("data",Collections.emptyList());
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

    public  Map<String,Object> batchUpdateBySku(RecommendGoods goods)  {

        Map<String,Object> res = new HashMap<>();
        try{
            // step1 构建 bool query builder
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            {
                int modifyFlag=0;
                if(!CollectionUtils.isEmpty(goods.getSkuIdSet())){
                    modifyFlag++;
                    boolQueryBuilder.must(QueryBuilders.termsQuery("sku_id",goods.getSkuIdSet()));
                }
                if(modifyFlag==0){
                    throw new UnsupportedOperationException("必修提供修改条件!!!!");
                }
            }

            // step2 构建 脚本查询参数
            StringBuilder sb = new StringBuilder();
            {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                if(goods.getStatus()!=null){
                    sb.append(String.format(scriptFormat, "status", goods.getStatus()));
                    if(goods.getStatus()==-2){
                        sb.append(String.format(scriptFormat, "tags", null));
                        sb.append(String.format(scriptFormat, "ad_tags", null));
                    }
                }

                goods.setModified(new Date());
                sb.append(String.format(scriptFormat, "modified", "\""+sdf.format(goods.getModified())+"\""));

                log.info("script: {}", sb.toString());
            }

            // step3 执行操作
            updateByQueryAsyncPre(boolQueryBuilder,sb.toString());
        }catch (Throwable e){
            log.error("批量更新失败:",e);
        }
        return res;
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

    public  Map<String, Object> updateCoupanList(List<RecommendGoods> datas){
        Map<String,Object> map = new HashMap<>();
        map.put("code",-1);
        try{
            if(CollectionUtils.isEmpty(datas)){
                return map;
            }
            BulkRequest request = new BulkRequest();
            for (RecommendGoods goods : datas) {
                UpdateRequest updateRequest = new UpdateRequest(index,type,goods.getRecommendGoodsId()+"");
                StringBuilder sb = new StringBuilder();
                sb.append(String.format(scriptFormat, "coupon_num", goods.getCouponNum()+"L"));
                sb.append(String.format(scriptFormat, "coupon_remain_num", goods.getCouponRemainNum()+"L"));
                sb.append(String.format(scriptFormat, "coupon_num_reduce_last10m", goods.getCouponNumReduceLast10m()+"L"));
                sb.append(String.format(scriptFormat, "coupon_num_reduce_last2h", goods.getCouponNumReduceLast2h()+"L"));
                sb.append(String.format(scriptFormat, "coupon_num_reduce_last24h", goods.getCouponNumReduceLast24h()+"L"));
                String scriptString = sb.toString();
                updateRequest.script(new Script(scriptString));
                request.add(updateRequest);
            }
//            for(int i = 1;i<4;i++){
//                try {
//                    response = httpClient.bulk(request,RequestOptions.DEFAULT);
//                    for (BulkItemResponse responseItem : response.getItems()) {
//                        responseItem.isFailed();
//                    }
//                    break;
//                } catch (IOException e) {
//                    log.warn("update error retry once !");
//                }
//            }
            this.bulkAsync(request);

            map.put("code",200);
        }catch (Throwable e){
            log.error("批量失败:",e);
        }
        return map;
    }

    public  Map<String, Object> updateCoupan(RecommendGoods goods){
        Map<String,Object> map = new HashMap<>();
        map.put("code",-1);
        try{
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
                    .must(QueryBuilders.termQuery("recommend_goods_id",goods.getRecommendGoodsId()));
            StringBuilder sb = new StringBuilder();
            sb.append(String.format(scriptFormat, "coupon_num", goods.getCouponNum()+"L"));
            sb.append(String.format(scriptFormat, "coupon_remain_num", goods.getCouponRemainNum()+"L"));
            sb.append(String.format(scriptFormat, "coupon_num_reduce_last10m", goods.getCouponNumReduceLast10m()+"L"));
            sb.append(String.format(scriptFormat, "coupon_num_reduce_last2h", goods.getCouponNumReduceLast2h()+"L"));
            sb.append(String.format(scriptFormat, "coupon_num_reduce_last24h", goods.getCouponNumReduceLast24h()+"L"));
            String scriptString = sb.toString();
            updateByQueryAsyncPre(queryBuilder,scriptString);
            map.put("code",200);
        }catch (Throwable e){
            log.error("更新失败:",e);
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

    public  Map<String,Object> updateFenxiangInspectionGoodsBySkuId(RecommendGoodsQuery goods)  {

        Map<String,Object> res = new HashMap<>();
        try{
            // step1 构建 bool query builder
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            {

                //            `is_inspection` = 1
                boolQueryBuilder.must(QueryBuilders.termQuery("is_inspection",1));

                //                 and `sku_id` = #{skuId}
                if(StringUtils.isNumeric(goods.getSkuId())){
                    boolQueryBuilder.must(QueryBuilders.termQuery("sku_id",Long.parseLong(goods.getSkuId())));
                }
                //                AND NOW() BETWEEN coupon_take_begin_time AND coupon_take_end_time
                Integer isInspectioneffective = goods.getIsInspectioneffective();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.CHINA);
                if(isInspectioneffective != null && isInspectioneffective == 1){
                    String now = sdf.format(new Date());
                    boolQueryBuilder.must(QueryBuilders.rangeQuery("coupon_take_begin_time").lte(now));
                    boolQueryBuilder.must(QueryBuilders.rangeQuery("coupon_take_end_time").gte(now));
                    //                AND `shelf_time` &lt;= NOW()
                    boolQueryBuilder.must(QueryBuilders.rangeQuery("shelf_time").lte(now));
                }

            }

            // step2 构建 脚本查询参数
            RecommendGoods realGoods = new RecommendGoods();
            realGoods.setInspectionTop(goods.getInspectionTop());
            realGoods.setShelfTime(goods.getShelfTime());
            realGoods.setModified(new Date());
            String scriptString = buildScript(realGoods);
            Script script = new Script(scriptString);
            log.info("script: {}", scriptString);

            // step3 执行操作
            updateByQueryAsyncPre(boolQueryBuilder,scriptString);
        }catch (Throwable e){
            log.error("更新失败:",e);
        }
        return res;
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

    public void deleteByIds(List<String> ids){
        BulkRequest bulkRequest = new BulkRequest();
        for (String id : ids) {
            DeleteRequest deleteRequest = new DeleteRequest(index, type, id);
            bulkRequest.add(deleteRequest);
        }
        bulkAsync(bulkRequest);
    }

    //集成所有的更新操作
    private void updateByQueryAsyncPre(QueryBuilder queryBuilder, String scriptString){
        UpdateByQueryRequest updateByQueryRequest = new UpdateByQueryRequest(index);
        //默认情况下，版本冲突会中止UpdateByQueryRequest进程 = abortOnVersionConflict(false)
        updateByQueryRequest.setConflicts("abort");
        //条件
        updateByQueryRequest.setQuery(queryBuilder);
        //脚本
        updateByQueryRequest.setScript(new Script(scriptString));
        //timeout
        updateByQueryRequest.setTimeout(TimeValue.timeValueSeconds(5));
        //刷新索引
        updateByQueryRequest.setRefresh(true);
        //索引相关的参数规则
        updateByQueryRequest.setIndicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);
//            for(int i = 1;i<4;i++){
//                try {
//                    response = httpClient.updateByQuery(updateByQueryRequest, RequestOptions.DEFAULT);
//                    res.put("took",response.getTook().getMillis());
//                    res.put("status",response.getStatus().toString());
//                    break;
//                } catch (IOException e) {
//                    log.warn("update error retry once !");
//                }
//            }
        updateByQueryAsync(updateByQueryRequest);
    }

    //集成所有的删除操作
    private void deleteByQueryAsyncPre(QueryBuilder queryBuilder){
        DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(index);
        //默认情况下，版本冲突会中止UpdateByQueryRequest进程 = abortOnVersionConflict(false)
        deleteByQueryRequest.setConflicts("abort");
        //类型
        deleteByQueryRequest.setDocTypes(type);
        //条件
        deleteByQueryRequest.setQuery(queryBuilder);
        //脚本
        //timeout
        deleteByQueryRequest.setTimeout(TimeValue.timeValueSeconds(5));
        //刷新索引
        deleteByQueryRequest.setRefresh(true);
        //索引相关的参数规则
        deleteByQueryRequest.setIndicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);

//            for(int i =1; i < 4; i++){
//                try{
//                    response = httpClient.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);
//                    res.put("took",response.getTook().getMillis());
//                    res.put("status",response.getStatus().toString());
//                    break;
//                } catch (IOException e) {
//                    log.warn("delete error retry once !");
//                }
//            }
        this.deleteByQueryAsync(deleteByQueryRequest);
    }

    //更新异步调用
    private void updateByQueryAsync(final UpdateByQueryRequest updateByQueryRequest) {
        //第三次失败回调发送邮件
        final ActionListener<BulkByScrollResponse> ACTION_LISTENER_3 = new ActionListener<BulkByScrollResponse>() {
            @Override
            public void onResponse(BulkByScrollResponse bulkByScrollResponse) {}

            @Override
            public void onFailure(Exception e) {
                //TODO: send mail
            }
        };
        //第二次失败回调第三次
        final ActionListener<BulkByScrollResponse> ACTION_LISTENER_2 = new ActionListener<BulkByScrollResponse>() {
            @Override
            public void onResponse(BulkByScrollResponse bulkByScrollResponse) {
                log.info("updateByQueryAsync success in 2 try");
            }
            @Override
            public void onFailure(Exception e) {
                log.error("updateByQueryAsync error 2 time :",e);
                try {
                    //2s后重试
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException ex) {}
                RecommendGoodsEsRepository_2.this.httpClient.updateByQueryAsync(updateByQueryRequest, RequestOptions.DEFAULT, ACTION_LISTENER_3);
            }
        };
        //第一次失败回调第二次
        final ActionListener<BulkByScrollResponse> ACTION_LISTENER_1 = new ActionListener<BulkByScrollResponse>() {
            @Override
            public void onResponse(BulkByScrollResponse bulkByScrollResponse) {
                log.info("updateByQueryAsync success in 1 try");
            }
            @Override
            public void onFailure(Exception e) {
                log.error("updateByQueryAsync error 1 time:",e);
                try {
                    //2s后重试
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException ex) {}
                RecommendGoodsEsRepository_2.this.httpClient.updateByQueryAsync(updateByQueryRequest, RequestOptions.DEFAULT, ACTION_LISTENER_2);
            }
        };
        this.httpClient.updateByQueryAsync(updateByQueryRequest, RequestOptions.DEFAULT, ACTION_LISTENER_1);
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
        }
    }

    //删除异步调用
    private void deleteByQueryAsync(final DeleteByQueryRequest deleteByQueryRequest){
        final ActionListener<BulkByScrollResponse> ACTION_LISTENER_3 = new ActionListener<BulkByScrollResponse>() {
            @Override
            public void onResponse(BulkByScrollResponse bulkByScrollResponse) {}
            @Override
            public void onFailure(Exception e) {
                //TODO: send mail
            }
        };
        final ActionListener<BulkByScrollResponse> ACTION_LISTENER_2 = new ActionListener<BulkByScrollResponse>() {
            @Override
            public void onResponse(BulkByScrollResponse bulkByScrollResponse) {
                log.info("deleteByQueryAsync success in 2 try");
            }
            @Override
            public void onFailure(Exception e) {
                log.error("deleteByQueryAsync error 2 time:",e);
                try {
                    //2s后重试
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException ex) {}
                RecommendGoodsEsRepository_2.this.httpClient.deleteByQueryAsync(deleteByQueryRequest, RequestOptions.DEFAULT, ACTION_LISTENER_3);
            }
        };
        final ActionListener<BulkByScrollResponse> ACTION_LISTENER_1 = new ActionListener<BulkByScrollResponse>() {
            @Override
            public void onResponse(BulkByScrollResponse bulkByScrollResponse) {
                log.info("deleteByQueryAsync success in 1 try");
            }
            @Override
            public void onFailure(Exception e) {
                log.error("deleteByQueryAsync error 1 time:",e);
                try {
                    //2s后重试
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException ex) {}
                RecommendGoodsEsRepository_2.this.httpClient.deleteByQueryAsync(deleteByQueryRequest, RequestOptions.DEFAULT, ACTION_LISTENER_2);
            }
        };
        this.httpClient.deleteByQueryAsync(deleteByQueryRequest, RequestOptions.DEFAULT, ACTION_LISTENER_1);
    }

    //批量操作异步调用
    private void bulkAsync(final BulkRequest bulkRequest){
        final ActionListener<BulkResponse> ACTION_LISTENER_3 = new ActionListener<BulkResponse>() {
            @Override
            public void onResponse(BulkResponse bulkByScrollResponse) {}
            @Override
            public void onFailure(Exception e) {
                //TODO: send mail
            }
        };
        final ActionListener<BulkResponse> ACTION_LISTENER_2 = new ActionListener<BulkResponse>() {
            @Override
            public void onResponse(BulkResponse bulkByScrollResponse) {
                log.info("bulkAsync success in 2 try");
            }
            @Override
            public void onFailure(Exception e) {
                log.error("bulkAsync error 2 time:",e);
                try {
                    //2s后重试
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException ex) {}
                RecommendGoodsEsRepository_2.this.httpClient.bulkAsync(bulkRequest, RequestOptions.DEFAULT, ACTION_LISTENER_3);
            }
        };
        final ActionListener<BulkResponse> ACTION_LISTENER_1 = new ActionListener<BulkResponse>() {
            @Override
            public void onResponse(BulkResponse bulkByScrollResponse) {
                log.info("bulkAsync success in 1 try");
            }
            @Override
            public void onFailure(Exception e) {
                log.error("bulkAsync error 1 time:",e);
                try {
                    //2s后重试
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException ex) {}
                RecommendGoodsEsRepository_2.this.httpClient.bulkAsync(bulkRequest, RequestOptions.DEFAULT, ACTION_LISTENER_2);
            }
        };
        this.httpClient.bulkAsync(bulkRequest, RequestOptions.DEFAULT, ACTION_LISTENER_1);
        try{
            TimeUnit.SECONDS.sleep(3);
        }catch (Exception e){
        }
    }

    private XContentBuilder buildContent(RecommendGoods goods) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return jsonBuilder()
                    .startObject()
                    .field("recommend_goods_id", goods.getRecommendGoodsId())
                    .field("sku_id", goods.getSkuId())
                    .field("sku_name", goods.getSkuName())
                    .field("material_url", goods.getMaterialUrl())
                    .field("jd_price", goods.getJdPrice())
                    .field("image_url", goods.getImageUrl())
                    .field("img_url_list", goods.getImageUrlList()==null ? null : goods.getImageUrlList().toString().substring(1,goods.getImageUrlList().toString().length()-1))
                    .field("cid1", goods.getCid1())
                    .field("cid2", goods.getCid2())
                    .field("cid3", goods.getCid3())
                    .field("coupon_url", goods.getCouponUrl())
                    .field("coupon_take_begin_time", goods.getCouponTakeBeginTime() == null ? null : sdf.format(goods.getCouponTakeBeginTime()))
                    .field("coupon_take_end_time", goods.getCouponTakeEndTime() == null ? null : sdf.format(goods.getCouponTakeEndTime()))
                    .field("coupon_use_begin_time", goods.getCouponUseBeginTime() == null ? null : sdf.format(goods.getCouponUseBeginTime()))
                    .field("coupon_use_end_time", goods.getCouponUseEndTime() == null ? null : sdf.format(goods.getCouponUseEndTime()))
                    .field("coupon_price", goods.getCouponPrice())
                    .field("coupon_discount", goods.getCouponDiscount())
                    .field("coupon_quota", goods.getCouponQuota())
                    .field("commission", goods.getCommission())
                    .field("reasons", goods.getReasons())
                    .field("pin_gou_price", StringUtils.isBlank(goods.getPinGouPrice())? null : goods.getPinGouPrice())
                    .field("pingou_tm_count", goods.getPingouTmCount())
                    .field("pingou_url", StringUtils.isBlank(goods.getPingouUrl()) ? null : goods.getPingouUrl())
                    .field("commission_share", goods.getCommissionShare())
                    .field("top", goods.getTop())
                    .field("ac_top", goods.getAcTop())
                    .field("comments", goods.getComments())
                    .field("good_comments_share", goods.getGoodCommentsShare())
                    .field("in_order_count_30days", goods.getInOrderCount30Days())
                    .field("tags", StringUtils.isNotBlank(goods.getTags())?Arrays.asList(goods.getTags().split(",")):null)
                    .field("tags_time", goods.getTagsTime())
                    .field("owner", goods.getOwner())
                    .field("brand_code", goods.getBrandCode())
                    .field("brand_name", goods.getBrandName())
                    .field("spuid", goods.getSpuid()) // spuid shop_id
                    .field("shop_name", goods.getShopName())
                    .field("shop_id", goods.getShopId())
                    .field("show_in_page", goods.getShowInPage())
                    .field("ad_tags", StringUtils.isNotBlank(goods.getAdTags())?Arrays.asList(goods.getAdTags().split(",")):null)
                    .field("source", goods.getSource())
                    .field("good_type", goods.getGoodType())
                    .field("original_sku_name", goods.getOriginalSkuName())
                    .field("service_charge", goods.getServiceCharge())
                    .field("input_name", goods.getInputName())
                    .field("coupon_num", goods.getCouponNum())
                    .field("coupon_remain_num", goods.getCouponRemainNum())
                    .field("coupon_num_reduce_last10m", goods.getCouponNumReduceLast10m())
                    .field("coupon_num_reduce_last2h", goods.getCouponNumReduceLast2h())
                    .field("coupon_num_reduce_last24h", goods.getCouponNumReduceLast24h())
                    .field("recommend", goods.getRecommend())
                    .field("image_owner_url", goods.getImageOwnerUrl())
                    .field("is_inspection", goods.getIsInspection())
                    .field("shelf_time", goods.getShelfTime() == null ? null : sdf.format(goods.getShelfTime()))
                    .field("inspection_top", goods.getInspectionTop())
                    .field("below_reason", goods.getBelowReason())
                    .field("below_time", goods.getBelowTime() == null ? null : sdf.format(goods.getBelowTime()))
                    .field("status", goods.getStatus())
                    .field("created", goods.getCreated() == null ? null : sdf.format(goods.getCreated()))
                    .field("modified", goods.getModified() == null ? null : sdf.format(goods.getModified()))
                    .field("reco_channel", goods.getRecoChannel())
                    .field("coupons_flag", goods.getCouponsFlag())
                    .endObject();
        }catch (Exception e){
            log.error("build content error", e);
            return null;
        }
    }

    private String buildScript(RecommendGoods goods) {
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if(goods.getRecommendGoodsId()!=null){
            sb.append(String.format(scriptFormat, "recommend_goods_id", goods.getRecommendGoodsId()));
        }
        if(goods.getSkuId()!=null){
            sb.append(String.format(scriptFormat, "sku_id", goods.getSkuId() +"L"));
        }
        if(StringUtils.isNotBlank(goods.getSkuName())){
            sb.append(String.format(scriptFormat, "sku_name",  "\""+goods.getSkuName() +"\""));
        }
        if(StringUtils.isNotBlank(goods.getMaterialUrl())){
            sb.append(String.format(scriptFormat, "material_url", "\""+goods.getMaterialUrl() +"\""));
        }
        if(StringUtils.isNotBlank(goods.getJdPrice())){
            sb.append(String.format(scriptFormat, "jd_price",  "\""+goods.getJdPrice() +"\""));
        }
        if(StringUtils.isNotBlank(goods.getImageUrl())){
            sb.append(String.format(scriptFormat, "image_url", "\""+goods.getImageUrl() +"\""));
        }
        if(!CollectionUtils.isEmpty(goods.getImageUrlList())){
            final String lists = goods.getImageUrlList().toString();
            sb.append(String.format(scriptFormat, "img_url_list", "\""+lists.substring(0,lists.length()-1) +"\""));
        }
        if(goods.getCid1()!=null){
            sb.append(String.format(scriptFormat, "cid1", goods.getCid1() + "L"));
        }
        if(goods.getCid2()!=null){
            sb.append(String.format(scriptFormat, "cid2", goods.getCid2() +"L"));
        }
        if(goods.getCid3()!=null){
            sb.append(String.format(scriptFormat, "cid3", goods.getCid3() +"L"));
        }
        if(StringUtils.isNotBlank(goods.getCouponUrl())){
            sb.append(String.format(scriptFormat, "coupon_url", "\""+goods.getCouponUrl() +"\""));
        }
        if(goods.getCouponTakeBeginTime()!=null){
            sb.append(String.format(scriptFormat, "coupon_take_begin_time",  "\""+sdf.format(goods.getCouponTakeBeginTime()) +"\""));
        }
        if(goods.getCouponTakeEndTime()!=null){
            sb.append(String.format(scriptFormat, "coupon_take_end_time",  "\""+sdf.format(goods.getCouponTakeEndTime()) +"\""));
        }
        if(goods.getCouponUseBeginTime()!=null){
            sb.append(String.format(scriptFormat, "coupon_use_begin_time",  "\""+sdf.format(goods.getCouponUseBeginTime()) +"\""));
        }
        if(goods.getCouponUseEndTime()!=null){
            sb.append(String.format(scriptFormat, "coupon_use_end_time",  "\""+sdf.format(goods.getCouponUseEndTime()) +"\""));
        }
        if(StringUtils.isNotBlank(goods.getCouponPrice())){
            sb.append(String.format(scriptFormat, "coupon_price", "\""+goods.getCouponPrice() +"\""));
        }
        if(goods.getCouponDiscount()!=null){
            sb.append(String.format(scriptFormat, "coupon_discount", goods.getCouponDiscount() +"L"));
        }
        if(goods.getCouponQuota()!=null){
            sb.append(String.format(scriptFormat, "coupon_quota", goods.getCouponQuota()) + "L");
        }
        if(StringUtils.isNotBlank(goods.getCommission())){
            sb.append(String.format(scriptFormat, "commission", "\""+goods.getCommission()+"\""));
        }
        if(StringUtils.isNotBlank(goods.getReasons())){
            sb.append(String.format(scriptFormat, "reasons", "\""+goods.getReasons()+"\""));
        }
        if(StringUtils.isNotBlank(goods.getPinGouPrice())){
            sb.append(String.format(scriptFormat, "pin_gou_price", "\""+goods.getPinGouPrice()+"\""));
        }
        if(goods.getPingouTmCount()!=null){
            sb.append(String.format(scriptFormat, "pingou_tm_count", goods.getPingouTmCount() +"L"));
        }
        if(StringUtils.isNotBlank(goods.getPingouUrl())){
            sb.append(String.format(scriptFormat, "pingou_url", "\""+goods.getPingouUrl()+"\""));
        }
        if(StringUtils.isNotBlank(goods.getCommissionShare())){
            sb.append(String.format(scriptFormat, "commission_share", "\""+goods.getCommissionShare()+"\""));
        }
        if(goods.getTop()!=null){
            sb.append(String.format(scriptFormat, "top", goods.getTop() + "L"));
        }
        if(goods.getAcTop()!=null){
            sb.append(String.format(scriptFormat, "ac_top", goods.getAcTop() +"L"));
        }
        if(goods.getComments()!=null){
            sb.append(String.format(scriptFormat, "comments", goods.getComments()+"L"));
        }
        if(goods.getGoodCommentsShare()!=null){
            sb.append(String.format(scriptFormat, "good_comments_share", "\""+goods.getGoodCommentsShare()+"\""));
        }
        if(goods.getInOrderCount30Days()!=null){
            sb.append(String.format(scriptFormat, "in_order_count_30days", goods.getInOrderCount30Days() + "L"));
        }
        if(StringUtils.isNotBlank(goods.getTags())){
            sb.append(String.format(scriptFormat, "tags", "\""+goods.getTags()+"\""));
        }
        if(goods.getTagsTime()!=null){
            sb.append(String.format(scriptFormat, "tags_time", goods.getTagsTime() +"L"));
        }
        if(StringUtils.isNotBlank(goods.getOwner())){
            sb.append(String.format(scriptFormat, "owner", "\""+goods.getOwner()+"\""));
        }
        if(StringUtils.isNotBlank(goods.getBrandCode())){
            sb.append(String.format(scriptFormat, "brand_code", "\""+goods.getBrandCode()+"\""));
        }
        if(StringUtils.isNotBlank(goods.getBrandName())){
            sb.append(String.format(scriptFormat, "brand_name", "\""+goods.getBrandName()+"\""));
        }
        if(goods.getSpuid()!=null){
            sb.append(String.format(scriptFormat, "spuid", goods.getSpuid()));
        }
        if(goods.getShopId()!=null){
            sb.append(String.format(scriptFormat, "shop_id", goods.getShopId()));
        }
        if(StringUtils.isNotBlank(goods.getShopName())){
            sb.append(String.format(scriptFormat, "shop_name", "\""+goods.getShopName()+"\""));
        }
        if(goods.getShowInPage()!=null){
            sb.append(String.format(scriptFormat, "show_in_page", goods.getShowInPage()));
        }

        if(StringUtils.isNotBlank(goods.getAdTags())){
            sb.append(String.format(scriptFormat, "ad_tags", "\""+goods.getAdTags()+"\""));
        }

        if(StringUtils.isNotBlank(goods.getSource())){
            sb.append(String.format(scriptFormat, "source", "\""+goods.getSource()+"\""));
        }
        if(goods.getGoodType()!=null){
            sb.append(String.format(scriptFormat, "good_type", goods.getGoodType()));
        }
        if(StringUtils.isNotBlank(goods.getOriginalSkuName())){
            sb.append(String.format(scriptFormat, "original_sku_name", "\""+goods.getOriginalSkuName()+"\""));
        }
        if(goods.getServiceCharge()!=null){
            sb.append(String.format(scriptFormat, "service_charge", goods.getServiceCharge()));
        }
        if(StringUtils.isNotBlank(goods.getInputName())){
            sb.append(String.format(scriptFormat, "input_name", "\""+goods.getInputName()+"\""));
        }
        if(goods.getCouponNum()!=null){
            sb.append(String.format(scriptFormat, "coupon_num", goods.getCouponNum()+"L"));
        }
        if(goods.getCouponRemainNum()!=null){
            sb.append(String.format(scriptFormat, "coupon_remain_num", goods.getCouponRemainNum()+"L"));
        }
        if(goods.getCouponNumReduceLast10m()!=null){
            sb.append(String.format(scriptFormat, "coupon_num_reduce_last10m", goods.getCouponNumReduceLast10m()+"L"));
        }
        if(goods.getCouponNumReduceLast2h()!=null){
            sb.append(String.format(scriptFormat, "coupon_num_reduce_last2h", goods.getCouponNumReduceLast2h()+"L"));
        }
        if(goods.getCouponNumReduceLast24h()!=null){
            sb.append(String.format(scriptFormat, "coupon_num_reduce_last24h", goods.getCouponNumReduceLast24h()+"L"));
        }
        if(StringUtils.isNotBlank(goods.getRecommend())){
            sb.append(String.format(scriptFormat, "recommend", "\""+goods.getRecommend()+"\""));
        }
        if(StringUtils.isNotBlank(goods.getImageOwnerUrl())){
            sb.append(String.format(scriptFormat, "image_owner_url", "\""+goods.getImageOwnerUrl()+"\""));
        }
        if(goods.getIsInspection()!=null){
            sb.append(String.format(scriptFormat, "is_inspection", goods.getIsInspection()));
        }
        if(goods.getShelfTime()!=null){
            sb.append(String.format(scriptFormat, "shelf_time",  "\""+sdf.format(goods.getShelfTime())+"\""));
        }
        if(goods.getInspectionTop()!=null){
            sb.append(String.format(scriptFormat, "inspection_top", goods.getInspectionTop()));
        }
        if(StringUtils.isNotBlank(goods.getBelowReason())){
            sb.append(String.format(scriptFormat, "below_reason", "\""+goods.getBelowReason()+"\""));
            sb.append(String.format(scriptFormat, "below_time",  "\""+sdf.format(new Date())+"\""));
        }

        if(goods.getStatus()!=null){
            sb.append(String.format(scriptFormat, "status", goods.getStatus()));
        }
        if(goods.getCreated()!=null){
            sb.append(String.format(scriptFormat, "created",  "\""+sdf.format(goods.getCreated())+"\""));
        }
        if(goods.getModified()!=null){
            sb.append(String.format(scriptFormat, "modified",  "\""+sdf.format(goods.getModified())+"\""));
        }
        if(StringUtils.isNotBlank(goods.getRecoChannel())){
            sb.append(String.format(scriptFormat, "reco_channel", "\""+goods.getRecoChannel()+"\""));
        }
        if(goods.getCouponsFlag()!=null){
            sb.append(String.format(scriptFormat, "coupons_flag", goods.getCouponsFlag()+"L"));
        }
        return sb.toString();
    }


}
