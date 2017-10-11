package cn.huwhy.wx.sdk.api;

import java.io.IOException;

import com.alibaba.fastjson.JSON;

import cn.huwhy.wx.sdk.model.TemplateMessage;

public class TemplateMessageApi {
    private static String api = "https://api.weixin.qq.com/cgi-bin/message/template/send";

    public static void send(String accessToken, TemplateMessage message) throws IOException {
        HttpClientUtil.post(api, accessToken, JSON.toJSONString(message));
    }

    public static void main(String[] args) throws IOException {
        String accessToken = "5D_hPh3yfmWS9hTzTwa0LXpzU_dMCy2nzdLZnQEhUdanmP-qQxTleJgGgJuTBqR01oxmwCB_SHhaQp430ZXmpJej-nubLjExdxdn6TO9lib-9cHqn3ZK4_vpHlaCBWZgCRFjAAAMBP";
        TemplateMessage message = new TemplateMessage();
        message.setToUser("o4FxKuCStiQwcUXrPSLxLmBtUc3s");
        message.setTemplateId("e3oiRbdIReasB8C_YmPJe_JcogsmFI7RV9cfgCUtYj8");
        message.setUrl("http://angel.huwhy.cn/mp-article/5.html");
        message.addData("name", "hudaojun", "#fff");
        send(accessToken, message);
    }
}
