package com.fenxiang.hbase.phoenix.dao.phoenix;

import com.fenxiang.hbase.phoenix.domain.GoodsPushRecord;
import com.fenxiang.hbase.phoenix.domain.SearchKeyword;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PhoenixMapper {

    void insertOne(GoodsPushRecord goodsPushRecord);
    Integer count(@Param("start") String start, @Param("end") String end);
    List<SearchKeyword> keywordSearch(@Param("limit") Integer limit);
}