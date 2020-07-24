package com.qianlima.demo.mongo.repository;

import com.qianlima.demo.mongo.model.DataModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Description
 * @Author Lun_qs
 * @Date 2019/7/11 11:45
 * @Version 0.0.1
 */
@Repository
public interface DataRepository extends MongoRepository<DataModel,String> {
    DataModel findByName(String uid);

    void deleteByName(String uid);
}
