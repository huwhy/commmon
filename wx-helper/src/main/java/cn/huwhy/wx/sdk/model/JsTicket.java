package cn.huwhy.wx.sdk.model;

import com.alibaba.fastjson.annotation.JSONField;

public class JsTicket {

    private String ticket;
    @JSONField(name = "expires_in")
    private int    expiresIn;

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }
}
