package com.qianlima.demo.mongo.repository;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.model.Filters;
import com.qianlima.demo.mongo.SpringBootDemoMongodbApplicationTests;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description
 * @Author Lun_qs
 * @Date 2019/12/24 15:52
 * @Version 0.0.1
 */
@Slf4j
public class MongoClentTest extends SpringBootDemoMongodbApplicationTests {

    @Autowired
    private MongoClient mongoClient;

    @Test
    public void test1(){

        ArrayList<Document> documents = mongoClient.getDatabase("qlmdb")
                .getCollection("notice_info")
                .find(Filters.eq("id", 165771687))
                .projection(new Document("expandField", 1))
                .limit(1)
                .into(new ArrayList<>());

        List<String> stringList = new ArrayList<>(4);
        if(documents != null){
            for(Document doc : documents){//execute once
                JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(doc.get("expandField")));

                JSONArray tenderees = jsonObject.getJSONArray("tenderees");
                if(tenderees != null){
                    for(int idx = 0; idx < tenderees.size(); idx++){
                        String curr = tenderees.getString(idx);
                        if(StringUtils.isNotBlank(curr)){
                            stringList.add(curr);
                        }
                    }
                }

                JSONArray agents = jsonObject.getJSONArray("agents");
                if(agents != null){
                    for(int idx = 0; idx < agents.size(); idx++){
                        String curr = agents.getString(idx);
                        if(StringUtils.isNotBlank(curr)){
                            stringList.add(curr);
                        }
                    }
                }

                JSONArray winners = jsonObject.getJSONArray("winners");
                if(winners != null){
                    for(int idx = 0; idx < winners.size(); idx++){
                        JSONObject item = winners.getJSONObject(idx);
                        JSONArray bidderDetails = item.getJSONArray("bidderDetails");
                        if(bidderDetails != null){
                            for(int tmp = 0; tmp < bidderDetails.size(); tmp++){
                                JSONObject bidderInfo = bidderDetails.getJSONObject(tmp);
                                String curr = bidderInfo.getString("bidder");
                                if(StringUtils.isNotBlank(curr)){
                                    stringList.add(curr);
                                }
                            }
                        }
                    }
                }

            }
        }

        log.warn("公司名称: {}",stringList);
    }

    @Test
    //聚合
    public void test2(){}
}
