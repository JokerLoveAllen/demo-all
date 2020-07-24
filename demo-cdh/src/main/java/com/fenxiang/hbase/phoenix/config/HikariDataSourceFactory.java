package com.fenxiang.hbase.phoenix.config;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory;
import org.springframework.context.annotation.Configuration;

/**
 *  使用Hikari数据源兼容 Phoenix Hbase 客户端
 */
//@Configuration
public class HikariDataSourceFactory extends UnpooledDataSourceFactory {

  public HikariDataSourceFactory() {
    this.dataSource = new HikariDataSource();
  }
}