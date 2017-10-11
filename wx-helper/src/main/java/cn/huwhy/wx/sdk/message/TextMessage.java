package cn.huwhy.wx.sdk.message;

public class TextMessage extends Message{

    private String content;

    public TextMessage() {
        this.setMsgType("text");
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
