package com.qianlima.demo.webflux.controller;

import com.qianlima.demo.webflux.domain.MyDomain;
import com.qianlima.demo.webflux.service.MyDomainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @Description
 * @Author Lun_qs
 * @Date 2019/7/15 16:25
 * @Version 0.0.1
 */
@RestController
@RequestMapping("/wf")
public class WebFluxDataController {
    @Autowired
    private MyDomainService service;

    @GetMapping("/sayHello/{v}")
    public Mono<String> sayHello(@PathVariable String v){
        if("1".equals(v)){
            throw new RuntimeException("抛个异常玩玩!");
        }
        return Mono.just("第一个WebFlux请求~");
    }

    @GetMapping("/list")
    public Flux<MyDomain> list(){
        Flux<MyDomain> res = service.getList();
        return res;
    }

    @GetMapping("/randomAdd")
    public Mono<Long> randomAdd(){
        return service.addRandomMyDomain();
    }

    @GetMapping("/get/{id}")
    public Mono<MyDomain> get(@PathVariable Long id){
        return service.getMyDomain(id);
    }
}
