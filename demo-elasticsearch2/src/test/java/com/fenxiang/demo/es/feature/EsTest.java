package com.fenxiang.demo.es.feature;

import com.fenxiang.demo.es.SpringbootElasticTest;
import com.fenxiang.demo.es.domain.RecommendGoods;
import com.fenxiang.demo.es.domain.RecommendGoodsQuery;
import com.fenxiang.demo.es.repoistory.RecommendGoodsEsRepository_2;
import com.fenxiang.demo.es.repoistory.RecommendGoodsEsRepository;
import com.fenxiang.demo.es.service.RecommendGoodsEsService;
import org.apache.commons.lang3.StringEscapeUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.*;

/**
 * @ClassName EsTest
 * @Author zy
 * @Date 2020/5/14 15:24
 */
public class EsTest extends SpringbootElasticTest {

    private @Autowired
    RecommendGoodsEsService recommendGoodsEsService;

    private @Autowired
    RecommendGoodsEsRepository_2 repository;

    private @Autowired
    RecommendGoodsEsRepository  recommendGoodsEsRepository;

    @Test
    public void importAll_Test(){
        recommendGoodsEsService.importAll();
    }

    @Test
    public void find_Test() throws Exception{
        RecommendGoodsQuery recommendGoodsQuery = new RecommendGoodsQuery();
        recommendGoodsQuery.setTags("700");
//        recommendGoodsQuery.setKeyword("果汁");
//        recommendGoodsQuery.setPinGou(true);
        System.out.println(repository.find(recommendGoodsQuery,true));
    }
    @Test
    public void findForSyncByPage_Test() throws Exception{
        RecommendGoodsQuery recommendGoodsQuery = new RecommendGoodsQuery();
        final Calendar instance = Calendar.getInstance();
        instance.set(2019,9,20,9,9,9);
        recommendGoodsQuery.setBeginTime(instance.getTime());
        instance.set(2020,9,20,9,9,9);
        recommendGoodsQuery.setEndTime(instance.getTime());
        recommendGoodsQuery.setPinGou(true);
        System.out.println(recommendGoodsEsRepository.findForSyncByPage(recommendGoodsQuery,false));
    }
    @Test
    public void findCouponExpire_Test() throws Exception{
        RecommendGoodsQuery recommendGoodsQuery = new RecommendGoodsQuery();
        final Calendar instance = Calendar.getInstance();
        instance.set(2019,9,20,9,9,9);
        recommendGoodsQuery.setNow(instance.getTime());
        recommendGoodsQuery.setPinGou(true);
        System.out.println(recommendGoodsEsRepository.findCouponExpire(recommendGoodsQuery));
    }

    @Test
    public void fenxiangInspectionGoodsByPage_Test() throws Exception{
        RecommendGoodsQuery recommendGoodsQuery = new RecommendGoodsQuery();
        recommendGoodsQuery.setSkuIds(new ArrayList<>(Arrays.asList(32789087667L,40565697863L,31935803506L,33148100282L,29849525191L)));
        recommendGoodsQuery.setGoodType(126);
        recommendGoodsQuery.setIsInspectioneffective(1);
        recommendGoodsQuery.setStatus(-2);
        final Calendar instance = Calendar.getInstance();
        instance.set(2019,9,20,9,9,9);
        recommendGoodsQuery.setInspectionStartTime(instance.getTime());
        instance.set(2029,9,20,9,9,9);
        recommendGoodsQuery.setInspectionEndTime(instance.getTime());
        recommendGoodsQuery.setSkuName("皮带");
        System.out.println(recommendGoodsEsRepository.fenxiangInspectionGoodsByPage(recommendGoodsQuery,true));
    }
    @Test
    public void findHotSale_Test() throws Exception{
        RecommendGoodsQuery recommendGoodsQuery = new RecommendGoodsQuery();
        recommendGoodsQuery.setSkuIds(new ArrayList<>(Arrays.asList(32789087667L,40565697863L,31935803506L,33148100282L,29849525191L)));
        recommendGoodsQuery.setShowInPage(1);
        recommendGoodsQuery.setTop(0);
        recommendGoodsQuery.setTags("5");
        recommendGoodsQuery.setAdTags("44");
        recommendGoodsQuery.setNow(new Date());
        final Calendar instance = Calendar.getInstance();
        instance.set(2019,9,20,9,9,9);
        recommendGoodsQuery.setInspectionStartTime(instance.getTime());
        instance.set(2029,9,20,9,9,9);
        recommendGoodsQuery.setInspectionEndTime(instance.getTime());
        recommendGoodsQuery.setSkuName("皮带");
        System.out.println(recommendGoodsEsRepository.findHotSale(recommendGoodsQuery));
    }
    @Test
    public void findPastGoods_Test() throws Exception{
        RecommendGoodsQuery recommendGoodsQuery = new RecommendGoodsQuery();
        final Calendar instance = Calendar.getInstance();
        instance.set(2029,9,20,9,9,9);
        recommendGoodsQuery.setCleanDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(instance.getTime()));
        recommendGoodsQuery.setStatus(1);
        recommendGoodsQuery.setGoodType(126);
        System.out.println(recommendGoodsEsRepository.findPastGoods(recommendGoodsQuery,false));
    }

    @Test
    public void findOwnerGoods_Test() throws Exception{
        RecommendGoodsQuery recommendGoodsQuery = new RecommendGoodsQuery();
        recommendGoodsQuery.setNow(new Date());
        recommendGoodsQuery.setStatus(1);
        recommendGoodsQuery.setGoodType(126);
        recommendGoodsQuery.setSortName("created");
        recommendGoodsQuery.setSort("desc");
        System.out.println(recommendGoodsEsRepository.findOwnerGoods(recommendGoodsQuery));
    }


    // commission 类型 ?? varchar 用int比较
    @Test
    public void board_Test() throws Exception{
        RecommendGoodsQuery recommendGoodsQuery = new RecommendGoodsQuery();
        recommendGoodsQuery.setIsPraise(1);
        recommendGoodsQuery.setIsHighCommission(1);
        recommendGoodsQuery.setStatus(1);
//        recommendGoodsQuery.setGoodType(126);
        recommendGoodsQuery.setSortField("created");
        recommendGoodsQuery.setSort("desc");
        System.out.println(recommendGoodsEsRepository.board(recommendGoodsQuery,true));
    }

    @Test
    public void deletePinGouPrice_Test() throws Exception{
//        RecommendGoodsQuery recommendGoodsQuery = new RecommendGoodsQuery();
//        recommendGoodsQuery.setSkuId("40565697863");
//        System.out.println(recommendGoodsEsRepository.deletePinGouPrice(recommendGoodsQuery));
    }

    @Test
    public void findInspectionGoodsNear24Hour_Test() throws Exception{
        Map<String,Date> queryMap = new HashMap<>();
        queryMap.put("yesterdayDate", new Date(LocalDateTime.of(LocalDate.of(2020,5,15), LocalTime.MIN).toInstant(ZoneOffset.ofHours(8)).toEpochMilli()));
        queryMap.put("nowDate", new Date(LocalDateTime.of(LocalDate.of(2020,5,16), LocalTime.MIN).toInstant(ZoneOffset.ofHours(8)).toEpochMilli()));
        System.out.println(recommendGoodsEsRepository.findInspectionGoodsNear24Hour(queryMap,true));
    }

    @Test
    public void listBySkuIdList_Test() throws Exception{
        RecommendGoodsQuery recommendGoodsQuery = new RecommendGoodsQuery();
        recommendGoodsQuery.setSkuIds(Arrays.asList(32789087667L,40565697863L,31935803506L,33148100282L,29849525191L));
        System.out.println(recommendGoodsEsRepository.listBySkuIdList(recommendGoodsQuery,true));
    }
    @Test
    public void getByPrimaryKey_Test() throws Exception{
        RecommendGoodsQuery recommendGoodsQuery = new RecommendGoodsQuery();
        recommendGoodsQuery.setSkuIds(Arrays.asList(32789087667L,40565697863L,31935803506L,33148100282L,29849525191L));
        recommendGoodsQuery.setRecommendGoodsId(13081);
        System.out.println(recommendGoodsEsRepository.getByPrimaryKey(recommendGoodsQuery,true));
    }

    @Test
    public void getBySkuIdAndGoodType_Test() throws Exception{
        RecommendGoodsQuery recommendGoodsQuery = new RecommendGoodsQuery();
        recommendGoodsQuery.setSkuId("32789087667");
        recommendGoodsQuery.setGoodType(126);
        System.out.println(recommendGoodsEsRepository.getBySkuIdAndGoodType(recommendGoodsQuery,true));
    }


    @Test
    public void getBySkuId_Test() throws Exception{
        RecommendGoodsQuery recommendGoodsQuery = new RecommendGoodsQuery();
        recommendGoodsQuery.setSkuId("40565697863");
        recommendGoodsQuery.setGoodType(126);
        System.out.println(recommendGoodsEsRepository.getBySkuId(recommendGoodsQuery,true));
    }


    @Test
    public void getSqlDataForSkuId_Test(){
        this.recommendGoodsEsService.getSqlDataForSkuId(68686724165L);
    }
}
