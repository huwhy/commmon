package cn.huwhy.wx.sdk.api;

import java.util.HashMap;
import java.util.Map;

import cn.huwhy.wx.sdk.model.JsTicket;
import cn.huwhy.wx.sdk.model.Result;

public class JsApiTicketApi {

    private static final String API = "https://api.weixin.qq.com/cgi-bin/ticket/getticket";

    public static JsTicket getTicked(String accessToken) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", accessToken);
        params.put("type", "jsapi");
        Result result = HttpClientUtil.get(API, params, JsTicket.class);
        return result.isOk() ? (JsTicket) result.getData() : null;
    }
}
