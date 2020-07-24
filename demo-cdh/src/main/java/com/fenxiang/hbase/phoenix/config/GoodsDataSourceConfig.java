package com.fenxiang.hbase.phoenix.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * 商品库数据库连接
 */
//@Configuration
//@MapperScan(basePackages = "com.fenxiang.hbase.phoenix.dao.mysql.goods", sqlSessionTemplateRef  = "goodsSqlSessionTemplate")
public class GoodsDataSourceConfig {

    @Bean(name = "goodsDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.goods")
    @Primary
    public DataSource goodsDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "goodsSqlSessionFactory")
    @Primary
    public SqlSessionFactory goodsSqlSessionFactory(@Qualifier("goodsDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setConfigLocation(new PathMatchingResourcePatternResolver().getResource("classpath:mybatis/mybatis-goods-config.xml"));
        return bean.getObject();
    }

//    @Bean(name = "goodsTransactionManager")
//    @Primary
//    public DataSourceTransactionManager testTransactionManager(@Qualifier("goodsDataSource") DataSource dataSource) {
//        return new DataSourceTransactionManager(dataSource);
//    }

    @Bean(name = "goodsSqlSessionTemplate")
    @Primary
    public SqlSessionTemplate goodsSqlSessionTemplate(@Qualifier("goodsSqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

}