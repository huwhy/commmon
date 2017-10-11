package cn.huwhy.wx.sdk.model;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

public class GroupMessage {
    @JSONField(name = "touser")
    private List<String> toUser;

    @JSONField(name = "msgtype")
    private String type;

    public GroupMessage(List<String> toUser, String type) {
        this.toUser = toUser;
        this.type = type;
    }

    public List<String> getToUser() {
        return toUser;
    }

    public void setToUser(List<String> toUser) {
        this.toUser = toUser;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
