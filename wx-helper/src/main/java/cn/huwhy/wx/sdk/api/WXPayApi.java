package cn.huwhy.wx.sdk.api;

import cn.huwhy.wx.sdk.aes.WxCryptUtil;
import cn.huwhy.wx.sdk.model.WxOrderResult;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class WXPayApi {

    private static Logger logger = LoggerFactory.getLogger(WXPayApi.class);

    private static CloseableHttpClient httpClient;

    public static void setHttpClient(CloseableHttpClient httpClient) {
        WXPayApi.httpClient = httpClient;
    }

    public static WxOrderResult wxPrepay(String appId, String mchId, String partnerKey, String outTradeNo, String body, int totalFee, String ip, String openId, String notifyUrl) {
        final SortedMap<String, String> params = new TreeMap<>();
        params.put("body", body);
        params.put("out_trade_no", outTradeNo);
        params.put("total_fee", Integer.toString(totalFee));
        params.put("spbill_create_ip", ip);
        params.put("openid", openId);
        params.put("notify_url", notifyUrl);
        params.put("trade_type", "JSAPI");
        String nonce_str = System.currentTimeMillis() + "";

        params.put("appid", appId);
        params.put("mch_id", mchId);
        params.put("nonce_str", nonce_str);
        checkParameters(params);
        String sign = WxCryptUtil.createSign(params, partnerKey);
        params.put("sign", sign);
        StringBuilder data = new StringBuilder("<xml>");
        for (Map.Entry<String, String> para : params.entrySet()) {
            data.append(String.format("<%s>%s</%s>", para.getKey(), para.getValue(), para.getKey()));
        }
        data.append("</xml>");
        String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
        HttpPost httpPost = new HttpPost(url);

        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(10000).setConnectTimeout(10000).build();
        httpPost.setConfig(requestConfig);

        StringEntity postEntity = new StringEntity(data.toString(), "UTF-8");
        httpPost.addHeader("Content-Type", "text/xml");
        httpPost.addHeader("User-Agent", "wxpay sdk java v1.0 ");
        httpPost.setEntity(postEntity);
        try {
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            String result = EntityUtils.toString(httpEntity, "UTF-8");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            StringReader sr = new StringReader(result);
            InputSource is = new InputSource(sr);
            Document document = db.parse(is);
            Element root = document.getDocumentElement();
            WxOrderResult orderResult = new WxOrderResult();
            orderResult.setReturn_code(nodeValue("return_code", root));
            orderResult.setReturn_msg(nodeValue("return_msg", root));
            orderResult.setAppid(nodeValue("appid", root));
            orderResult.setMch_id(nodeValue("mch_id", root));
            orderResult.setNonce_str(nodeValue("nonce_str", root));
            orderResult.setSign(nodeValue("sign", root));
            orderResult.setResult_code(nodeValue("result_code", root));
            orderResult.setPrepay_id(nodeValue("prepay_id", root));
            orderResult.setTrade_type(nodeValue("trade_type", root));
            orderResult.setErr_code(nodeValue("err_code", root));
            orderResult.setErr_code_des(nodeValue("err_code_des", root));
            orderResult.setCode_url(nodeValue("code_url", root));
            return orderResult;
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new RuntimeException("Failed to get prepay id due to IO exception.", e);
        }

    }


    final static String[] REQUIRED_ORDER_PARAMETERS = new String[]{"appid", "mch_id", "body", "out_trade_no", "total_fee", "spbill_create_ip", "notify_url",
            "trade_type",};

    private static void checkParameters(Map<String, String> parameters) {
        for (String para : REQUIRED_ORDER_PARAMETERS) {
            if (!parameters.containsKey(para))
                throw new IllegalArgumentException("Reqiured argument '" + para + "' is missing.");
        }
        if ("JSAPI".equals(parameters.get("trade_type")) && !parameters.containsKey("openid"))
            throw new IllegalArgumentException("Reqiured argument 'openid' is missing when trade_type is 'JSAPI'.");
        if ("NATIVE".equals(parameters.get("trade_type")) && !parameters.containsKey("product_id"))
            throw new IllegalArgumentException("Reqiured argument 'product_id' is missing when trade_type is 'NATIVE'.");
    }

    private static String nodeValue(String nodeName, Element root) {
        NodeList node = root.getElementsByTagName(nodeName);
        if (node.getLength() == 1) {
            return node.item(0).getTextContent().trim();
        }
        return null;
    }
}
