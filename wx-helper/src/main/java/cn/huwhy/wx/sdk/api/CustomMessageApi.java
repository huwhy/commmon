package cn.huwhy.wx.sdk.api;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import cn.huwhy.wx.sdk.model.CustomMessage;
import cn.huwhy.wx.sdk.model.GroupMessage;
import cn.huwhy.wx.sdk.model.Result;
import cn.huwhy.wx.sdk.model.TextGroupMessage;

/**
 * 微信客服消息接口
 * 只能48小时之内有互动（正面六个动作）的用户才用发送到
 * 1、用户发送信息
 * 2、点击自定义菜单（仅有点击推事件、扫码推事件、扫码推事件且弹出“消息接收中”提示框这3种菜单类型是会触发客服接口的）
 * 3、关注公众号
 * 4、扫描二维码
 * 5、支付成功
 * 6、用户维权
 */
public abstract class CustomMessageApi {

    public static final String API_URI = "https://api.weixin.qq.com/cgi-bin/message/custom/send";

    public static final String API_GROUD_URI = "https://api.weixin.qq.com/cgi-bin/message/mass/send";

    private static Logger logger = LoggerFactory.getLogger(CustomMessageApi.class);

    public static boolean send(String accessToken, CustomMessage message) {
        try {
            Result result = HttpClientUtil.post(API_URI, accessToken, JSON.toJSONString(message));
            return result.isOk();
        } catch (Throwable e) {
            logger.warn("custom message send:", e);
            return false;
        }
    }

    public static boolean send(String accessToken, GroupMessage message) {
        try {
            Result result = HttpClientUtil.post(API_GROUD_URI, accessToken, JSON.toJSONString(message));
            return result.isOk();
        } catch (Throwable e) {
            logger.warn("group message send:", e);
            return false;
        }
    }

    public static void main(String[] args) {
        String accessToken = "5D_hPh3yfmWS9hTzTwa0LXpzU_dMCy2nzdLZnQEhUdanmP-qQxTleJgGgJuTBqR01oxmwCB_SHhaQp430ZXmpJej-nubLjExdxdn6TO9lib-9cHqn3ZK4_vpHlaCBWZgCRFjAAAMBP";
        TextGroupMessage message = new TextGroupMessage(Arrays.asList("o4FxKuCStiQwcUXrPSLxLmBtUc3s", "TextGroupMessage"));
        message.setContent("hello, world!");
        send(accessToken, message);
    }
}
