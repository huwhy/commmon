package cn.huwhy.wx.sdk.model;

import com.alibaba.fastjson.annotation.JSONField;

public class Article {

    private String title;
    private String description;
    private String url;
    @JSONField(name = "picurl")
    private String picUrl;

    public Article() {
    }

    public Article(String title, String description, String url, String picUrl) {
        this.title = title;
        this.description = description;
        this.url = url;
        this.picUrl = picUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }
}
