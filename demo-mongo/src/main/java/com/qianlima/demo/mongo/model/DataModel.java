package com.qianlima.demo.mongo.model;

import lombok.*;
import org.springframework.data.annotation.Id;

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
public class DataModel {
    @Id
    private String id;
    private String name;
    private String pwd;
    private String from;
    private String job;
}
