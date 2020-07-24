package com.fenxiang.hbase.phoenix.batch;

import com.fenxiang.hbase.phoenix.domain.GoodsPushRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.StopWatch;

import java.io.Closeable;
import java.sql.*;
import java.util.List;

@Slf4j
public class PhoenixJdbc implements Closeable{
    private int commitSize = 1000; // number of rows you want to commit per batch.
    private final Connection conn;
    private static final String driverClass = "org.apache.phoenix.jdbc.PhoenixDriver";
    private static final String jdbcUrl = "jdbc:phoenix:hbase1:2181";

    static {
        try {
            Class.forName(driverClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    private final String upsertSql = "upsert into my_schema.goods_push(message_id, user_id, robot_id, group_id, sku_id, sku_name, material_url, jd_price, image_url, pin_gou_price, coupon_price, coupon_url, coupon_discount, commission, reasons, promotion_text, source, error_msg, type, status, created, modified, img_url_List, commission_share, goods_push_id, unique_id) VALUES" +
            " (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public PhoenixJdbc() throws Exception{
        super();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        conn = DriverManager.getConnection(jdbcUrl);
        conn.setAutoCommit(false);
        stopWatch.stop();
        log.info("获取到hbase连接!耗时:{} ms",stopWatch.getTime());
    }

    public PhoenixJdbc(int commitSize) throws Exception{
        this();
        this.commitSize = commitSize;
    }

    public int executeBatch(String resource, int offset, int goods_push_id,List<GoodsPushRecord> lst){
        if(conn == null){
            throw new IllegalStateException("cannot get connection !");
        }
       int batchSize = 0;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
       try (PreparedStatement stmt = conn.prepareStatement(upsertSql)) {
           for (GoodsPushRecord item : lst) {
               //message_id, user_id, robot_id, group_id, sku_id, sku_name, material_url, jd_price, image_url, pin_gou_price, coupon_price, coupon_url, coupon_discount, commission,
               // reasons, promotion_text, source, error_msg, type, status, created, modified, img_url_List, commission_share
               stmt.setString(1,item.getMessage_id());
               stmt.setInt(2,item.getUser_id());
               stmt.setInt(3,item.getRobot_id());
               stmt.setInt(4,item.getGroup_id());
               stmt.setString(5,item.getSku_id());
               stmt.setString(6,item.getSku_name());
               stmt.setString(7,item.getMaterial_url());
               stmt.setString(8,item.getJd_price());
               stmt.setString(9,item.getImage_url());
               stmt.setString(10,item.getPin_gou_price());
               stmt.setString(11,item.getCoupon_price());
               stmt.setString(12,item.getCoupon_url());
               stmt.setString(13,item.getCoupon_discount());
               stmt.setString(14,item.getCommission());
               stmt.setString(15,item.getReasons());
               stmt.setString(16,item.getPromotion_text());
               stmt.setString(17,item.getSource());
               stmt.setString(18,item.getError_msg());
               stmt.setInt(19,item.getType());
               stmt.setInt(20,item.getStatus());
               stmt.setDate(21,item.getCreated() ==null? null: new Date(item.getCreated().getTime()));
               stmt.setDate(22,item.getModified() ==null? null: new Date(item.getModified().getTime()));
               stmt.setString(23,item.getImg_url_List());
               stmt.setString(24,item.getCommission_share());
               stmt.setInt(25,item.getGoods_push_id());
               stmt.setLong(26,item.getUnique_id());
               stmt.executeUpdate();
               batchSize++;
               if (batchSize % commitSize == 0) {
                   conn.commit();
               }
           }
           conn.commit(); // commit the last batch of records
           stopWatch.stop();
           log.info("phoenix 执行耗时:{} ms",stopWatch.getTime());

           return batchSize;
       }catch (Exception e){
            log.error("批量phoenix执行出错 table({}) , offset({}), goods_push_id_end({}):",resource, offset, goods_push_id, e);
       }
       return -1;
   }

   private static final long deleteGapFactor = 1000000000000L;
    public void deleteRange(long start, long end){
        long plainStart = start;
        try(PreparedStatement preparedStatement = conn.prepareStatement("delete from my_schema.goods_push where unique_id between ? and ?")){
            while(start + deleteGapFactor < end){
                preparedStatement.setLong(1,start);
                preparedStatement.setLong(2,start += deleteGapFactor);
                preparedStatement.execute();
            }
            if (start < end){
                preparedStatement.setLong(1,start);
                preparedStatement.setLong(2,end);
                preparedStatement.execute();
            }
        }catch (Exception e){
            log.error("删除 ({}), ({}) 异常",plainStart,end,e);
        }
    }

   @Override
   public final void close(){
       if(conn != null){
           try {
               conn.close();
           } catch (SQLException e) {
               e.printStackTrace();
           }
       }
   }
}
