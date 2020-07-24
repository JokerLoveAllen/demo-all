package com.fenxiang.demo.es.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @Description :
 * @Author : sujinbo
 * @Date : 2018/11/6 12:08
 */
@Data
public class RecommendGoods {

    private String _id;

    private Integer status;

    private Date created;

    private Date modified;

    //选品看板订单转化数超过1000的商品信息缓存键
    public static final String XUAN_PIN_KAN_BAN_ORDER_CONVERSIONS_MORE_THAN_1000_CACHE_KEY = "xuanPinKanBanOrderConversionsMoreThan1000";

    public static final Integer STATUS_COUPON_EXPIRE = -2;

    // 商家保存但是未提报的商品状态，需要商家保存之后，点击提报才能进入运营审核，运营审核通过才能上线
    public static final Integer SHOP_GOODS_STATUS = 5;

    // 商家提报的商品状态，此时进入运营审核，运营审核通过才能上线
    public static final Integer SHOP_GOODS_COMMIT_STATUS = 6;

    //审核驳回的商品状态
    public static final Integer SHOP_GOODS_REJECT_STATUS = 7;



    // 好评之王标记
    public static final String GOOD_COMMENT = "1";


    private Integer recommendGoodsId;

    private String tags;

    private String adTags;

    private Integer acTop;

    private Integer showInPage;

    private String oneServiceFee;

    // 放到父类BaseRecommendGoods
//    private Long couponNum;
//    private Long couponRemainNum;

    private Long couponNumReduceLast10m;

    private Long couponNumReduceLast2h;

    private Long couponNumReduceLast24h;

    private Long tagsTime;

    private Integer isInspection;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date shelfTime;

    private Integer inspectionTop;

    @JsonIgnore
    private Set<Long> skuIdSet;

    //商品下架原因
    private String belowReason;

    //下架时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date belowTime;

    // 新增字段-推荐频道
    private String recoChannel;

    //是否多文多图
    private Integer isMulti;


    public static final String OWNER_JD = "g";
    public static final String OWNER_POP = "p";

    /**
     * 芬香自有商品类型
     */
    public static final Integer GOODS_TYPE_OWNER = 1;
    /**
     * 联盟商品
     */
    public static final Integer GOODS_TYPE_UNION = 2;

    /**
     * 外采商品
     */
    public static final Integer GOODS_TYPE_OPEN = 3;

    /**
     * 商家自行提报的外采品
     */
    public static final Integer SHOP_GOODS_TYPE_OPEN = 4;

    public static final Integer IS_COUPONS_Flag = 1;
    public static final Integer IS_NOT_COUPONS_Flag = 2;


    // 佣金包赔
    public static final String COMMISSION_COMPENSATION = "1";

    // 佣金不包赔
    public static final String COMMISSION_NOT_COMPENSATION = "0";

    // 商品id
    private Long skuId;

    // 商品名
    private String skuName;

    // 商品落地页  （下单链接）
    private String materialUrl;

    // 图片路径
    private String imageUrl;

    // 评论数
    private Long comments;

    // 商品好评率
    private Double goodCommentsShare;

    // 30天引单量
    private Long inOrderCount30Days;

    // 一级栏目ID
    private Long cid1;

    // 二级栏目id
    private Long cid2;

    // 三级栏目id
    private Long cid3;

    // 推荐理由 非京东字字段
    private String reasons;

    // 置顶
    private Integer top;

    // 京东价
    private String jdPrice;

    // 拼购价
    private String pinGouPrice;

    // 拼购成团人数
    private Long pingouTmCount;

    // 拼购落地页url
    private String pingouUrl;

    // 券后价
    private String couponPrice;

    // 卷地址
    private String couponUrl;

    // 优惠券数量
    private Long couponNum;

    // 优惠券剩余数量
    private Long couponRemainNum;

    // 拼购开始时间
    private Date couponTakeBeginTime;

    // 拼购结束时间
    private Date couponTakeEndTime;

    private Date couponUseBeginTime;

    private Date couponUseEndTime;

    // 优惠券额度
    private Long couponDiscount;

    // 优惠券满减限制
    private Long couponQuota;

    // 佣金
    private String commission;

    // 拥挤比例
    private String commissionShare;

    // g=自营，p=pop
    private String owner;

    //品牌code
    private String brandCode;

    // 品牌名称
    private String brandName;

    // spuid，其值为同款商品的主skuid
    private Long spuid;

    // 店铺id
    private Integer shopId;

    // 店铺名
    private String shopName;

    // 1：招商  2：联盟
    private Integer goodType;

    private String originalSkuName;

    private String inputName;

    private String serviceCharge;

    private String recommend;

    private String imageOwnerUrl;

    // 存库 图片集合
    private String imgUrlList;

    private Integer couponsFlag;

    //以下字段不保存DB
    //返利金额
    private String rebate;

    private String source;

    private String thumbnailUrl;

    private String isJdSale;

    private List<String> imageUrlList;

    // 商品库存
    private Long stock;

    // 商家提报时间
    private String commitTime;

    // 视频地址
    private String videoUrl;

    //驳回原因
    private String rejectReason;

    // 渠道商家的渠道名字
    private String channelName;

}
