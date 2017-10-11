package cn.huwhy.wx.sdk.model;

import java.util.List;

public class TextGroupMessage extends GroupMessage {
    public TextGroupMessage(List<String> toUser) {
        super(toUser, "text");
    }

    private TextCustomMessage.Text text;

    public TextCustomMessage.Text getText() {
        return text;
    }

    public void setText(TextCustomMessage.Text text) {
        this.text = text;
    }

    public void setContent(String content) {
        this.text = new TextCustomMessage.Text(content);
    }
}
