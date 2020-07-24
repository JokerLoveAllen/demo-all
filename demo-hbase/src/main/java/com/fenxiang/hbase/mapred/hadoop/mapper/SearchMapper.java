package com.fenxiang.hbase.mapred.hadoop.mapper;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

/**
 * @ClassName SearchMapper
 * @Author lqs
 * @Date 2020/5/8 11:01
 */
public class SearchMapper extends Mapper<LongWritable, Text, Text, LongWritable> {
    LongWritable one = null;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        if (one == null) {
            one = new LongWritable(1);
        }
    }

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        //接收到的每一行数据
        String line = value.toString();
        if(line.contains("/goods/search")){
            int startIdx = line.indexOf("request=");
            int endIdx = line.indexOf(", response=");
            if(endIdx>startIdx && startIdx > -1){
                String json = line.substring(startIdx + 8, endIdx);
                json = StringEscapeUtils.unescapeJava(json);
                final JSONObject jsonObject = JSONObject.parseObject(json);
                if(jsonObject.containsKey("keyword")){
                    context.write(new Text(jsonObject.getString("keyword")), one);
                }
            }
        }
    }

//    private String decode(String encStr){
//        String res = "";
//        try {
//            final String[] split = encStr.split("#");
//            for(String item : split){
//                if(item.length()>0)
//                    res += (char)Integer.valueOf(item,16).intValue();
//            }
//        }catch (Exception e){
//            System.out.println("__________________________________________-> " +encStr);
//            throw new RuntimeException(e);
//        }
//        return res;
//    }
//    public static void main(String[] args) {
//        String line = "调用 [ http://localhost:8003/goods/search ] , request= {\"isCoupon\":\"1\",\"keyword\":\"家装节\",\"pageIndex\":\"1\",\"pageSize\":\"20\"}, response= {\"msg\":\"成功\",\"code\":0,\"success\":true}, 耗时= 282 ms";
//        if(line.contains("/goods/search")){
//            int startIdx = line.indexOf("request=");
//            int endIdx = line.indexOf(", response=");
//            if(endIdx>startIdx && startIdx > -1){
//                final JSONObject jsonObject = JSONObject.parseObject(line.substring(startIdx + 8, endIdx));
//                System.out.println(jsonObject);
//            }
//        }
//    }
}
