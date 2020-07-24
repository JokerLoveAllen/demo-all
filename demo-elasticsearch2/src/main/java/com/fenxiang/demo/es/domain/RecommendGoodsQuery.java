package com.fenxiang.demo.es.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * @Description :
 * @Author : lqs
 */
@Data
public class RecommendGoodsQuery{
    private Integer recommendGoodsId;
    private Integer shopId;
    private Integer pageIndex = 1;
    private Integer pageSize = 10;

    private Integer startRow;

    private Integer endRow;

    private Integer status;

    private Integer statuses[];

    private String skuId;

    private String skuName;

    private String keyword;

    private String tags;

    private Long cid1;

    private Long cid2;

    private Long cid3;

    private Integer top;

    private Integer acTop;

    private Integer showInPage;

    private String adTags;

    private String sortDate = "modified";

    private Date now;

    private Date yesterdayNow;

    private boolean notPinGou = false;

    private boolean PinGou = false;

    private String owner;

    private Integer goodType;

    private List<Long> skuIds;

    private String inputName;

    private String sortName;

    private String sort;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date beginTime;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date endTime;

    private Integer isPraise;

    private Integer isHighCommission;

    private String sortField;

    private String source;

    private Long tagsTime;

    private String inputBeginTime;

    private String inputEndTime;

    private Integer isInspection;

    /*验货是否已经生效*/
    private Integer isInspectioneffective;

    /** 预估上线日期号 */
    private String inspectionDateNum;

    /** 验货商品上架开始时间 */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date inspectionStartTime;

    /** 验货商品上架结束时间 */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date inspectionEndTime;

    /** 是否使用缓存 */
    private Boolean useCache = false;

    /** 芬香验货是否置顶 */
    private Integer inspectionTop;

    /** 芬香验货上架时间 */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date shelfTime;

    // 清理过期数据时间点
    private String cleanDate;
}
