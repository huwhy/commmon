package cn.huwhy.wx.sdk.model;

import com.alibaba.fastjson.annotation.JSONField;

public class UserInfo {
    private int    subscribe;
    @JSONField(name = "openid")
    private String openId;
    @JSONField(name = "nickname")
    private String nick;
    private int    sex;
    @JSONField(name = "language")
    private String lang;
    private String city;
    private String province;
    private String country;
    @JSONField(name = "headimgurl")
    private String headImgUrl;
    @JSONField(name = "subscribe_time")
    private long   subscribeTime;
    @JSONField(name = "unionid")
    private String unionId;
    private String remark;
    @JSONField(name = "groupid")
    private int    groupid;

    public int getSubscribe() {
        return subscribe;
    }

    public void setSubscribe(int subscribe) {
        this.subscribe = subscribe;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getHeadImgUrl() {
        return headImgUrl;
    }

    public void setHeadImgUrl(String headImgUrl) {
        this.headImgUrl = headImgUrl;
    }

    public long getSubscribeTime() {
        return subscribeTime;
    }

    public void setSubscribeTime(long subscribeTime) {
        this.subscribeTime = subscribeTime;
    }

    public String getUnionId() {
        return unionId;
    }

    public void setUnionId(String unionId) {
        this.unionId = unionId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getGroupid() {
        return groupid;
    }

    public void setGroupid(int groupid) {
        this.groupid = groupid;
    }
}
