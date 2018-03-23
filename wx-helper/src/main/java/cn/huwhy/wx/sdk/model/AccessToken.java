package cn.huwhy.wx.sdk.model;

import com.alibaba.fastjson.annotation.JSONField;

public class AccessToken {

    @JSONField(name = "access_token")
    private String accessToken;
    @JSONField(name = "refresh_token")
    private String refreshToken;
    @JSONField(name = "expires_in")
    private int expiresIn;
    @JSONField(name = "openid")
    private String openId;
    @JSONField(name = "scope")
    private String scope;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    @Override
    public String toString() {
        return "AccessToken{" +
                "accessToken='" + accessToken + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", expiresIn=" + expiresIn +
                ", openId='" + openId + '\'' +
                ", scope='" + scope + '\'' +
                '}';
    }
}
