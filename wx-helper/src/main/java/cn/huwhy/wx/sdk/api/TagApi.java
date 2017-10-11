package cn.huwhy.wx.sdk.api;

import java.io.IOException;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.ImmutableMap;

import cn.huwhy.wx.sdk.model.Result;
import cn.huwhy.wx.sdk.model.Tag;
import cn.huwhy.wx.sdk.model.Tags;

public class TagApi {
    private static String CREATE_API = "https://api.weixin.qq.com/cgi-bin/tags/create";
    private static String GET_API    = "https://api.weixin.qq.com/cgi-bin/tags/get";
    private static String UPDATE_API = "https://api.weixin.qq.com/cgi-bin/tags/update";
    private static String DELETE_API = "https://api.weixin.qq.com/cgi-bin/tags/delete";

    public static Tags create(String accessToken, Tags tags) throws IOException {
        Result result = HttpClientUtil.post(CREATE_API, accessToken, JSON.toJSONString(tags), Tags.class);
        return result.isOk() ? (Tags) result.getData() : null;
    }

    public static Tags get(String accessToken) {
        Result result = HttpClientUtil.get(GET_API, ImmutableMap.of("access_token", accessToken), Tags.class);
        return result.isOk() ? (Tags) result.getData() : null;
    }

    public static Tags update(String accessToken, Tags tags) throws IOException {
        Result result = HttpClientUtil.post(UPDATE_API, accessToken, JSON.toJSONString(tags), Tags.class);
        return result.isOk() ? (Tags) result.getData() : null;
    }

    public static Boolean delete(String accessToken, Tags tags) throws IOException {
        Result result = HttpClientUtil.post(DELETE_API, accessToken, JSON.toJSONString(tags));
        return result.isOk();
    }

    public static void main(String[] args) throws IOException {
        String accessToken = "5DsDwC6UIpd75YzvCz20o0fpRX2XzDMwK-d1nX7U4aNKRelnmvQRCQCS-bVbpyb0ysalPvtxRot2vDY2QWOJUY2jpgfhQCfKSWYTw9PgTvgUs6O6GDTK_APC8LgL4n2gMYHjAHARDM";
        Tags tags = new Tags();
        Tag tag = new Tag();
        tag.setId(100);
        tag.setName("新用户2");
        tags.setTag(tag);
        delete(accessToken, tags);
        Tags rt = get(accessToken);
        System.out.println(rt);
    }
}
