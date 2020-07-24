package com.qianlima.demo.mongo.repository;

import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;

/**
 * @ClassName CommonTest
 * @Author lun qs
 * @Date 2020/6/23 11:18
 */
public class CommonTest {
    public static void main(String[] args) {
        Criteria wapAnalysisCriteria = Criteria.where("fromUrlCodes").exists(true);
        Aggregation wapAnalysisAggregation = Aggregation.newAggregation(Aggregation.match(wapAnalysisCriteria),
                Aggregation.project("fromUrlCodes"),
                Aggregation.unwind("fromUrlCodes"),
                Aggregation.group("fromUrlCodes.1").count().as("total"),
                Aggregation.project("fromUrlCodes.1")
        );
        System.out.println(wapAnalysisAggregation.toString());
    }
}
