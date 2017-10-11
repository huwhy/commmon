package cn.huwhy.wx.sdk.api;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;

import cn.huwhy.wx.sdk.model.Result;

public class HttpClientUtil {

    private static Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);

    private static CloseableHttpClient httpClient;
    private static CloseableHttpClient client = HttpClients.custom().build();

    public static void setHttpClient(CloseableHttpClient httpClient) {
        HttpClientUtil.httpClient = httpClient;
    }

    public static Result post(String url, String accessToken, String json) throws IOException {
        return post(url, accessToken, json, null);
    }

    public static Result post(String url, String accessToken, String json, Class clazz) throws IOException {
        StringBuilder sbUrl = new StringBuilder(url);
        if (!Strings.isNullOrEmpty(accessToken)) {
            if (sbUrl.indexOf("?") != -1) {
                sbUrl.append("&access_token=").append(accessToken);
            } else {
                sbUrl.append("?access_token=").append(accessToken);
            }
        }

        HttpPost httpPost = new HttpPost(sbUrl.toString());
        if (json != null) {
            StringEntity entity = new StringEntity(json, Consts.UTF_8);
            httpPost.setEntity(entity);
        }

        try (CloseableHttpResponse response = getClient().execute(httpPost)) {
            return getResult(response, clazz);
        }
    }

    public static String postXml(String url, String data) throws IOException, ParserConfigurationException, SAXException {
        HttpPost httpPost = new HttpPost(url);
        if (data != null) {
            StringEntity entity = new StringEntity(data, Consts.UTF_8);
            httpPost.setEntity(entity);
        }
        try (CloseableHttpResponse response = getClient().execute(httpPost)) {
            final StatusLine statusLine = response.getStatusLine();
            final HttpEntity entity = response.getEntity();
            if (statusLine.getStatusCode() >= 300) {
                EntityUtils.consume(entity);
                return "";
            }
            if (entity == null)
                return "";
            return EntityUtils.toString(entity, Consts.UTF_8);
        }

    }

    public static Result postForm(String url, String accessToken, Map<String, Object> params, Class clazz) throws IOException {
        StringBuilder sbUrl = new StringBuilder(url);
        if (sbUrl.indexOf("?") != -1) {
            sbUrl.append("&access_token=").append(accessToken);
        } else {
            sbUrl.append("?access_token=").append(accessToken);
        }

        HttpPost httpPost = new HttpPost(sbUrl.toString());

        if (params != null && !params.isEmpty()) {
            List<NameValuePair> pairs = new ArrayList<>();
            for (Map.Entry<String, Object> entity : params.entrySet()) {
                NameValuePair pair = new BasicNameValuePair(entity.getKey(), Objects.toString(entity.getValue()));
                pairs.add(pair);
            }
            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(pairs, "UTF-8");
            httpPost.setEntity(formEntity);
        }

        try (CloseableHttpResponse response = getClient().execute(httpPost)) {
            return getResult(response, clazz);
        }
    }

    public static Result get(String url, Map<String, String> params, Class clazz) {
        String uri = buildUri(url, params);
        HttpGet httpGet = new HttpGet(uri);
        try {
            try (CloseableHttpResponse response = getClient().execute(httpGet)) {
                return getResult(response, clazz);
            }
        } catch (IOException e) {
            return new Result(1, e.getMessage());
        }
    }

    private static Result getResult(CloseableHttpResponse response, Class clazz) throws IOException {
        final StatusLine statusLine = response.getStatusLine();
        final HttpEntity entity = response.getEntity();
        if (statusLine.getStatusCode() >= 300) {
            EntityUtils.consume(entity);
            return new Result(statusLine.getStatusCode(), statusLine.getReasonPhrase());
        }
        if (entity == null)
            return new Result(1, "no result");
        String text = EntityUtils.toString(entity, Consts.UTF_8);
        Result result = JSONObject.parseObject(text, Result.class);
        logger.debug("response json: {}", text);
        if (result.isOk() && clazz != null) {
            result.setData(JSONObject.parseObject(text, clazz));
        }
        return result;
    }

    private static String buildUri(String url, Map<String, String> params) {
        StringBuilder uri = new StringBuilder(url);
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (uri.indexOf("?") != -1) {
                    uri.append("&").append(entry.getKey()).append("=").append(entry.getValue());
                } else {
                    uri.append("?").append(entry.getKey()).append("=").append(entry.getValue());
                }
            }
        }
        return uri.toString();
    }

    private static CloseableHttpClient getClient() {
        return httpClient != null ? httpClient : client;
    }

    public static void main(String[] args) throws IOException {
        HttpPost post = new HttpPost("https://s.taobao.com/image");
        File file = new File("/data/taobao.jpg");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        FileInputStream f = new FileInputStream(file);
        byte[] buf = new byte[1024];
        while (f.read(buf) != -1) {
            out.write(buf);
        }
        ByteArrayEntity ent = new ByteArrayEntity(out.toByteArray(), ContentType.create("multipart/form-data", Charset.defaultCharset()));
        post.setEntity(ent);
//        post.setHeader("Referer", "https://s.taobao.com");
//        post.setHeader("Origin", "https://s.taobao.com");
        HttpClientBuilder clientBuilder = HttpClients.custom();
        CloseableHttpClient client = clientBuilder.build();
        CloseableHttpResponse response = client.execute(post);
        final StatusLine statusLine = response.getStatusLine();
        System.out.println(statusLine.getStatusCode());
        final HttpEntity entity = response.getEntity();
        InputStream in = entity.getContent();
        byte[] bytes = new byte[1024];
        in.read(bytes);
        System.out.println(new String(bytes));
    }
}
