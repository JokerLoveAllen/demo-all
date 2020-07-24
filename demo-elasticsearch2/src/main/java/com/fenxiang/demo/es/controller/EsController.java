package com.fenxiang.demo.es.controller;

import com.fenxiang.demo.es.domain.RecommendGoods;
import com.fenxiang.demo.es.service.RecommendGoodsEsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @ClassName EsController
 * @Author lqs
 * @Date 2020/5/6 17:38
 */
@Slf4j
@RestController
@RequestMapping("/api/v2")
public class EsController {

    @Autowired
    private RecommendGoodsEsService esService;

    @Autowired
    @Qualifier("goods")
    private JdbcTemplate jdbcTemplate;

    private volatile boolean ss;
    @GetMapping({"/import"})
    public synchronized Map<String,Object> importAll(){
        if(ss){
            return new HashMap<String, Object>(){{put("code","0"); put("data", "已经被执行");}};
        }
        ss = true;
        new Thread(()->{esService.importAll();ss=false;}).start();
        return new HashMap<String, Object>(){{put("code","200"); put("data", "succ");}};
    }

    private RecommendGoods fetchOne(Integer recommendId){
        if(recommendId!=null){
            return jdbcTemplate.queryForObject("select * from `recommend_goods` where recommend_goods_id = " + recommendId, ROW_MAPPER);
        }
        Random random = new Random(System.currentTimeMillis());
        return jdbcTemplate.queryForObject("select * from `recommend_goods` order by recommend_goods_id desc limit 1 offset " + random.nextInt(5000), ROW_MAPPER);
    }

    private static final RowMapper<RecommendGoods> ROW_MAPPER = (r1, var2) -> {
        RecommendGoods recommendGoods = new RecommendGoods();
        recommendGoods.setRecommendGoodsId(r1.getInt("recommend_goods_id"));
        recommendGoods.setSkuId(r1.getLong("sku_id"));
        recommendGoods.setSkuName(r1.getString("sku_name"));
        recommendGoods.setMaterialUrl(r1.getString("material_url"));
        recommendGoods.setJdPrice(r1.getString("jd_price"));
        recommendGoods.setImageUrl(r1.getString("image_url"));
        recommendGoods.setImageUrlList(StringUtils.isBlank(r1.getString("img_url_list")) ? null : new ArrayList<>());
        recommendGoods.setCid1(r1.getLong("cid1"));
        recommendGoods.setCid2(r1.getLong("cid2"));
        recommendGoods.setCid3(r1.getLong("cid3"));
        recommendGoods.setCouponUrl(r1.getString("coupon_url"));
        recommendGoods.setCouponTakeBeginTime(r1.getDate("coupon_take_begin_time"));
        recommendGoods.setCouponTakeEndTime(r1.getDate("coupon_take_end_time"));
        recommendGoods.setCouponUseBeginTime(r1.getDate("coupon_use_begin_time"));
        recommendGoods.setCouponUseEndTime(r1.getDate("coupon_use_end_time"));
        recommendGoods.setCouponPrice(r1.getString("coupon_price"));
        recommendGoods.setCouponDiscount(r1.getLong("coupon_discount"));
        recommendGoods.setCouponQuota(r1.getLong("coupon_quota"));
        recommendGoods.setCommission(r1.getString("commission"));
        recommendGoods.setReasons(r1.getString("reasons"));
        recommendGoods.setPinGouPrice(r1.getString("pin_gou_price"));
        recommendGoods.setPingouTmCount(r1.getLong("pingou_tm_count"));
        recommendGoods.setPingouUrl(r1.getString("pingou_url"));
        recommendGoods.setCommissionShare(r1.getString("commission_share"));
        recommendGoods.setTop(r1.getInt("top"));
        recommendGoods.setAcTop(r1.getInt("ac_top"));
        recommendGoods.setComments(r1.getLong("comments"));
        recommendGoods.setGoodCommentsShare(r1.getString("good_comments_share") == null ? null : Double.parseDouble(r1.getString("good_comments_share")));
        recommendGoods.setInOrderCount30Days(r1.getLong("in_order_count_30days"));
        recommendGoods.setTags(r1.getString("tags"));
        recommendGoods.setTagsTime(r1.getLong("tags_time"));
        recommendGoods.setOwner(r1.getString("owner"));
        recommendGoods.setBrandCode(r1.getString("brand_code"));
        recommendGoods.setBrandName(r1.getString("brand_name"));
        recommendGoods.setSpuid(r1.getString("spuid") == null ? null : Long.parseLong(r1.getString("spuid")));
        recommendGoods.setSkuName(r1.getString("shop_name"));
        recommendGoods.setShopId(r1.getString("shop_id") == null ? null : Integer.parseInt(r1.getString("shop_id")));
        recommendGoods.setAdTags(r1.getString("ad_tags"));
        recommendGoods.setSource(r1.getString("source"));
        recommendGoods.setGoodType(r1.getInt("good_type"));
        recommendGoods.setOriginalSkuName(r1.getString("original_sku_name"));
        recommendGoods.setServiceCharge(r1.getString("service_charge"));
        recommendGoods.setInputName(r1.getString("input_name"));
        recommendGoods.setCouponNum(r1.getLong("coupon_num"));
        recommendGoods.setCouponRemainNum(r1.getLong("coupon_remain_num"));
        recommendGoods.setCouponNumReduceLast10m(r1.getLong("coupon_num_reduce_last10m"));
        recommendGoods.setCouponNumReduceLast2h(r1.getLong("coupon_num_reduce_last2h"));
        recommendGoods.setCouponNumReduceLast24h(r1.getLong("coupon_num_reduce_last24h"));
        recommendGoods.setRecommend(r1.getString("recommend"));
        recommendGoods.setImageOwnerUrl(r1.getString("image_owner_url"));
        recommendGoods.setIsInspection(r1.getInt("is_inspection"));
        recommendGoods.setShelfTime(r1.getDate("shelf_time"));
        recommendGoods.setInspectionTop(r1.getInt("inspection_top"));
        recommendGoods.setBelowReason(r1.getString("below_reason"));
        recommendGoods.setBelowTime(r1.getDate("below_time"));
        recommendGoods.setStatus(r1.getInt("status"));
        recommendGoods.setCreated(r1.getDate("created"));
        recommendGoods.setModified(r1.getDate("modified"));
        recommendGoods.setRecoChannel(r1.getString("reco_channel"));
        recommendGoods.setCouponsFlag(r1.getInt("coupons_flag"));
        return recommendGoods;
    };
}
