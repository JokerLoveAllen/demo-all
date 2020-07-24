package com.fenxiang.hbase.phoenix.common.bean;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;


/**
 * Service的返回对象
 * Date: 2016-7-26
 */
public class Result {

    /**
     * 失败消息
     */
    private String msg;
    /**
     * 编码
     */
    private int code;
    /**
     * 结果数据
     */
    private Map<String, Object> data = new HashMap<String, Object>();

    /**
     * 默认构造方法
     */
    public Result() {
        this.code = ResultCode.success.code;
        this.msg = ResultCode.getMsg(this.code);
    }

    public Result(int code) {
        this.code = code;
        this.msg = ResultCode.getMsg(this.code);
    }

    public Result(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Result add(String key, Object value) {
        this.data.put(key, value);
        return this;
    }

    public Object get(String key) {
        if (this.data.containsKey(key)) {
            return this.data.get(key);
        }
        return null;
    }

    public String getMsg() {
        return StringUtils.isNotBlank(this.msg) ? this.msg : ResultCode.getMsg(this.code);
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public boolean isSuccess() {
        return ResultCode.success.code == this.code;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
