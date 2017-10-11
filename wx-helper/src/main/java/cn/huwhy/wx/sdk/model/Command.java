package cn.huwhy.wx.sdk.model;

public class Command {
    private String toUserName;
    private String fromUserName;
    private long createTime;
    private String msgType;
    private String event;
    private String eventKey;
    private String latitude;
    private String longitude;
    private String precision;
    private String ticket;
    private String msgId;
    private String picUrl;
    private String mediaId;
    private String title;
    private String url;
    private String description;
    private String locationX;
    private String locationY;
    private String scale;
    private String label;
    private String thumbMediaid;
    private String content;
    private String format;
    private String recognition;
    private String status;

    public String getToUserName() {
        return toUserName;
    }

    public void setToUserName(String toUserName) {
        this.toUserName = toUserName;
    }

    public String getFromUserName() {
        return fromUserName;
    }

    public void setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getEventKey() {
        return eventKey;
    }

    public void setEventKey(String eventKey) {
        this.eventKey = eventKey;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getPrecision() {
        return precision;
    }

    public void setPrecision(String precision) {
        this.precision = precision;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocationX() {
        return locationX;
    }

    public void setLocationX(String locationX) {
        this.locationX = locationX;
    }

    public String getLocationY() {
        return locationY;
    }

    public void setLocationY(String locationY) {
        this.locationY = locationY;
    }

    public String getScale() {
        return scale;
    }

    public void setScale(String scale) {
        this.scale = scale;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getThumbMediaid() {
        return thumbMediaid;
    }

    public void setThumbMediaid(String thumbMediaid) {
        this.thumbMediaid = thumbMediaid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRecognition() {
        return recognition;
    }

    public void setRecognition(String recognition) {
        this.recognition = recognition;
    }

    public String getCommandKey() {
        if (eventKey != null) {
            if(eventKey.startsWith("qrscene_")){
                return EVENT_KEYS.QRSCENE_EVENT;
            } else {
                return event;
            }
        } else if (event != null) {
            return event;
        } else if (msgType != null) {
            return msgType;
        } else {
            return EVENT_KEYS.DEFAULT;
        }
    }

    @Override
    public String toString() {
        return "Command{" +
                "toUserName='" + toUserName + '\'' +
                ", fromUserName='" + fromUserName + '\'' +
                ", createTime=" + createTime +
                ", msgType='" + msgType + '\'' +
                ", event='" + event + '\'' +
                ", eventKey='" + eventKey + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", precision='" + precision + '\'' +
                ", ticket='" + ticket + '\'' +
                ", msgId='" + msgId + '\'' +
                ", picUrl='" + picUrl + '\'' +
                ", mediaId='" + mediaId + '\'' +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", description='" + description + '\'' +
                ", locationX='" + locationX + '\'' +
                ", locationY='" + locationY + '\'' +
                ", scale='" + scale + '\'' +
                ", label='" + label + '\'' +
                ", thumbMediaid='" + thumbMediaid + '\'' +
                ", content='" + content + '\'' +
                ", format='" + format + '\'' +
                ", recognition='" + recognition + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    public static interface EVENT_KEYS {
        String TEXT_MSG = "text";
        String IMG_MSG = "image";
        String VOICE_MSG = "voice";
        String VIDEO_MSG = "video";
        String S_VIDEO_MSG = "shortvideo";
        String LOCATION_MSG = "location";
        String LINK_MSG = "link";
        String EVENT = "event";
        String SUBSCRIBE_EVENT = "subscribe";
        String UN_SUBSCRIBE_EVENT = "unsubscribe";
        String QRSCENE_EVENT = "qrscene";
        String SCAN_EVENT = "SCAN";
        String LOCATION_EVENT = "LOCATION";
        String MENU_CLICK_EVENT = "CLICK";
        String MENU_VIEW_EVENT = "VIEW";
        String TEMPLATE_MSG_END = "TEMPLATESENDJOBFINISH";
        String DEFAULT = "";
    }
}
