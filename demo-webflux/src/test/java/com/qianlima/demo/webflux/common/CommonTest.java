package com.qianlima.demo.webflux.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @Description
 * @Author Lun_qs
 * @Date 2019/7/16 9:34
 * @Version 0.0.1
 */
@Slf4j
public class CommonTest {

    public static void main(String[] args) {
        WebClient cc = WebClient.builder().baseUrl("http://special.qianlima.com").build();
        log.warn("{}",cc.get()
                .uri(uriBuilder ->
                        uriBuilder.path("/ss/api/data/getStatistics")
                                .queryParam("fromDate","2019-6-6")
                                .queryParam("toDate","2019-8-8")
                                .queryParam("userid","154623")
                                .queryParam("limit","1000")
                                .queryParam("page","1")
//                                    .queryParam("status","")
                                .build())
                .cookie("qlm_username","qianlima444")
                .cookie("qlm_password","m7o8jRm87mCgmmUoE3mopBgogouj7j73")
                .exchange().flatMap(res -> res.bodyToMono(String.class)).block());
    }
}
