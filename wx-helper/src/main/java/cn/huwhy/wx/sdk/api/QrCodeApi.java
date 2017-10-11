package cn.huwhy.wx.sdk.api;

import cn.huwhy.wx.sdk.model.QrCode;

public class QrCodeApi {

    private static String API = "https://api.weixin.qq.com/cgi-bin/qrcode/create";

    public static QrCode createTemp(String accessToken, int sceneId) {
        return null;
    }

    static class QrCodeParam {
        private String action_name;
    }

}
