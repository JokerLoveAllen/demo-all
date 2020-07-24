package com.fenxiang.hbase.phoenix.domain;

import com.fenxiang.hbase.phoenix.common.bean.Saveable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class GoodsPushRecord extends Saveable {
//    private String message_id,sku_id,sku_name,material_url,jd_price,image_url,
//            pin_gou_price,coupon_price,coupon_url,coupon_discount,
//            commission,reasons,promotion_text,source,error_msg
//            ,img_url_List,commission_share;
//    private Integer goods_push_id, user_id,robot_id,group_id,type,status;
//    private Date created,modified;
//    private Long unique_id;
    private Long uniqueId;
    private Integer goodsPushId;
    private String messageId;
    private Integer userId;
    private Integer groupId;
    private Integer robotId;
    private String skuId;
    private String skuName;
    private String materialUrl;
    private String imageUrl;
    private String jdPrice;
    private String pinGouPrice;
    private String couponPrice;
    private String couponUrl;
    private String couponDiscount;
    private String reasons;
    private String commission;
    private String source;
    private String errorMsg;
    private Integer type;
    private String promotionText;
    private String commissionShare;
    private String imgUrlList;
}