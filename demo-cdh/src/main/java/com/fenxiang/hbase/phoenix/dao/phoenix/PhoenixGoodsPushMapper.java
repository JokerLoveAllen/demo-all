package com.fenxiang.hbase.phoenix.dao.phoenix;

import com.fenxiang.hbase.phoenix.domain.GoodsPushRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PhoenixGoodsPushMapper {
    void insertOne(GoodsPushRecord goodsPushRecord);
    Integer count(@Param("start") Long start, @Param("end") Long end);
}