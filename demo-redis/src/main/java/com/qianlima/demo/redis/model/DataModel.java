package com.qianlima.demo.redis.model;

import lombok.*;
import org.springframework.data.annotation.Id;

import java.io.Serializable;

/**
 * @Description
 * @Author Lun_qs
 * @Date 2019/7/11 11:44
 * @Version 0.0.1
 */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DataModel implements Serializable {
    @Id
    private String id;
    private String name;
    private String pwd;
    private String from;
    private String job;
}
