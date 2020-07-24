package com.fenxiang.hbase.phoenix.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName DruidPhoenixDataSourceFactory
 * @Author lunqisehn
 * @Date 2020/4/15 10:18
 */
@Configuration
public class DruidDataSourceFactory extends UnpooledDataSourceFactory {

    public DruidDataSourceFactory(){
        DruidDataSource druidDataSource = new DruidDataSource();
        this.dataSource = druidDataSource;
    }
}
