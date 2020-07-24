package com.fenxiang.hbase.phoenix.common.bean;

/**
 * 服务端的返回码。 <br/>
 * 0 正常<br/>
 * 业务异常返回码：1xx
 * rpc调用异常返回码：2xx
 */
public enum ResultCode {

    success(0, "成功"),
    parameter_error(1, "参数异常"),
    exception(-1, "系统内部异常，请稍后重试"),
    no_session(-2, "登录失效，请重新登陆"),
    no_permission(-3,"无权限进行此操作"),
    user_not_exits(2,"用户不存在"),
    group_too_many(3, "当前最多只能创建10个群"),
    translate_url_fail(101, "转链失败"),
    sms_error(200,"短信获取太频繁了");

    /**
     * 返回码。
     * 0为正常
     */
    public int code;

    /**
     * 附加信息
     */
    public String msg;

    ResultCode(int code) {
        this.code = code;
    }

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static String getMsg(int code) {
        switch (code) {
            case 0:
                return "成功";
            case -1:
                return exception.msg;
            default:
                return "";
        }
    }

    @Override
    public String toString() {
        return "ResultCode [code=" + code + ", msg=" + msg + "]";
    }

}
