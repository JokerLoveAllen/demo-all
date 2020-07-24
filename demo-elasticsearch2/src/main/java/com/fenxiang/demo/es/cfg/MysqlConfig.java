package com.fenxiang.demo.es.cfg;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * @ClassName MysqlConfig
 * @Author lqs
 * @Date 2020/5/6 17:33
 */
@Configuration
public class MysqlConfig {
    @Value("${fenxiang.goods.driverClassName:com.mysql.jdbc.Driver}")
    private String driverClassName;
    @Value("${fenxiang.goods.url:jdbc:mysql://rr-m5eqoi7p78tz9kac7.mysql.rds.aliyuncs.com:3306/goods?autoReconnect=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false}")
    private String url;
    @Value("${fenxiang.goods.username:canal}")
    private String username;
    @Value("${fenxiang.goods.password:0bZfwqLJvykmoyNn}")
    private String password;
    @Bean("goods")
    public JdbcTemplate jdbcTemplate() {
            DriverManagerDataSource dataSourceGoods = new DriverManagerDataSource();
            dataSourceGoods.setDriverClassName(driverClassName);
            dataSourceGoods.setUrl(url);
            dataSourceGoods.setUsername(username);
            dataSourceGoods.setPassword(password);

//            dataSourceGoods.setDriverClassName("com.mysql.jdbc.Driver");
//            dataSourceGoods.setUrl("jdbc:mysql://bj-cdb-fzdtkqss.sql.tencentcdb.com:61711/goods?autoReconnect=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false");
//            dataSourceGoods.setUsername("dev");
//            dataSourceGoods.setPassword("fenxiang88!@#");

//            dataSourceGoods.setUrl("jdbc:mysql://47.104.102.104:3306/goods?autoReconnect=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false");
//            dataSourceGoods.setUsername("goods_readonly");
//            dataSourceGoods.setPassword("n1h3dnNfsWjlA69D");
            return new JdbcTemplate(dataSourceGoods);
        }
}
