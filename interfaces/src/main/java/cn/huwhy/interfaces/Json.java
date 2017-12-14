package cn.huwhy.interfaces;

import java.io.Serializable;

public class Json implements Serializable {

    public static Json SUCCESS() {
        return new Json(200L);
    }

    public static Json ERROR() {
        return new Json(500L);
    }

    public static <T> Json REDIRECT() {
        return new Json(302L);
    }

    /**
     * 编码
     */
    private long code;
    /**
     * 消息
     */
    private String message;
    /**
     * 重定向URL
     */
    private String url;
    /**
     * 数据
     */
    private Object data;

    public Json() {
    }

    public Json(long code) {
        this.code = code;
    }

    public Json(long code, String message) {
        this.code = code;
        this.message = message;
    }

    public long getCode() {
        return code;
    }

    public Json setCode(long code) {
        this.code = code;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public Json setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public Json setUrl(String url) {
        this.url = url;
        return this;
    }

    public <T> T getData() {
        return (T) data;
    }

    public Json setData(Object data) {
        this.data = data;
        return this;
    }

    public boolean isOk() {
        return 200L == this.code;
    }

    public boolean isRedirect() {
        return 302L == this.code;
    }
}
