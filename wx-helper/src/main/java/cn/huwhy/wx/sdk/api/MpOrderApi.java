package cn.huwhy.wx.sdk.api;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.base.Strings;

import cn.huwhy.wx.sdk.aes.WxCryptUtil;

/**
 * 微信统一下单
 */
public class MpOrderApi {
    private static String SUCCESS = "SUCCESS";
    private static Logger logger  = LoggerFactory.getLogger(MpOrderApi.class);
    private static String API_URL = "https://api.mch.weixin.qq.com/pay/unifiedorder";

    public static MpOrderResult orderByPc(MpOrderParam param) {
        param.setTradeType("NATIVE");
        return order(param);
    }

    public static MpOrderResult orderByMp(MpOrderParam param) {
        param.setTradeType("JSAPI");
        return order(param);
    }

    private static MpOrderResult order(MpOrderParam param) {
        Map<String, String> map = new TreeMap<>();
        map.put("appid", param.getAppId());
        map.put("mch_id", param.getMchId());
        if (!Strings.isNullOrEmpty(param.getDeviceInfo())) {
            map.put("device_info", param.getDeviceInfo());
        }
        map.put("nonce_str", Long.toString(System.currentTimeMillis()));
        map.put("body", param.getBody());
        if (!Strings.isNullOrEmpty(param.getAttach())) {
            map.put("attach", param.getAttach());
        }
        map.put("out_trade_no", param.getOutTradeNo());
        map.put("total_fee", param.getTotalFee().toString());
        map.put("spbill_create_ip", param.getSpbillCreateIp());
        map.put("notify_url", param.getNotifyUrl());
        map.put("trade_type", param.getTradeType());
        if (!Strings.isNullOrEmpty(param.getProductId())) {
            map.put("product_id", param.getProductId());
        }
        if (!Strings.isNullOrEmpty(param.getOpenId())) {
            map.put("openid", param.getOpenId());
        }
        checkParameters(map);
        String sign = WxCryptUtil.createSign(map, param.getMchKey());
        map.put("sign", sign);
        StringBuilder request = new StringBuilder("<xml>");
        for (Map.Entry<String, String> para : map.entrySet()) {
            request.append(String.format("<%s>%s</%s>", para.getKey(), para.getValue(), para.getKey()));
        }
        request.append("</xml>");
        MpOrderResult result = new MpOrderResult();
        try {
            String text = HttpClientUtil.postXml(API_URL, request.toString());
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            StringReader sr = new StringReader(text);
            InputSource is = new InputSource(sr);
            Document document = db.parse(is);
            Element root = document.getDocumentElement();
            NodeList msgTypeNode = root.getElementsByTagName("return_code");
            String returnCode = msgTypeNode.item(0).getTextContent().trim();
            result.setReturnCode(returnCode);
            NodeList returnMsgTypeNode = root.getElementsByTagName("return_msg");
            String returnMsg = returnMsgTypeNode.item(0).getTextContent().trim();
            result.setReturnMsg(returnMsg);
            NodeList resultCodeNode = root.getElementsByTagName("result_code");
            String result_code = resultCodeNode.item(0).getTextContent().trim();
            result.setResultCode(result_code);
            NodeList node = root.getElementsByTagName("err_code");
            if (node != null && node.getLength() == 1) {
                String err_code = node.item(0).getTextContent().trim();
                result.setErrCode(err_code);
                node = root.getElementsByTagName("err_code_des");
                String err_code_des = node.item(0).getTextContent().trim();
                result.setErrCodeDes(err_code_des);
            }
            node = root.getElementsByTagName("appid");
            result.setAppId(node.item(0).getTextContent().trim());
            node = root.getElementsByTagName("mch_id");
            result.setMchId(node.item(0).getTextContent().trim());
            node = root.getElementsByTagName("device_info");
            if (node != null && node.getLength() == 1) {
                result.setDeviceInfo(node.item(0).getTextContent().trim());
            }
            node = root.getElementsByTagName("nonce_str");
            result.setNonceStr(node.item(0).getTextContent().trim());
            node = root.getElementsByTagName("sign");
            result.setSign(node.item(0).getTextContent().trim());
            node = root.getElementsByTagName("trade_type");
            result.setTradeType(node.item(0).getTextContent().trim());
            node = root.getElementsByTagName("prepay_id");
            result.setPrepayId(node.item(0).getTextContent().trim());
            node = root.getElementsByTagName("code_url");
            if (node != null && node.getLength() == 1) {
                result.setCodeUrl(node.item(0).getTextContent().trim());
            }
        } catch (IOException | ParserConfigurationException | SAXException e) {
            logger.error("", e);
            result.setResultCode("FAIL");
            result.setErrCodeDes(e.getMessage());
        }
        return result;
    }

    static final String[] REQUIRED_ORDER_PARAMETERS = new String[]{"appid", "mch_id", "body", "out_trade_no", "total_fee", "spbill_create_ip", "notify_url",
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

    public static class MpOrderParam {
        /**
         * 公众帐号ID(appid)
         */
        private String  appId;
        /**
         * 商户号(mch_id)
         */
        private String  mchId;
        /**
         * 商户号 支付key
         */
        private String  mchKey;
        /**
         * 设备号(device_info):  可不传
         * 自定义参数，可以为终端设备号(门店号或收银设备ID)，PC网页或公众号内支付可以传"WEB"
         */
        private String  deviceInfo;
        /**
         * 随机字符串（nonce_str）
         */
        private String  nonceStr;
        /**
         * 签名
         */
        private String  sign;
        /**
         * 签名类型(sign_type) 默认MD5 可不传
         */
        private String  signType;
        /**
         * 商品描述
         */
        private String  body;
        /**
         * 商品详情(未上线) 可不传
         */
        private String  detail;
        /**
         * 附加数据
         * 附加数据，在查询API和支付通知中原样返回，可作为自定义参数使用。
         */
        private String  attach;
        /**
         * 商户订单号(out_trade_no)
         * 商户系统内部订单号，要求32个字符内，只能是数字、大小写字母_-|*@ ，且在同一个商户号下唯一
         */
        private String  outTradeNo;
        /**
         * 标价币种(fee_type) 可不传    默认 CNY
         */
        private String  feeType;
        /**
         * 标价金额(total_fee) 单位分
         */
        private Integer totalFee;
        /**
         * 终端IP （spbill_create_ip） APP和网页支付提交用户端ip，Native支付填调用微信支付API的机器IP。
         */
        private String  spbillCreateIp;
        /**
         * 交易起始时间 time_start   可不传
         * 订单生成时间，格式为yyyyMMddHHmmss
         */
        private String  timeStart;
        /**
         * 交易结束时间  time_expire  可不传
         * 订单失效时间，格式为yyyyMMddHHmmss
         */
        private String  timeExpire;
        /**
         * 订单优惠标记 goods_tag 可不传
         */
        private String  goodsTag;
        /**
         * 通知地址 notify_url
         * 异步接收微信支付结果通知的回调地址，通知url必须为外网可访问的url，不能携带参数。
         */
        private String  notifyUrl;
        /**
         * 交易类型 trade_type
         * JSAPI--公众号支付、
         * NATIVE--原生扫码支付、
         * APP--app支付，统一下单接口trade_type的传参可参考这里
         * MICROPAY--刷卡支付，刷卡支付有单独的支付接口，不调用统一下单接口
         */
        private String  tradeType;
        /**
         * 商品ID product_id   可不传
         * trade_type=NATIVE时（即扫码支付），此参数必传。此参数为二维码中包含的商品ID，商户自行定义。
         */
        private String  productId;
        /**
         * 指定支付方式  limit_pay  可不传
         * 上传此参数no_credit--可限制用户不能使用信用卡支付
         */
        private String  limitPay;
        /**
         * 用户标识  可不传
         * trade_type=JSAPI时（即公众号支付），此参数必传，此参数为微信用户在商户对应appid下的唯一标识
         */
        private String  openId;

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getMchId() {
            return mchId;
        }

        public void setMchId(String mchId) {
            this.mchId = mchId;
        }

        public String getMchKey() {
            return mchKey;
        }

        public void setMchKey(String mchKey) {
            this.mchKey = mchKey;
        }

        public String getDeviceInfo() {
            return deviceInfo;
        }

        public void setDeviceInfo(String deviceInfo) {
            this.deviceInfo = deviceInfo;
        }

        public String getNonceStr() {
            return nonceStr;
        }

        public void setNonceStr(String nonceStr) {
            this.nonceStr = nonceStr;
        }

        public String getSign() {
            return sign;
        }

        public void setSign(String sign) {
            this.sign = sign;
        }

        public String getSignType() {
            return signType;
        }

        public void setSignType(String signType) {
            this.signType = signType;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public String getDetail() {
            return detail;
        }

        public void setDetail(String detail) {
            this.detail = detail;
        }

        public String getAttach() {
            return attach;
        }

        public void setAttach(String attach) {
            this.attach = attach;
        }

        public String getOutTradeNo() {
            return outTradeNo;
        }

        public void setOutTradeNo(String outTradeNo) {
            this.outTradeNo = outTradeNo;
        }

        public String getFeeType() {
            return feeType;
        }

        public void setFeeType(String feeType) {
            this.feeType = feeType;
        }

        public Integer getTotalFee() {
            return totalFee;
        }

        public void setTotalFee(Integer totalFee) {
            this.totalFee = totalFee;
        }

        public String getSpbillCreateIp() {
            return spbillCreateIp;
        }

        public void setSpbillCreateIp(String spbillCreateIp) {
            this.spbillCreateIp = spbillCreateIp;
        }

        public String getTimeStart() {
            return timeStart;
        }

        public void setTimeStart(String timeStart) {
            this.timeStart = timeStart;
        }

        public String getTimeExpire() {
            return timeExpire;
        }

        public void setTimeExpire(String timeExpire) {
            this.timeExpire = timeExpire;
        }

        public String getGoodsTag() {
            return goodsTag;
        }

        public void setGoodsTag(String goodsTag) {
            this.goodsTag = goodsTag;
        }

        public String getNotifyUrl() {
            return notifyUrl;
        }

        public void setNotifyUrl(String notifyUrl) {
            this.notifyUrl = notifyUrl;
        }

        public String getTradeType() {
            return tradeType;
        }

        public void setTradeType(String tradeType) {
            this.tradeType = tradeType;
        }

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public String getLimitPay() {
            return limitPay;
        }

        public void setLimitPay(String limitPay) {
            this.limitPay = limitPay;
        }

        public String getOpenId() {
            return openId;
        }

        public void setOpenId(String openId) {
            this.openId = openId;
        }
    }

    public static class MpOrderResult {
        /**
         * 返回状态码
         */
        private String returnCode;
        /**
         * 返回信息
         */
        private String returnMsg;
        /**
         * 公众号ID
         */
        private String appId;
        /**
         * 商户号ID
         */
        private String mchId;
        /**
         * 设备号
         * 下单时参数 原样返回
         */
        private String deviceInfo;
        /**
         * 随机字符串
         */
        private String nonceStr;
        /**
         *
         */
        private String openId;
        /**
         * 签名
         */
        private String sign;
        /**
         * 业务结果
         */
        private String resultCode;
        /**
         * 错误代码
         */
        private String errCode;
        /**
         * 错误代码描述
         */
        private String errCodeDes;
        /**
         * 交易类型
         */
        private String tradeType;
        /**
         * 预支付交易会话标识
         * 微信生成的预支付会话标识，用于后续接口调用中使用，该值有效期为2小时
         */
        private String prepayId;
        /**
         * 二维码链接
         * trade_type为NATIVE时返回  用于生成二维码
         * 展示给用户进行扫码支付
         */
        private String codeUrl;

        public String getReturnCode() {
            return returnCode;
        }

        public void setReturnCode(String returnCode) {
            this.returnCode = returnCode;
        }

        public String getReturnMsg() {
            return returnMsg;
        }

        public void setReturnMsg(String returnMsg) {
            this.returnMsg = returnMsg;
        }

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getMchId() {
            return mchId;
        }

        public void setMchId(String mchId) {
            this.mchId = mchId;
        }

        public String getDeviceInfo() {
            return deviceInfo;
        }

        public void setDeviceInfo(String deviceInfo) {
            this.deviceInfo = deviceInfo;
        }

        public String getNonceStr() {
            return nonceStr;
        }

        public void setNonceStr(String nonceStr) {
            this.nonceStr = nonceStr;
        }

        public String getOpenId() {
            return openId;
        }

        public void setOpenId(String openId) {
            this.openId = openId;
        }

        public String getSign() {
            return sign;
        }

        public void setSign(String sign) {
            this.sign = sign;
        }

        public String getResultCode() {
            return resultCode;
        }

        public void setResultCode(String resultCode) {
            this.resultCode = resultCode;
        }

        public String getErrCode() {
            return errCode;
        }

        public void setErrCode(String errCode) {
            this.errCode = errCode;
        }

        public String getErrCodeDes() {
            return errCodeDes;
        }

        public void setErrCodeDes(String errCodeDes) {
            this.errCodeDes = errCodeDes;
        }

        public String getTradeType() {
            return tradeType;
        }

        public void setTradeType(String tradeType) {
            this.tradeType = tradeType;
        }

        public String getPrepayId() {
            return prepayId;
        }

        public void setPrepayId(String prepayId) {
            this.prepayId = prepayId;
        }

        public String getCodeUrl() {
            return codeUrl;
        }

        public void setCodeUrl(String codeUrl) {
            this.codeUrl = codeUrl;
        }

        public boolean isOk() {
            return SUCCESS.equals(returnCode) && SUCCESS.equals(resultCode);
        }
    }

}
