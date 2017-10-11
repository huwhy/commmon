package cn.huwhy.common.util.oss;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PolicyConditions;

import net.sf.json.JSONObject;

public class OssUtils {
    /**
     * 上传OSS 签名
     *
     * @param endpoint      OSS 外网域名
     * @param accessKey     accessKey
     * @param accessSecret  accessSecret
     * @param bucket        OSS Bucket名
     * @param userDir       用户自定义前缀
     * @param fileLimitSize 上传文件大小限制
     * @param expireTime    失效时间
     * @return 前段上传图片需要的参数
     * @throws UnsupportedEncodingException
     */
    public static String generalPostObjectPolicy(String scheme,
                                                 String endpoint,
                                                 String accessKey,
                                                 String accessSecret,
                                                 String bucket,
                                                 String userDir,
                                                 long fileLimitSize,
                                                 long expireTime,
                                                 String imgBaseUrl)
            throws UnsupportedEncodingException {
        OSSClient client = new OSSClient(endpoint, accessKey, accessSecret);
        PolicyConditions policyConditions = new PolicyConditions();
        policyConditions.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, userDir);
        policyConditions.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, fileLimitSize);

        long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
        String postPolicy = client.generatePostPolicy(new Date(expireEndTime), policyConditions);
        byte[] binaryData = postPolicy.getBytes("utf-8");
        String encodedPolicy = BinaryUtil.toBase64String(binaryData);
        String postSignature = client.calculatePostSignature(postPolicy);

        Map<String, String> respMap = new LinkedHashMap<>();
        respMap.put("accessKey", accessKey);
        respMap.put("policy", encodedPolicy);
        respMap.put("signature", postSignature);
        respMap.put("userDir", userDir);
        respMap.put("host", scheme + bucket + "." + endpoint);
        respMap.put("expire", String.valueOf(expireEndTime / 1000));
        respMap.put("imgBaseUrl", imgBaseUrl);
        return JSONObject.fromObject(respMap).toString();
    }

    /**
     * 文件上传到 OSS 工具类
     *
     * @param endpoint     OSS 外网域名
     * @param accessKey    accessKey
     * @param accessSecret accessSecret
     * @param bucket       OSS Bucket名
     * @param filename     上传后文件名
     * @param file         文件
     */
    public static void uploadFileToOss(String endpoint,
                                       String accessKey,
                                       String accessSecret,
                                       String bucket,
                                       String filename,
                                       File file) {
        OSSClient ossClient = new OSSClient(endpoint, accessKey, accessSecret);
        ossClient.putObject(bucket, filename, file);
        ossClient.shutdown();
    }

    /**
     * 文件上传到 OSS 工具类
     *
     * @param endpoint     OSS 外网域名
     * @param accessKey    accessKey
     * @param accessSecret accessSecret
     * @param bucket       OSS Bucket名
     * @param filename     上传后文件名
     * @param imgUrl       图片地址
     */
    public static boolean uploadFileToOss(String endpoint,
                                          String accessKey,
                                          String accessSecret,
                                          String bucket,
                                          String filename,
                                          String imgUrl) {
        try {
            OSSClient ossClient = new OSSClient(endpoint, accessKey, accessSecret);
            URL url = new URL(imgUrl);
            InputStream in = url.openConnection().getInputStream();
            ossClient.putObject(bucket, filename, in, new ObjectMetadata());
            ossClient.shutdown();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 文件上传到 OSS 工具类
     *
     * @param endpoint     OSS 外网域名
     * @param accessKey    accessKey
     * @param accessSecret accessSecret
     * @param bucket       OSS Bucket名
     * @param filename     上传后文件名
     */
    public static boolean uploadFileToOss(String endpoint,
                                          String accessKey,
                                          String accessSecret,
                                          String bucket,
                                          String filename,
                                          InputStream in) {
        OSSClient ossClient = new OSSClient(endpoint, accessKey, accessSecret);
        ossClient.putObject(bucket, filename, in, new ObjectMetadata());
        ossClient.shutdown();
        return true;
    }

}
