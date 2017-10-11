package cn.huwhy.wx.sdk.model;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.annotation.JSONField;

public class TemplateMessage {

    @JSONField(name = "touser")
    private String toUser;

    @JSONField(name = "template_id")
    private String templateId;

    private String url;

    private Map<String, TemplateMessageItem> data = new HashMap<>();

    public String getToUser() {
        return toUser;
    }

    public void setToUser(String toUser) {
        this.toUser = toUser;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, TemplateMessageItem> getData() {
        return data;
    }

    public void setData(Map<String, TemplateMessageItem> data) {
        this.data = data;
    }

    public void addData(String key, String value, String color) {
        this.data.put(key, new TemplateMessageItem(value, color));
    }

    public static class TemplateMessageItem {
        private String value;

        private String color;

        public TemplateMessageItem() {
        }

        public TemplateMessageItem(String value) {
            this.value = value;
        }

        public TemplateMessageItem(String value, String color) {
            this.value = value;
            this.color = color;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }
    }

}
