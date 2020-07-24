package com.qianlima.demo.webflux;

import com.qianlima.demo.webflux.controller.WebFluxDataController;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @Description
 * @Author Lun_qs
 * @Date 2019/7/15 16:44
 * @Version 0.0.1
 */
@Slf4j
@RunWith(SpringRunner.class)
@WebFluxTest(controllers = WebFluxDataController.class)
public class WebFluxTestClazz {
    @Autowired
    private WebTestClient client;

    /**
     * 本系统内
     */
    @Test
    public void sayHello(){
        client.get()
                .uri("/wf/sayHello/2")
                .exchange()
                .expectBody(String.class)
                .value(System.out::println);
    }

    /**
     * 本系统内
     */
    @Test
    public void postReq(){
        client.post().uri("http://special.qianlima.com/ss/api/data/getStatistics")
                .attribute("fromDate","2019-6-6")
                .attribute("toDate","2019-8-8")
                .attribute("userid","154623")
                .attribute("limit","1000")
                .attribute("page","1")
                .attribute("status","")
                .cookie("qlm_username","qianlima444")
                .cookie("qlm_password","m7o8jRm87mCgmmUoE3mopBgogouj7j73")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .returnResult(Object.class).consumeWith(System.out::println);
    }

    /**
     * http 请求
     */
    @Test
    public void old(){
//      WebClient cc = WebClient.create("http://special.qianlima.com");
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
    //            .contentType(MediaType.APPLICATION_JSON_UTF8)
                .exchange().flatMap(res -> res.bodyToMono(String.class)).block());
    }
}
