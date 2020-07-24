package com.qianlima.demo.es.bean.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * @Description
 * @Author Lun_qs
 * @Date 2019/9/17 16:00
 * @Version 0.0.1
 */
@Setter
@Getter
@ToString
public class QueryVo {
    //es
    private int from = 0, size = 10;
    // business logic
    private String  lowerSellTime, higherSellTime;
    //contents
    private String contents;
    //names
    private String names;

    private BigDecimal lowerPrice = BigDecimal.ZERO, higherPrice = BigDecimal.valueOf(Long.MAX_VALUE);

    public int getFrom(){
        return from / size;
    }
}
