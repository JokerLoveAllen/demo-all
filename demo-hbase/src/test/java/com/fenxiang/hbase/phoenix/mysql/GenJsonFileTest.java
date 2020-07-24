package com.fenxiang.hbase.phoenix.mysql;

import com.alibaba.fastjson.JSONObject;
import com.fenxiang.hbase.phoenix.batch.MysqlJdbc;
import com.fenxiang.hbase.phoenix.domain.GoodsPushRecord;
import com.fenxiang.hbase.phoenix.utils.SnowFlakeGeneratorBatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @ClassName GenCsvFileTest
 * @Author lqs
 * @Date 2020/4/20 9:52
 */
@Slf4j
public class GenJsonFileTest {
    static final String filePathPrefix = "D:\\Hbase_Release\\batch_file\\";
    static final MysqlJdbc mysqlJdbc = new MysqlJdbc(100);
    static final String fileSplitor = ",";
    public static void main(String[] args) {
        try {
            genRange("20191224","goods_push_20200318",0,100,"20200318_0.json");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static void genRange(String startTime,String tablename,int push_goods_id, int total, String fileName) throws Exception{
        int writeCnt = 0;
        String dateString = tablename.substring(11);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        long currStamp = sdf.parse(dateString).getTime();
        long startStamp = sdf.parse(startTime).getTime();
        SnowFlakeGeneratorBatch generator = new SnowFlakeGeneratorBatch(startStamp, currStamp, 1, 1);
        MysqlJdbc.ResultCursor<List<GoodsPushRecord>> resultCursor = mysqlJdbc.batchGoods(tablename,0,push_goods_id);
        while(CollectionUtils.isNotEmpty(resultCursor.getData())){
            log.info("tablename:({}) offset:({})",tablename, resultCursor.getOffset());
            List<GoodsPushRecord> cursorData = resultCursor.getData();
            List<String> iterable = new ArrayList<>();
            if(CollectionUtils.isNotEmpty(cursorData)){
                for (GoodsPushRecord item : cursorData) {
                    if(writeCnt >= total){break;}
                    writeCnt++;
                    JSONObject object = new JSONObject();
                    object.put("unique_id",item.getUnique_id());
                    object.put("goods_push_id",item.getGoods_push_id());
                    object.put("message_id",item.getMessage_id());
                    object.put("user_id",item.getUser_id());
                    object.put("robot_id",item.getRobot_id());
                    object.put("group_id",item.getGroup_id());
                    object.put("sku_id",value(item.getSku_id()));
                    object.put("sku_name",value(item.getSku_name()));
                    object.put("material_url",value(item.getMaterial_url()));
                    object.put("jd_price",value(item.getJd_price()));
                    object.put("image_url",value(item.getImage_url()));
                    object.put("pin_gou_price",value(item.getPin_gou_price()));
                    object.put("coupon_price",value(item.getCoupon_price()));
                    object.put("coupon_url",value(item.getCoupon_url()));
                    object.put("coupon_discount",value(item.getCoupon_discount()));
                    object.put("commission",value(item.getCommission()));
                    object.put("reasons",value(item.getReasons()));
                    object.put("promotion_text",value(item.getPromotion_text()));
                    object.put("source",value(item.getSource()));
                    object.put("error_msg",value(item.getError_msg()));
                    object.put("img_url_List",value(item.getImg_url_List()));
                    object.put("commission_share",value(item.getCommission_share()));
                    object.put("type",value(item.getType()));
                    object.put("status",value(item.getStatus()));
                    object.put("created",date(item.getCreated()));
                    object.put("modified",date(item.getModified()));
                    iterable.add(object.toJSONString());
                }
                File f = new File(filePathPrefix + dateString);
                if(!f.exists()){
                    f.mkdir();
                }
                Files.write(Paths.get(filePathPrefix + dateString,fileName), iterable);
            }
            if(resultCursor.getOffset() > 0 && writeCnt < total){
                resultCursor = mysqlJdbc.batchGoods(tablename,resultCursor.getOffset(),resultCursor.getGoods_push_id());
            } else {
                break;
            }
        }
    }
    private static String value(Object s){
        if(Objects.isNull(s)){
            return "";
        }
        String ss = s.toString();
        return StringUtils.isBlank(ss) ? "" : ss.trim();
    }
    private static String date(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if(date == null){
            return "";
        }
        return sdf.format(date);
    }
}
