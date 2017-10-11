package cn.huwhy.wx.sdk.message;

public class ImageMessage extends Message{

    private String picUrl;
    private String mediaId;

    public ImageMessage() {
        this.setMsgType("image");
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

}
