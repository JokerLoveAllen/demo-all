package com.qianlima.demo.webflux.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Random;

/**
 * @Description
 * @Author Lun_qs
 * @Date 2019/7/16 10:02
 * @Version 0.0.1
 */
@Setter
@Getter
@Builder
public class MyDomain {
    private Long id;
    private String A,B,C,D;
}
