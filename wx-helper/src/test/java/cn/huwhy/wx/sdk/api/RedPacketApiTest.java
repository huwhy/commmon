package cn.huwhy.wx.sdk.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;

public class RedPacketApiTest {

    public static void main(String[] args) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        File file = new File("/data/wechat/conf/apiclient_cert.p12");
        FileInputStream fileInputStream = new FileInputStream(file);
        char[] password = "1265636101".toCharArray();
        keyStore.load(fileInputStream, password);

        SSLContext content = SSLContexts.custom()
                .loadKeyMaterial(keyStore, password).build();
        HttpClientBuilder builder = HttpClients.custom();
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                content,
                new String[]{"TLSv1"},
                null,
                SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
        builder.setSSLSocketFactory(sslsf);
        CloseableHttpClient httpClient = builder.build();
        RedPacketApi.RedPacketParam param = new RedPacketApi.RedPacketParam();
        param.setMchBillNo("test126");
        param.setPartnerId("1265636101");
        param.setPartnerKey("brjh9ztiwlih6zkixtaa7hol377ie5jf");
        param.setAppId("wx093a7087c59fbc99");
        param.setSendName("拼货宝");
        param.setOpenId("o5bZkvyeCMn9Ofy0ZeBmGoBuhi7U");
        param.setTotalAmount(500);
        param.setTotalNum(1);
        param.setWishing("给你一个红包");
        param.setClientIp("192.168.1.1");
        param.setActName("测试红包");
        param.setRemark("test");
        RedPacketApi.WxRedpackResult redpackResult = RedPacketApi.sendRedPacket(httpClient, param);
        System.out.println(redpackResult);
    }
}