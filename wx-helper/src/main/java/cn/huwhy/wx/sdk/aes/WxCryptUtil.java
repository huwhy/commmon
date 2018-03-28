package cn.huwhy.wx.sdk.aes;

import java.io.IOException;
import java.io.StringReader;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import cn.huwhy.wx.sdk.model.WxPayResult;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import cn.huwhy.wx.sdk.model.Command;

public class WxCryptUtil {

    private static Logger logger = LoggerFactory.getLogger(WxCryptUtil.class);

    /**
     * 微信公众号支付签名算法(详见:http://pay.weixin.qq.com/wiki/doc/api/index.php?chapter=4_3)
     *
     * @param packageParams 原始参数
     * @param signKey       加密Key(即 商户Key)
     * @return 签名字符串
     */
    public static String createSign(Map<String, String> packageParams, String signKey) {
        SortedMap<String, String> sortedMap = new TreeMap<String, String>();
        sortedMap.putAll(packageParams);

        List<String> keys = new ArrayList<String>(sortedMap.keySet());
        Collections.sort(keys);

        StringBuilder toSign = new StringBuilder();
        for (String key : keys) {
            String value = sortedMap.get(key);
            if (null != value && !"".equals(value) && !"sign".equals(key)
                    && !"key".equals(key)) {
                toSign.append(key).append("=").append(value).append("&");
            }
        }
        toSign.append("key=").append(signKey);
        return DigestUtils.md5Hex(toSign.toString())
                .toUpperCase();
    }

    public static WxPayResult transform(String xml) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            StringReader sr = new StringReader(xml);
            InputSource is = new InputSource(sr);
            Document document = db.parse(is);
            Element root = document.getDocumentElement();
            WxPayResult result = new WxPayResult();
            NodeList returnCodeNode = root.getElementsByTagName("return_code");
            result.setReturn_code(returnCodeNode.item(0).getTextContent().trim());
            NodeList returnMsgNode = root.getElementsByTagName("return_msg");
            result.setReturn_msg(returnMsgNode.item(0).getTextContent().trim());
            NodeList appIdNode = root.getElementsByTagName("appid");
            result.setAppid(appIdNode.item(0).getTextContent().trim());
            NodeList mchId = root.getElementsByTagName("mch_id");
            result.setMch_id(mchId.item(0).getTextContent().trim());
            NodeList deviceInfo = root.getElementsByTagName("device_info");
            result.setDevice_info(deviceInfo.item(0).getTextContent().trim());
            NodeList nonceStr = root.getElementsByTagName("nonce_str");
            result.setNonce_str(nonceStr.item(0).getTextContent().trim());
            NodeList sign = root.getElementsByTagName("sign");
            result.setSign(sign.item(0).getTextContent().trim());
            NodeList resultCode = root.getElementsByTagName("result_code");
            result.setResult_code(resultCode.item(0).getTextContent().trim());
            NodeList errCode = root.getElementsByTagName("err_code");
            result.setErr_code(errCode.item(0).getTextContent().trim());
            NodeList err_code_des = root.getElementsByTagName("err_code_des");
            result.setErr_code_des(err_code_des.item(0).getTextContent().trim());
            NodeList openId = root.getElementsByTagName("openid");
            result.setOpenid(openId.item(0).getTextContent().trim());
            NodeList isSubscribe = root.getElementsByTagName("is_subscribe");
            result.setIs_subscribe(isSubscribe.item(0).getTextContent().trim());
            NodeList tradeType = root.getElementsByTagName("trade_type");
            result.setTrade_type(tradeType.item(0).getTextContent().trim());
            NodeList bankType = root.getElementsByTagName("bank_type");
            result.setBank_type(bankType.item(0).getTextContent().trim());
            NodeList totalFee = root.getElementsByTagName("total_fee");
            result.setTotal_fee(totalFee.item(0).getTextContent().trim());
            NodeList feeType = root.getElementsByTagName("fee_type");
            result.setFee_type(feeType.item(0).getTextContent().trim());
            NodeList cashFee = root.getElementsByTagName("cash_fee");
            result.setCash_fee(cashFee.item(0).getTextContent().trim());
            NodeList cashFeeType = root.getElementsByTagName("cash_fee_type");
            result.setCash_fee_type(cashFeeType.item(0).getTextContent().trim());
            NodeList couponFee = root.getElementsByTagName("coupon_fee");
            result.setCoupon_fee(couponFee.item(0).getTextContent().trim());
            NodeList couponCount = root.getElementsByTagName("coupon_count");
            result.setCoupon_count(couponCount.item(0).getTextContent().trim());
            NodeList couponBatchId_$n = root.getElementsByTagName("coupon_batch_id_$n");
            result.setCoupon_batch_id_$n(couponBatchId_$n.item(0).getTextContent().trim());
            NodeList couponId_$n = root.getElementsByTagName("coupon_id_$n");
            result.setCoupon_id_$n(couponId_$n.item(0).getTextContent().trim());
            NodeList couponFee_$n = root.getElementsByTagName("coupon_fee_$n");
            result.setCoupon_fee_$n(couponFee_$n.item(0).getTextContent().trim());
            NodeList transactionId = root.getElementsByTagName("transaction_id");
            result.setTransaction_id(transactionId.item(0).getTextContent().trim());
            NodeList outTradeNo = root.getElementsByTagName("out_trade_no");
            result.setOut_trade_no(outTradeNo.item(0).getTextContent().trim());
            NodeList attach = root.getElementsByTagName("attach");
            result.setAttach(attach.item(0).getTextContent().trim());
            NodeList timeEnd = root.getElementsByTagName("time_end");
            result.setTime_end(timeEnd.item(0).getTextContent().trim());
            return result;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            logger.error("", e);
            throw new RuntimeException(e);
        }
    }

    public static Command transform(MpConfig config, String signature, String timestamp, String nonce, String postXML) {
        try {
            String ss = postXML;
            if (config.getSecure()) {
                ss = WXBizMsgCrypt.decryptMsg(config, signature, timestamp, nonce, postXML);
                logger.info("decrypt msg : " + ss);
            }
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            StringReader sr = new StringReader(ss);
            InputSource is = new InputSource(sr);
            Document document = db.parse(is);
            Element root = document.getDocumentElement();

            Command command = new Command();
            NodeList msgTypeNode = root.getElementsByTagName("MsgType");
            String msgType = msgTypeNode.item(0).getTextContent().trim();
            command.setMsgType(msgType);

            NodeList toNode = root.getElementsByTagName("ToUserName");
            String toUser = toNode.item(0).getTextContent();
            command.setToUserName(toUser);

            NodeList fromNode = root.getElementsByTagName("FromUserName");
            String fromUser = fromNode.item(0).getTextContent();
            command.setFromUserName(fromUser);

            NodeList createTimeNode = root.getElementsByTagName("CreateTime");
            long createTime = Long.parseLong(createTimeNode.item(0).getTextContent());
            command.setCreateTime(createTime);

            NodeList eventNode = root.getElementsByTagName("Event");
            if (eventNode.getLength() == 1) {
                command.setEvent(eventNode.item(0).getTextContent());
            }

            NodeList ticketNode = root.getElementsByTagName("Ticket");
            if (ticketNode.getLength() == 1) {
                command.setTicket(ticketNode.item(0).getTextContent());
            }

            NodeList ekNode = root.getElementsByTagName("EventKey");
            if (ekNode.getLength() == 1) {
                command.setEventKey(ekNode.item(0).getTextContent());
            }

            NodeList latitudeNode = root.getElementsByTagName("Latitude");
            if (latitudeNode.getLength() == 1) {
                command.setLatitude(latitudeNode.item(0).getTextContent());
            }

            NodeList longitudeNode = root.getElementsByTagName("Longitude");
            if (longitudeNode.getLength() == 1) {
                command.setLongitude(longitudeNode.item(0).getTextContent());
            }

            NodeList precisionNode = root.getElementsByTagName("Precision");
            if (precisionNode.getLength() == 1) {
                command.setPrecision(precisionNode.item(0).getTextContent());
            }

            NodeList msgIdNode = root.getElementsByTagName("MsgId");
            if (msgIdNode.getLength() == 1) {
                command.setMsgId(msgIdNode.item(0).getTextContent());
            }

            NodeList contentNode = root.getElementsByTagName("Content");
            if (contentNode.getLength() == 1) {
                command.setContent(contentNode.item(0).getTextContent());
            }

            NodeList picUrlNode = root.getElementsByTagName("PicUrl");
            if (picUrlNode.getLength() == 1) {
                command.setPicUrl(picUrlNode.item(0).getTextContent());
            }

            NodeList mediaIdNode = root.getElementsByTagName("MediaId");
            if (mediaIdNode.getLength() == 1) {
                command.setMediaId(mediaIdNode.item(0).getTextContent());
            }

            NodeList formatNode = root.getElementsByTagName("Format");
            if (formatNode.getLength() == 1) {
                command.setFormat(formatNode.item(0).getTextContent());
            }

            command.setThumbMediaid(nodeValue("ThumbMediaId", root));
            command.setLocationX(nodeValue("Location_X", root));
            command.setLocationY(nodeValue("Location_Y", root));
            command.setScale(nodeValue("Scale", root));
            command.setLabel(nodeValue("Label", root));
            command.setTitle(nodeValue("Title", root));
            command.setDescription(nodeValue("Description", root));
            command.setUrl(nodeValue("Url", root));
            command.setRecognition(nodeValue("Recognition", root));
            command.setStatus(nodeValue("Status", root));
            return command;

        } catch (IOException | ParserConfigurationException | AesException | SAXException | NoSuchAlgorithmException e) {
            logger.error("", e);
            throw new RuntimeException(e);
        }
    }

    private static String nodeValue(String nodeName, Element root) {
        NodeList node = root.getElementsByTagName(nodeName);
        if (node.getLength() == 1) {
            return node.item(0).getTextContent();
        }
        return null;
    }
}
