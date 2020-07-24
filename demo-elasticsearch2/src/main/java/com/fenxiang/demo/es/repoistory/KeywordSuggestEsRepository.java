package com.fenxiang.demo.es.repoistory;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * @ClassName KeywordSuggestEsRepoistory
 * @Author lqs
 * @Date 2020/5/25 17:33
 */
@Slf4j
@Repository
public class KeywordSuggestEsRepository {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Value("${elasticsearch.keyword.index:keyword_suggest}")
    private String index;
    @Value("${elasticsearch.keyword.type:suggest}")
    private String type;
    private final String searchSuggestName = "suggest";

    public Map<String,Object> upsert(String keyWord){
        Map<String,Object> map = new HashMap<>();
        try {
            final XContentBuilder contentBuilder = jsonBuilder().startObject()
                    .field("keyword", keyWord)
                    .field("createDate", System.currentTimeMillis())
                    .endObject();
            final UpdateRequest request1 = new UpdateRequest();
            request1.type(type).index(index).id(UUID.randomUUID().toString()).docAsUpsert(true).doc(contentBuilder);
            final UpdateResponse updateResponse = restHighLevelClient.update(request1, RequestOptions.DEFAULT);
            map.put("data",updateResponse.toString());
        } catch (IOException e) {
            log.error("插入异常: {} ",keyWord, e);
        }
        return map;
    }

    public Map<String,Object> searchSuggest(String searchKeyword){
        Map<String,Object> data = new HashMap<>();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //自动补全
        CompletionSuggestionBuilder suggestionBuilder =
                SuggestBuilders.completionSuggestion(index).prefix(searchKeyword)
                        .skipDuplicates(true).size(20);
        SuggestBuilder suggestBuilder = new SuggestBuilder();
        suggestBuilder.addSuggestion(searchSuggestName,suggestionBuilder);
        searchSourceBuilder.suggest(suggestBuilder);
        SearchRequest request = new SearchRequest();
        request.indices(index);
        request.types(type);
        request.source(searchSourceBuilder);
        Set<String> keySet = new HashSet<>();
        data.put("data",keySet);
        try {
            SearchResponse searchResponse = restHighLevelClient.search(request, RequestOptions.DEFAULT);
            final Suggest suggest = searchResponse.getSuggest();
            if(suggest!=null){
                final List<? extends Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option>> entries
                        = suggest.getSuggestion(searchSuggestName).getEntries();
                F0:for (Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option> entry : entries) {
                    for (Suggest.Suggestion.Entry.Option option : entry.getOptions()) {
                        final String keyword = option.getText().toString();
                        if(StringUtils.isNotBlank(keyword) && keyword.length() < 20){
                            if(searchKeyword.equals(keyword)){
                                continue;
                            }
                            keySet.add(keyword);
                            if(keySet.size()==10){
                                break F0;
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.error("搜索异常: {}",searchKeyword, e);
        }
        return data;
    }

    public Map<String,Object>  searchHot(){
        Integer size = 10;
        Map<String,Object> data = new HashMap<>();

        /** 获取最近一个月时间 */
        long preMonth = LocalDateTime.now().minusMonths(1).toInstant(ZoneOffset.of("+8")).toEpochMilli();
        long now = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();

        /** 统计最近一个月的热门搜索，长度最大10，方便显示 */
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder
                .aggregation(
                        AggregationBuilders.terms("hotSearch").field("keyword.key").size(size))
                .query(
                        QueryBuilders.rangeQuery("createDate").gte(preMonth).lte(now));

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.types(type).indices(index).source(searchSourceBuilder);
        try {
            final SearchResponse searchResponse;
            searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            Aggregations aggregations = searchResponse.getAggregations();
            Set<String> keywords = new HashSet<>();
            data.put("data",keywords);
            if (aggregations != null) {
                Terms hotSearch = aggregations.get("hotSearch");
                List<? extends Terms.Bucket> buckets = hotSearch.getBuckets();
                for (Terms.Bucket bucket: buckets) {
                    if (bucket.getKey().toString().length() <= 10) {
                        keywords.add((String)bucket.getKey());
                    }
                }
            }
        } catch (IOException e) {
            log.error("聚合搜索失败:",e);
        }
        return data;
    }
}
