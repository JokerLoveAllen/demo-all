package com.qianlima.demo.webflux.service;

import com.qianlima.demo.webflux.dao.MyDomainRepository;
import com.qianlima.demo.webflux.domain.MyDomain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Random;

/**
 * @Description
 * @Author Lun_qs
 * @Date 2019/7/16 10:14
 * @Version 0.0.1
 */
@Component
public class MyDomainService {

    private final MyDomainRepository repository;
    private final Random random;
    private final char[] charCode;

    @Autowired
    public MyDomainService(MyDomainRepository repository){
        this.repository = repository;
        this.random = new Random(System.currentTimeMillis());
        this.charCode = new char[52];
        doInit();
    }

    private void doInit() {
        int i = 0;
        for(char c1 = 'a',c2 = 'A'; c1 <= 'z'; c1++, c2++){
            charCode[i++] = c1;
            charCode[i++] = c2;
        }
    }

    public Mono<MyDomain> getMyDomain(Long id){
        return Mono.justOrEmpty(repository.getMyDomain(id));
    }

    public Flux<MyDomain> getList(){
        return Flux.fromIterable(repository.getList());
    }

    public Mono<Long> addRandomMyDomain(){
        MyDomain newDomain = MyDomain.builder()
                .id(generateId())
                .A(generate(7))
                .B(generate(7))
                .D(generate(7))
                .C(generate(7))
                .build();
        return Mono.create(monoSink -> monoSink.success(repository.addMyDomain(newDomain)));
    }


    private String generate(int len){
        StringBuilder sb = new StringBuilder();
        while(len-->0){
            sb.append(charCode[random.nextInt(charCode.length)]);
        }
        return sb.toString();
    }

    private Long generateId(){
       return System.currentTimeMillis()*100 + random.nextInt(100);
    }
}
