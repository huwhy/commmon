package cn.huwhy.wx.sdk.api;

import com.alibaba.fastjson.JSON;

import cn.huwhy.wx.sdk.api.MpOrderApi.MpOrderParam;

public class MpOrderApiTest {

    public static void main(String[] args) {
        MpOrderParam param = new MpOrderParam();
        param.setAppId("wx093a7087c59fbc99");
        param.setMchId("1265636101");
        param.setMchKey("brjh9ztiwlih6zkixtaa7hol377ie5jf");
        param.setBody("测试二维码支付");
        param.setAttach("test");
        param.setOutTradeNo("test12346");
        param.setTotalFee(100);
        param.setSpbillCreateIp("127.0.0.1");
        param.setNotifyUrl("http://dev.ecrm.so/wechat/mp/pay2.client");
        param.setProductId("0001");

        MpOrderApi.MpOrderResult result = MpOrderApi.orderByPc(param);
        System.out.println(JSON.toJSONString(result));
    }

}