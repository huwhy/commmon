package cn.huwhy.wx.sdk.model;

import com.alibaba.fastjson.annotation.JSONField;

public abstract class CustomMessage {

    public static NewsCustomMessage newsMessage(String toUser) {
        return new NewsCustomMessage(toUser);
    }
    public static TextCustomMessage textMessage(String toUser) {
        return new TextCustomMessage(toUser);
    }

    @JSONField(name = "touser")
    private String toUser;

    @JSONField(name = "msgtype")
    private String type;

    public CustomMessage(String toUser, String type) {
        this.toUser = toUser;
        this.type = type;
    }

    public String getToUser() {
        return toUser;
    }

    public void setToUser(String toUser) {
        this.toUser = toUser;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
