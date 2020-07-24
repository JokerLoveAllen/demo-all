package com.fenxiang.hbase.phoenix.batch;

import com.alibaba.fastjson.JSONObject;
import com.fenxiang.hbase.phoenix.domain.GoodsPushRecord;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.time.StopWatch;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * mysql.goods.url=jdbc:mysql://47.104.102.104:3306/goods?autoReconnect=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
 * mysql.information_schema.url=jdbc:mysql://47.104.102.104:3306/information_schema?autoReconnect=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
 * mysql.port=3306
 * mysql.username=goods_readonly
 * mysql.password=n1h3dnNfsWjlA69D
 */
@Slf4j
public class MysqlJdbc {
    private final JdbcTemplate jdbcTemplateGoods,jdbcTemplateInformation;
    private final DriverManagerDataSource dataSourceGoods, dataSourceInformation;
    private int maxLimit = 10000;
    public MysqlJdbc(){
        super();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        {
            dataSourceGoods = new DriverManagerDataSource();
            dataSourceGoods.setDriverClassName("com.mysql.jdbc.Driver");
            dataSourceGoods.setUrl("jdbc:mysql://47.104.102.104:3306/goods?autoReconnect=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false");
            dataSourceGoods.setUsername("goods_readonly");
            dataSourceGoods.setPassword("n1h3dnNfsWjlA69D");
            jdbcTemplateGoods = new JdbcTemplate(dataSourceGoods);
        }
        {
            dataSourceInformation = new DriverManagerDataSource();
            dataSourceInformation.setDriverClassName("com.mysql.jdbc.Driver");
            dataSourceInformation.setUrl("jdbc:mysql://47.104.102.104:3306/information_schema?autoReconnect=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false");
            dataSourceInformation.setUsername("goods_readonly");
            dataSourceInformation.setPassword("n1h3dnNfsWjlA69D");
            jdbcTemplateInformation = new JdbcTemplate(dataSourceInformation);
        }

        stopWatch.stop();
        log.info("获取到mysql连接!耗时:{} ms",stopWatch.getTime());
    }
    public MysqlJdbc(int maxLimit){
        this();
        this.maxLimit = maxLimit;
    }

    public ResultCursor<List<GoodsPushRecord>> batchGoods(String tname, int offset, int goods_push_id){

        try{
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            int start = goods_push_id, end = goods_push_id + maxLimit;
            String sql = String.format("select * from `goods`.`%s` where goods_push_id between %d and %d",tname, start, end);
            List<Map<String, Object>> list = jdbcTemplateGoods.queryForList(sql);
            List<GoodsPushRecord> res = list.stream().map(this::mappedFunc).collect(Collectors.toList());
            ResultCursor<List<GoodsPushRecord>> resultCursor =  new ResultCursor<>();
            if(CollectionUtils.isNotEmpty(res)){
                resultCursor.goods_push_id = res.get(res.size()-1).getGoods_push_id() + 1 ;
                resultCursor.offset = res.get(res.size()-1).getGoods_push_id() == end ? end : -1 ;
            } else {
                resultCursor.offset = resultCursor.goods_push_id = -1;
            }
            resultCursor.data = res;
            stopWatch.stop();
            log.info("Mysql查询耗时:{} ms",stopWatch.getTime());
            return resultCursor;
        }catch (Exception exp){
            log.error("批量mysql执行出错:",exp);
        }
        return new  ResultCursor<>(-2,null);
    }

    public List<String> batchTable(int offset, int limit){
        try{
            String sql = "SELECT TABLE_NAME from `TABLES` WHERE `TABLES`.TABLE_NAME like 'goods_push_%' ";
            sql += String.format("limit %d offset %d", limit, offset);
            List<String> strings = jdbcTemplateInformation.query(sql, new RowMapper<String>() {
                public String mapRow(ResultSet var1, int var2) throws SQLException {
                    return var1.getString(1);
                }
            });
            return strings;
        }catch (Exception e){
            log.error("获取数据表出现问题!!", e);
        }
        return Collections.emptyList();
    }

    @Setter
    @Getter
    @ToString
    public static class ResultCursor<T>{
        private int offset, goods_push_id;
        private T data;
        ResultCursor(){}
//        ResultCursor(int offset, T data){
//            this.offset = offset;
//            this.data = data;
//        }
        ResultCursor(int goods_push_id, T data){
            this.goods_push_id = goods_push_id;
            this.data = data;
        }
    }

   private GoodsPushRecord mappedFunc(Map<String, Object> e){
        JSONObject resultSet = new JSONObject(e);
        GoodsPushRecord record = new GoodsPushRecord();
        record.setUser_id(resultSet.getInteger("user_id"));
        record.setType(resultSet.getInteger("type"));
        record.setStatus(resultSet.getInteger("status"));
        record.setSource(resultSet.getString("source"));
        record.setSku_name(resultSet.getString("sku_name"));
        record.setSku_id(resultSet.getString("sku_id"));
        record.setRobot_id(resultSet.getInteger("robot_id"));
        record.setReasons(resultSet.getString("reasons"));
        record.setPromotion_text(resultSet.getString("promotion_text"));
        record.setPin_gou_price(resultSet.getString("pin_gou_price"));
        record.setModified(resultSet.getDate("modified"));
        record.setMessage_id(resultSet.getString("message_id"));
        record.setMaterial_url(resultSet.getString("material_url"));
        record.setJd_price(resultSet.getString( "jd_price"));
        record.setImg_url_List(resultSet.getString("img_url_List"));
        record.setImage_url(resultSet.getString("image_url"));
        record.setGroup_id(resultSet.getInteger("group_id"));
        record.setError_msg(resultSet.getString("error_msg"));
        record.setCreated(resultSet.getDate("created"));
        record.setCommission(resultSet.getString("commission"));
        record.setCommission_share(resultSet.getString("commission_share"));
        record.setCoupon_discount(resultSet.getString("coupon_discount"));
        record.setGoods_push_id(resultSet.getInteger("goods_push_id"));
        return record;
    }
}
