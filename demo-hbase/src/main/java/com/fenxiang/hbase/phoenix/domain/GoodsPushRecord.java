package com.fenxiang.hbase.phoenix.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class GoodsPushRecord {
    private String message_id,sku_id,sku_name,material_url,jd_price,image_url,
            pin_gou_price,coupon_price,coupon_url,coupon_discount,
            commission,reasons,promotion_text,source,error_msg
            ,img_url_List,commission_share;
    private Integer goods_push_id, user_id,robot_id,group_id,type,status;
    private Date created,modified;
    private Long unique_id;
}