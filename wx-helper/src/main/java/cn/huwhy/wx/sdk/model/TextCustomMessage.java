package cn.huwhy.wx.sdk.model;

public class TextCustomMessage extends CustomMessage {

    private Text text;

    public TextCustomMessage(String toUser) {
        super(toUser, "text");
    }

    public Text getText() {
        return text;
    }

    public void setText(Text text) {
        this.text = text;
    }

    public void setContent(String content) {
        this.text = new Text(content);
    }

    public static class Text {

        private String content;

        public Text() {
        }

        public Text(String content) {
            this.content = content;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

}
