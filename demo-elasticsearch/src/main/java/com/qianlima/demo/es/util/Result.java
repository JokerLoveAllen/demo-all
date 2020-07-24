package com.qianlima.demo.es.util;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description
 * @Author Lun_qs
 * @Date 2019/9/17 14:54
 * @Version 0.0.1
 */
@Data
public class Result<T> implements Cloneable, Serializable {
    private int code;
    private String msg;
    private long count;
    private T data;
    private final static Result OK = new Result(){{this.setCode(200);this.setMsg("操作成功");}};

    public static <T> Result<T> ok(){
        return OK;
    }
    public static  Result ok(int code, String msg){
        return new Result(){{this.setCode(code);this.setMsg(msg);}};
    }
    public static <T> Result<T> ok(T data, long count){
        return new Result<T>(){{this.setCode(200);this.setMsg("操作成功");this.setData(data); this.setCount(count);}};
    }
    public static <T> Result<T> error(int code){
        return error(code,"操作失败");
    }
    public static <T> Result<T> error(int code, String msg){
        return new Result<T>(){{this.setMsg(msg);this.setCode(code);}};
    }
}
