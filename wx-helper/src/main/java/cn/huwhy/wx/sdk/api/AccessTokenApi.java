package cn.huwhy.wx.sdk.api;

import java.util.HashMap;
import java.util.Map;

import cn.huwhy.wx.sdk.model.AccessToken;
import cn.huwhy.wx.sdk.model.Result;

public abstract class AccessTokenApi {

    private static final String APP_TAKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";

    public static AccessToken getAppAccessToken(String appId, String appSecret){
        Map<String, String> params = new HashMap<>();
        params.put("appid", appId);
        params.put("secret", appSecret);
        params.put("grant_type", "client_credential");
        Result result = HttpClientUtil.get(APP_TAKEN_URL, params, AccessToken.class);
        return result.isOk() ? (AccessToken) result.getData() : null;
    }

    public static void main(String[] args) {
        AccessToken token = getAppAccessToken("wxb4be7fb7ac42baf3", "7a2e23370e79aa7dd3ce15d5d448c12a");
        System.out.println(token);
    }

}
