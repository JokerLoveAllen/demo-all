package com.qianlima.demo.webflux.dao;

import com.qianlima.demo.webflux.domain.MyDomain;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description
 * @Author Lun_qs
 * @Date 2019/7/16 10:04
 * @Version 0.0.1
 */
@Repository
public class MyDomainRepository {

    private ConcurrentHashMap<Long, MyDomain> cmap = new ConcurrentHashMap<>();

    public MyDomain getMyDomain(Long id){
        return cmap.get(id);
    }

    public List<MyDomain> getList(){
        return new ArrayList<>(cmap.values());
    }

    public Long addMyDomain(MyDomain myDomain){
        cmap.put(myDomain.getId(),myDomain);
        return myDomain.getId();
    }
}
