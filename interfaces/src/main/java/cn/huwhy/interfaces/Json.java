package cn.huwhy.interfaces;

import java.io.Serializable;

public class Json<T> implements Serializable {

    public static <T> Json<T> SUCCESS() {
        return new Json<>(200L);
    }

    public static <T> Json<T> ERROR() {
        return new Json<>(500L);
    }

    public static <T> Json<T> REDIRECT() {
        return new Json<>(302L);
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
    private T      data;

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

    public T getData() {
        return data;
    }

    public Json setData(T data) {
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
