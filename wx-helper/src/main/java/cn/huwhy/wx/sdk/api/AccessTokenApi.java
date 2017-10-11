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
        AccessToken token = getAppAccessToken("wx8f7c6b54aac99143", "0aab85a2ae857d52c28141db2d4139b0");
        System.out.println(token);
    }

}
