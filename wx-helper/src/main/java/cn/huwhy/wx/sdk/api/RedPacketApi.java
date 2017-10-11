package cn.huwhy.wx.sdk.api;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.base.Strings;

import cn.huwhy.wx.sdk.aes.WxCryptUtil;

public class RedPacketApi {

    private static String SINGLE_URI = "https://api.mch.weixin.qq.com/mmpaymkttransfers/sendredpack";
    private static String GROUP_URI  = "https://api.mch.weixin.qq.com/mmpaymkttransfers/sendgroupredpack";
    private static Logger logger     = LoggerFactory.getLogger(RedPacketApi.class);

    public static WxRedpackResult sendRedPacket(CloseableHttpClient client, RedPacketParam param) {
        return send(client, param, SINGLE_URI);
    }

    public static WxRedpackResult sendGroupRedPacket(CloseableHttpClient client, RedPacketParam param) {
        param.setAmtType("ALL_RAND");
        return send(client, param, GROUP_URI);
    }

    private static WxRedpackResult send(CloseableHttpClient client, RedPacketParam param, String url) {
        Map<String, String> map = new HashMap<>();
        map.put("wxappid", param.getAppId());
        map.put("mch_id", param.getPartnerId());
        String nonce_str = Long.toString(System.currentTimeMillis());
        map.put("nonce_str", nonce_str);
        map.put("mch_billno", param.getMchBillNo());
        map.put("send_name", param.getSendName());
        map.put("re_openid", param.getOpenId());
        map.put("total_amount", Objects.toString(param.getTotalAmount()));
        map.put("total_num", Objects.toString(param.getTotalNum()));
        map.put("wishing", param.getWishing());
        map.put("client_ip", param.getClientIp());
        map.put("act_name", param.getActName());
        if (!Strings.isNullOrEmpty(param.getAmtType())) {
            map.put("amt_type", param.getAmtType());
        }
        if (!Strings.isNullOrEmpty(param.getSceneId())) {
            map.put("scene_id", param.getSceneId());
        }
        if (!Strings.isNullOrEmpty(param.getRiskInfo())) {
            map.put("risk_info", param.getRiskInfo());
        }
        if (!Strings.isNullOrEmpty(param.getConsumeMchId())) {
            map.put("consume_mch_id", param.getConsumeMchId());
        }

        String sign = WxCryptUtil.createSign(map, param.getPartnerKey());
        map.put("sign", sign);
        StringBuilder request = new StringBuilder("<xml>");
        for (Map.Entry<String, String> para : map.entrySet()) {
            request.append(String.format("<%s>%s</%s>", para.getKey(), para.getValue(), para.getKey()));
        }
        request.append("</xml>");
        WxRedpackResult result = new WxRedpackResult();
        try {
            HttpClientUtil.setHttpClient(client);

            String text = HttpClientUtil.postXml(url, request.toString());
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
            String err_code = node.item(0).getTextContent().trim();
            result.setErrCode(err_code);
            node = root.getElementsByTagName("err_code_des");
            String err_code_des = node.item(0).getTextContent().trim();
            result.setErrCodeDes(err_code_des);
            node = root.getElementsByTagName("mch_billno");
            String mch_billno = node.item(0).getTextContent().trim();
            result.setMchBillno(mch_billno);
            node = root.getElementsByTagName("mch_id");
            String mch_id = node.item(0).getTextContent().trim();
            result.setMchId(mch_id);
            node = root.getElementsByTagName("wxappid");
            String wxappid = node.item(0).getTextContent().trim();
            result.setWxappid(wxappid);
            node = root.getElementsByTagName("re_openid");
            String re_openid = node.item(0).getTextContent().trim();
            result.setReOpenid(re_openid);
            node = root.getElementsByTagName("total_amount");
            String total_amount = node.item(0).getTextContent().trim();
            result.setTotalAmount(Integer.valueOf(total_amount));
        } catch (IOException | ParserConfigurationException | SAXException e) {
            logger.error("", e);
            result.setResultCode("FAIL");
            result.setErrCodeDes(e.getMessage());
        }
        return result;
    }

    public static class RedPacketParam {
        /**
         *
         */
        private String  partnerId;
        private String  partnerKey;
        private String  appId;
        /**
         * 商户订单号
         */
        private String  mchBillNo;
        /**
         * 商户名称  send_name
         */
        private String  sendName;
        /**
         * 用户openid
         */
        private String  openId;
        /**
         * 金额 单位分
         */
        private Integer totalAmount;
        /**
         * 红包发放总人数
         */
        private Integer totalNum;
        /**
         * 红包金额设置方式
         */
        private String  amtType;
        /**
         * 红包祝福语
         */
        private String  wishing;
        /**
         * Ip地址
         */
        private String  clientIp;
        /**
         * 活动名称
         */
        private String  actName;
        /**
         * 备注
         */
        private String  remark;
        /**
         * 场景id
         * 发放红包使用场景，红包金额大于200时必传
         * PRODUCT_1:商品促销
         * PRODUCT_2:抽奖
         * PRODUCT_3:虚拟物品兑奖
         * PRODUCT_4:企业内部福利
         * PRODUCT_5:渠道分润
         * PRODUCT_6:保险回馈
         * PRODUCT_7:彩票派奖
         * PRODUCT_8:税务刮奖
         */
        private String  sceneId;
        /**
         * 活动信息
         */
        private String  riskInfo;
        /**
         * 资金授权商户号
         */
        private String  consumeMchId;

        public String getPartnerId() {
            return partnerId;
        }

        public void setPartnerId(String partnerId) {
            this.partnerId = partnerId;
        }

        public String getPartnerKey() {
            return partnerKey;
        }

        public void setPartnerKey(String partnerKey) {
            this.partnerKey = partnerKey;
        }

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getMchBillNo() {
            return mchBillNo;
        }

        public void setMchBillNo(String mchBillNo) {
            this.mchBillNo = mchBillNo;
        }

        public String getSendName() {
            return sendName;
        }

        public void setSendName(String sendName) {
            this.sendName = sendName;
        }

        public String getOpenId() {
            return openId;
        }

        public void setOpenId(String openId) {
            this.openId = openId;
        }

        public Integer getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(Integer totalAmount) {
            this.totalAmount = totalAmount;
        }

        public Integer getTotalNum() {
            return totalNum;
        }

        public void setTotalNum(Integer totalNum) {
            this.totalNum = totalNum;
        }

        public String getAmtType() {
            return amtType;
        }

        public void setAmtType(String amtType) {
            this.amtType = amtType;
        }

        public String getWishing() {
            return wishing;
        }

        public void setWishing(String wishing) {
            this.wishing = wishing;
        }

        public String getClientIp() {
            return clientIp;
        }

        public void setClientIp(String clientIp) {
            this.clientIp = clientIp;
        }

        public String getActName() {
            return actName;
        }

        public void setActName(String actName) {
            this.actName = actName;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public String getSceneId() {
            return sceneId;
        }

        public void setSceneId(String sceneId) {
            this.sceneId = sceneId;
        }

        public String getRiskInfo() {
            return riskInfo;
        }

        public void setRiskInfo(String riskInfo) {
            this.riskInfo = riskInfo;
        }

        public String getConsumeMchId() {
            return consumeMchId;
        }

        public void setConsumeMchId(String consumeMchId) {
            this.consumeMchId = consumeMchId;
        }
    }

    public static class WxRedpackResult implements Serializable {

        /**
         *
         */
        private static final long serialVersionUID = -4837415036337132073L;

        String returnCode;
        String returnMsg;
        String sign;
        String resultCode;

        String errCode;
        String errCodeDes;
        String mchBillno;
        String mchId;
        String wxappid;
        String reOpenid;
        int    totalAmount;
        String sendTime;
        String sendListid;

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

        public String getMchBillno() {
            return mchBillno;
        }

        public void setMchBillno(String mchBillno) {
            this.mchBillno = mchBillno;
        }

        public String getMchId() {
            return mchId;
        }

        public void setMchId(String mchId) {
            this.mchId = mchId;
        }

        public String getWxappid() {
            return wxappid;
        }

        public void setWxappid(String wxappid) {
            this.wxappid = wxappid;
        }

        public String getReOpenid() {
            return reOpenid;
        }

        public void setReOpenid(String reOpenid) {
            this.reOpenid = reOpenid;
        }

        public int getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(int totalAmount) {
            this.totalAmount = totalAmount;
        }

        public String getSendTime() {
            return sendTime;
        }

        public void setSendTime(String sendTime) {
            this.sendTime = sendTime;
        }

        public String getSendListid() {
            return sendListid;
        }

        public void setSendListid(String sendListid) {
            this.sendListid = sendListid;
        }

        @Override
        public String toString() {
            return "WxRedpackResult{" +
                    "returnCode=" + returnCode +
                    ", returnMsg=" + returnMsg +
                    ", sign=" + sign +
                    ", errCode=" + errCode +
                    ", errCodeDes=" + errCodeDes +
                    ", mchBillno=" + mchBillno +
                    ", mchId=" + mchId +
                    ", wxappid=" + wxappid +
                    ", reOpenid=" + reOpenid +
                    ", totalAmount=" + totalAmount +
                    ", sendTime=" + sendTime +
                    ", sendListid=" + sendListid +
                    '}';
        }
    }
}
