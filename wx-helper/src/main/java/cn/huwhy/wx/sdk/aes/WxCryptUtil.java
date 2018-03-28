package cn.huwhy.wx.sdk.aes;

import java.io.IOException;
import java.io.StringReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
     * @param data 原始参数
     * @param signKey       加密Key(即 商户Key)
     * @return 签名字符串
     */
    public static String createSign(Map<String, String> data, String signKey) throws Exception {
        Set<String> keySet = data.keySet();
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        Arrays.sort(keyArray);
        StringBuilder sb = new StringBuilder();
        for (String k : keyArray) {
            if (k.equals("sign")) {
                continue;
            }
            String value = data.get(k);
            if (value != null && value.trim().length() > 0) // 参数值为空，则不参与签名
                sb.append(k).append("=").append(value.trim()).append("&");
        }
        sb.append("key=").append(signKey);
        return MD5(sb.toString());
    }

    public static String MD5(String data) throws Exception {
        java.security.MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] array = md.digest(data.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte item : array) {
            sb.append(Integer.toHexString((item & 0xFF) | 0x100).substring(1, 3));
        }
        return sb.toString().toUpperCase();
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
            result.setReturn_code(nodeValue("return_code", root));
            result.setReturn_msg(nodeValue("return_msg", root));
            result.setAppid(nodeValue("appid", root));
            result.setMch_id(nodeValue("mch_id", root));
            result.setDevice_info(nodeValue("device_info", root));
            result.setNonce_str(nodeValue("nonce_str", root));
            result.setSign(nodeValue("sign", root));
            result.setResult_code(nodeValue("result_code", root));
            result.setErr_code(nodeValue("err_code", root));
            result.setErr_code_des(nodeValue("err_code_des", root));
            result.setOpenid(nodeValue("openid", root));
            result.setIs_subscribe(nodeValue("is_subscribe", root));
            result.setTrade_type(nodeValue("trade_type", root));
            result.setBank_type(nodeValue("bank_type", root));
            result.setTotal_fee(nodeValue("total_fee", root));
            result.setFee_type(nodeValue("fee_type", root));
            result.setCash_fee(nodeValue("cash_fee", root));
            result.setCash_fee_type(nodeValue("cash_fee_type", root));
            result.setCoupon_fee(nodeValue("coupon_fee", root));
            result.setCoupon_count(nodeValue("coupon_count", root));
            result.setCoupon_batch_id_$n(nodeValue("coupon_batch_id_$n", root));
            result.setCoupon_id_$n(nodeValue("coupon_id_$n", root));
            result.setCoupon_fee_$n(nodeValue("coupon_fee_$n", root));
            result.setTransaction_id(nodeValue("transaction_id", root));
            result.setOut_trade_no(nodeValue("out_trade_no", root));
            result.setAttach(nodeValue("attach", root));
            result.setTime_end(nodeValue("time_end", root));
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

    public static String nodeValue(String nodeName, Element root) {
        NodeList node = root.getElementsByTagName(nodeName);
        if (node.getLength() == 1) {
            return node.item(0).getTextContent();
        }
        return null;
    }
}
