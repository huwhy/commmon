package cn.huwhy.wx.sdk.message;

public class VoiceMessge extends Message {
    private String mediaId;
    private String format;

    public VoiceMessge() {
        this.setMsgType("voice");
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}
