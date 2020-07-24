package com.fenxiang.demo.es.common;

import cn.hutool.core.bean.BeanUtil;
import com.fenxiang.demo.es.domain.RecommendGoods;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @ClassName ReflectTest
 * @Author lqs
 * @Date 2020/5/7 10:24
 */
public class ReflectTest {
    public static void main(String[] args) {
//        final Class<RecommendGoods> recommendGoodsClass = RecommendGoods.class;
//        final Field[] declaredFields = recommendGoodsClass.getDeclaredFields();
//        for (Field declaredField : declaredFields) {
//            declaredField.setAccessible(true);
//            System.out.println(declaredField.getName());
//        }
//        System.out.println(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ").format(new Date()));
//
//        System.out.println(Long.toHexString(Long.MAX_VALUE));
//
//        System.out.println(Long.toUnsignedString(Long.MAX_VALUE, 36));
//
//
//        final HashMap<String, Object> in_order_count_30days = new HashMap<String, Object>() {{
//            put("sku_id", 1222220);
//            put("in_order_count_30days",1234);
//        }};
//
//        final RecommendGoods recommendGoods = BeanUtil.mapToBean(in_order_count_30days, RecommendGoods.class,true);
//        System.out.println(recommendGoods.getSkuId());
//        System.out.println(recommendGoods.getInOrderCount30Days());
//

        List<String> list = new ArrayList<>();
        for(int i = 0; i < 100; i++){
            list.add(i%8 == 0 ? null : (i + ".02"));
        }
        final String order ="aesc";
        list.sort((p1,p2)->{
            BigDecimal b1 = null, b2 = null;
            if(StringUtils.isNotBlank(p1)){
                b1 = new BigDecimal(p1);
            }
            if(StringUtils.isNotBlank(p2)){
                b2 = new BigDecimal(p2);
            }
            if(b1 == null){
                return b2 == null ? 0 : 1;
            }
            if(b2 == null){
                return -1;
            }
            return (order.equals("desc")? 1: -1) * b2.compareTo(b1);
        });
        System.out.println(list);
    }
}
