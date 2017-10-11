package cn.huwhy.wx.sdk.event;

import cn.huwhy.wx.sdk.model.Command;

public abstract class Event extends Command {
    private String toUserName;
    private String fromUserName;
    private long createTime;

    public String getToUserName() {
        return toUserName;
    }

    public void setToUserName(String toUserName) {
        this.toUserName = toUserName;
    }

    public String getFromUserName() {
        return fromUserName;
    }

    public void setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

}
