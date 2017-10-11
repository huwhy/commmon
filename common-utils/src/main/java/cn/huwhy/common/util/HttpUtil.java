package cn.huwhy.common.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);
    private static final String DEFAULT_CHARSET = "UTF-8";

    private static final int DEFAULT_SOCKET_TIMEOUT  = 30000;
    private static final int DEFAULT_CONNECT_TIMEOUT = 30000;

    public static String get(String url)
            throws IOException, ParseException, NullPointerException {
        return get(url, null, null, null);
    }

    public static String get(String url, Map<String, String> params)
            throws IOException, ParseException, NullPointerException {
        return get(url, params, null, null);
    }

    public static String get(String url, Map<String, String> params, String charset, String cookie)
            throws IOException, ParseException, NullPointerException {
        if (StringUtil.isEmpty(url))
            throw new NullPointerException("Empty url for get");

        long start_time = System.currentTimeMillis();
        String charsetUse = charset;
        if (StringUtil.isEmpty(charset))
            charsetUse = DEFAULT_CHARSET;

        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            String urlUse = url;
            if (CollectionUtil.isNotEmpty(params)) {
                StringBuilder builder = new StringBuilder(url).append("?");
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    builder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
                }

                urlUse = builder.deleteCharAt(builder.length() - 1).toString();
            }

            HttpGet httpGet = new HttpGet(urlUse);
            httpGet.addHeader("Content-Type", "text/html;charset=" + charsetUse);
            httpGet.setConfig(RequestConfig.custom()
                    .setSocketTimeout(DEFAULT_SOCKET_TIMEOUT)
                    .setConnectTimeout(DEFAULT_CONNECT_TIMEOUT)
                    .build());
            if (StringUtil.isNotEmpty(cookie))
                httpGet.setHeader("Cookie", cookie);

            return EntityUtils.toString(httpClient.execute(httpGet).getEntity(), charsetUse);
        } finally {
            try {
                httpClient.close();
            } catch (IOException ignore) {
            }

            if (logger.isInfoEnabled())
                logger.info("Get url {} in {}ms", url, System.currentTimeMillis() - start_time);
        }
    }

    public static String post(String url)
            throws IOException, ParseException, NullPointerException {
        return post(url, null, null, null);
    }

    public static String post(String url, Map<String, String> params)
            throws IOException, ParseException, NullPointerException {
        return post(url, params, null, null);
    }

    public static String post(String url, Map<String, String> params, String charset, String cookie)
            throws IOException, ParseException, NullPointerException {
        if (StringUtil.isEmpty(url))
            throw new NullPointerException("Empty url for post");

        long start_time = System.currentTimeMillis();
        String charsetUse = charset;
        if (StringUtil.isEmpty(charset))
            charsetUse = DEFAULT_CHARSET;

        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            List<NameValuePair> nvps = new ArrayList<>();
            if (CollectionUtil.isNotEmpty(params)) {
                nvps.addAll(params.entrySet().stream().map(entry -> new BasicNameValuePair(entry.getKey(), entry.getValue())).collect(Collectors.toList()));
            }

            HttpPost httppost = new HttpPost(url);
            httppost.setEntity(new UrlEncodedFormEntity(nvps, charsetUse));
            httppost.setConfig(RequestConfig.custom()
                    .setSocketTimeout(DEFAULT_SOCKET_TIMEOUT)
                    .setConnectTimeout(DEFAULT_CONNECT_TIMEOUT)
                    .build());
            if (StringUtil.isNotEmpty(cookie))
                httppost.setHeader("Cookie", cookie);

            return EntityUtils.toString(httpClient.execute(httppost).getEntity(), charsetUse);
        } finally {
            try {
                httpClient.close();
            } catch (IOException ignore) {
            }

            if (logger.isInfoEnabled())
                logger.info("Post url {} in {}ms", url, System.currentTimeMillis() - start_time);
        }
    }
}