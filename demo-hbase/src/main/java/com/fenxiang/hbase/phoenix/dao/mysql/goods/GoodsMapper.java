package com.fenxiang.hbase.phoenix.dao.mysql.goods;


import com.fenxiang.hbase.phoenix.domain.GoodsPushRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GoodsMapper{
    List<GoodsPushRecord>  list(@Param("tableName") String tableName,@Param("limit") Integer limit, @Param("offset") Integer offset);
}