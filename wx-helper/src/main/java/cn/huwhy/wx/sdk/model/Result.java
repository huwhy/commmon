package cn.huwhy.wx.sdk.model;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;

public class Result {

    private static Map<Integer, String> errorMsg = new HashMap<>();

    private static Integer ZERO = 0;

    static {
        errorMsg.put(ZERO, "success");
        errorMsg.put(-1, "微信服务器繁忙，请稍后再试");
        errorMsg.put(48001, "接口功能未授权，请确认公众号已获得该权限");
    }

    public static Result ok(Object data) {
        Result result = new Result();
        result.setCode(ZERO);
        result.setData(data);
        return result;
    }

    @JSONField(name = "errcode")
    private Integer code = ZERO;
    @JSONField(name = "errmsg")
    private String message;
    private Object data;

    public Result() {
    }

    public Result(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public boolean isOk() {
        return ZERO.equals(code);
    }

    public String toJson() {
        return JSON.toJSONString(this);
    }

    public String getErrMessage() {
        return errorMsg.getOrDefault(code, message);
    }
}