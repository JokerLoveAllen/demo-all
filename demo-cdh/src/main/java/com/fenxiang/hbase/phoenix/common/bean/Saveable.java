package com.fenxiang.hbase.phoenix.common.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@JsonIgnoreProperties({})
@Setter
@Getter
public abstract class Saveable implements Serializable {

    public static final Integer STATUS_VALID = 1;

    public static final Integer STATUS_INVALID = 0;

    public static final Integer STATUS_DELETE = -1;

    private Integer status;

    private Date created;

    private Date modified;
}