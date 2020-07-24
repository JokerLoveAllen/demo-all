package com.fenxiang.demo.es.service;

import com.alibaba.fastjson.JSONObject;
import com.fenxiang.demo.es.domain.RecommendGoods;
import com.fenxiang.demo.es.repoistory.RecommendGoodsEsRepository_2;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @ClassName EsService
 * @Author lqs
 * @Date 2020/5/6 16:46
 */
@Slf4j
@Service
public class RecommendGoodsEsService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RecommendGoodsEsRepository_2 recommendGoodsEsRepository;

    public void importAll(){
        Integer total = jdbcTemplate.queryForObject("select count(1) from `recommend_goods`", Integer.class);
        int offset = 0;
        total = total == null ? 0 : total;
        if(total == 0)
            return;
        int cntDown = (total / 1000);
        cntDown = cntDown == 0 ? 1 : cntDown;
        CountDownLatch countDownLatch = new CountDownLatch(cntDown);
        final ExecutorService executorService = Executors.newFixedThreadPool(6);
        while(offset + 1000 < total){
            final int off = offset;
            executorService.execute(()->{
                importBatch(off,1000);
                countDownLatch.countDown();
            });
            offset += 1000;
        }
//        if(offset < total){
        final int off = offset;
        executorService.execute(()->{
            importBatch(off,1000);
            countDownLatch.countDown();
        });
//        }
        try {
            countDownLatch.await();
            executorService.shutdown();
            TimeUnit.SECONDS.sleep(15);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void getSqlDataForSkuId(long skuId){
        final Map<String, Object> map = jdbcTemplate.queryForMap("select * from `recommend_goods` where sku_id = " + skuId);
        Date date =(Date) map.get("modified");
        System.out.println(date.getTime() + ":::::" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
    }

    private void importBatch(int offset, int limit){
        List<Map<String, Object>> datas = jdbcTemplate.queryForList(String.format("select * from `recommend_goods` order by recommend_goods_id limit %d offset %d ", limit, offset));
        if(!CollectionUtils.isEmpty(datas)){
//            datas.stream().map(this::transfer).forEach(e->{
//                try {
//                    recommendGoodsEsRepository.create(e,false);
//                    log.info("({}) import success !",e.getRecommendGoodsId());
//                }catch (Exception exp){
//                    log.error("({}) have exp", e.getRecommendGoodsId(), exp);
//                }
//            });
            try{
                final List<RecommendGoods> goods = datas.stream().map(this::transfer).collect(Collectors.toList());
                recommendGoodsEsRepository.bulkUpsert(goods);
                log.info("offset ({}) finished", offset);
            }catch (Exception e){
                log.error("have error:" ,e);
            }
        }
    }

    private RecommendGoods transfer( Map<String,Object> data){
        JSONObject r1 = new JSONObject(data);
        RecommendGoods recommendGoods = new RecommendGoods();
        recommendGoods.setRecommendGoodsId(r1.getInteger("recommend_goods_id"));
        recommendGoods.setSkuId(r1.getLong("sku_id"));
        recommendGoods.setSkuName(r1.getString("sku_name"));
        recommendGoods.setMaterialUrl(r1.getString("material_url"));
        recommendGoods.setJdPrice(r1.getString("jd_price"));
        recommendGoods.setImageUrl(r1.getString("image_url"));
        recommendGoods.setImageUrlList(StringUtils.isBlank(r1.getString("img_url_list")) ? null :  Arrays.asList(r1.getString("img_url_list").split(",")));
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
        recommendGoods.setTop(r1.getInteger("top"));
        recommendGoods.setAcTop(r1.getInteger("ac_top"));
        recommendGoods.setComments(r1.getLong("comments"));
        recommendGoods.setGoodCommentsShare(r1.getString("good_comments_share") == null ? null : Double.parseDouble(r1.getString("good_comments_share")));
        recommendGoods.setInOrderCount30Days(r1.getLong("in_order_count_30days"));
        recommendGoods.setTags(r1.getString("tags"));
        recommendGoods.setTagsTime(r1.getLong("tags_time"));
        recommendGoods.setOwner(r1.getString("owner"));
        recommendGoods.setBrandCode(r1.getString("brand_code"));
        recommendGoods.setBrandName(r1.getString("brand_name"));
        recommendGoods.setSpuid(r1.getLong("spuid"));
        recommendGoods.setShopName(r1.getString("shop_name"));
        recommendGoods.setShopId(r1.getInteger("shop_id"));
        recommendGoods.setAdTags(r1.getString("ad_tags"));
        recommendGoods.setSource(r1.getString("source"));
        recommendGoods.setGoodType(r1.getInteger("good_type"));
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
        recommendGoods.setIsInspection(r1.getInteger("is_inspection"));
        recommendGoods.setShelfTime(r1.getDate("shelf_time"));
        recommendGoods.setInspectionTop(r1.getInteger("inspection_top"));
        recommendGoods.setBelowReason(r1.getString("below_reason"));
        recommendGoods.setBelowTime(r1.getDate("below_time"));
        recommendGoods.setStatus(r1.getInteger("status"));
        recommendGoods.setCreated(r1.getDate("created"));
        recommendGoods.setModified(r1.getDate("modified"));
        recommendGoods.setRecoChannel(r1.getString("reco_channel"));
        recommendGoods.setCouponsFlag(r1.getInteger("coupons_flag"));
        return recommendGoods;
    };

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
        recommendGoods.setImageUrlList(StringUtils.isBlank(r1.getString("img_url_list")) ? null : Arrays.asList(r1.getString("img_url_list").split(",")));
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
        recommendGoods.setShopName(r1.getString("shop_name"));
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
