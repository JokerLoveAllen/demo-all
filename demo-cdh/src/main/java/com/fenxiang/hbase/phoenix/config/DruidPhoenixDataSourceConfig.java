package com.fenxiang.hbase.phoenix.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.ExceptionSorter;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @ClassName PhoenixDataSourceConfig
 * @Author zy
 * @Date 2020/4/15 10:41
 */
@Configuration
@MapperScan(basePackages = "com.fenxiang.hbase.phoenix.dao.phoenix.**", sqlSessionFactoryRef = "PhoenixSqlSessionFactory")
public class DruidPhoenixDataSourceConfig {
    @Value("${spring.datasource.druid.initial-size:5}")
    private Integer initialSize;
    @Value("${spring.datasource.druid.max-active:20}")
    private Integer maxActive;
    @Value("${spring.datasource.druid.max-wait:60000}")
    private Integer maxWait;
    @Value("${spring.datasource.druid.min-idle:5}")
    private Integer minIdle;
    @Value("${spring.datasource.druid.min-evictable-idle-time-millis:30000}")
    private Long minIdleTimeOut;
    @Value("${spring.datasource.druid.max-pool-prepared-statement-per-connection-size:20}")
    private Integer maxPoolPerSize;
    @Value("${spring.datasource.druid.time-between-eviction-runs-millis:60000}")
    private Integer timeBetweenEviction;
    @Value("${phoenix.driverClassName:org.apache.phoenix.jdbc.PhoenixDriver}")
    private String driverClassName;
    @Value("${phoenix.jdbcUrl}")
    private String jdbcUrl;

    @Bean(name = "PhoenixDataSource")
    public DataSource phoenixDataSource() throws IOException {
        DruidDataSourceFactory factory = new DruidDataSourceFactory();
        DruidDataSource dataSource = (DruidDataSource)factory.getDataSource();
        dataSource.setInitialSize(initialSize);
        dataSource.setMaxActive(maxActive);
        dataSource.setMinIdle(minIdle);
        dataSource.setMinEvictableIdleTimeMillis(minIdleTimeOut);
        dataSource.setMaxPoolPreparedStatementPerConnectionSize(maxPoolPerSize);
        dataSource.setTimeBetweenEvictionRunsMillis(timeBetweenEviction);
        dataSource.setMaxWait(maxWait);
        dataSource.setTestOnBorrow(false);
        dataSource.setTestOnReturn(false);
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(jdbcUrl);
        dataSource.setExceptionSorter(new PhoenixExceptionSorter());
        return dataSource;
    }

    @Bean(name = "PhoenixSqlSessionFactory")
    public SqlSessionFactory phoenixSqlSessionFactory(
            @Qualifier("PhoenixDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        ResourceLoader loader = new DefaultResourceLoader();
        String resource = "classpath:config/mybatis-config.xml";
        factoryBean.setConfigLocation(loader.getResource(resource));
        factoryBean.setSqlSessionFactoryBuilder(new SqlSessionFactoryBuilder());
        factoryBean.setConfigLocation(new PathMatchingResourcePatternResolver().getResource("classpath:mybatis/mybatis-phoenix-config.xml"));
//        factoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:sqlmap/phoenix/**.xml"));
        return factoryBean.getObject();
    }

    /**
      * 当网络断开或者数据库服务器Crash时，连接池里面会存在“不可用连接”，连接池需要
      * 一种机制剔除这些“不可用连接”。在Druid和JBoss连接池中，剔除“不可用连接”的机
      * 制称为ExceptionSorter，实现的原理是根据异常类型/Code/Reason/Message来识
      * 别“不可用连接”。没有类似ExceptionSorter的连接池，在数据库重启或者网络中断之
      * 后，不能恢复工作，所以ExceptionSorter是连接池是否稳定的重要标志。
      */
    class PhoenixExceptionSorter implements ExceptionSorter {

        @Override
        public boolean isExceptionFatal(SQLException e) {
            if (e.getMessage() == null || e.getMessage().contains("Connection is null or closed")) {
                return true;
            }
            return false;
        }

        @Override
        public void configFromProperties(Properties properties) {

        }
    }
}
