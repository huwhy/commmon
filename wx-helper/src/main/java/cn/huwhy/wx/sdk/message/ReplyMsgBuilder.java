package cn.huwhy.wx.sdk.message;

import org.beetl.core.BeetlKit;

import com.google.common.collect.ImmutableMap;

public class ReplyMsgBuilder {

    public static final String MSG_TEMPLATE = "<xml>" +
            "<ToUserName><![CDATA[${msg.toUserName!}]]></ToUserName>" +
            "<FromUserName><![CDATA[${msg.fromUserName!}]]></FromUserName>" +
            "<CreateTime>${msg.createTime}</CreateTime>" +
            "<MsgType><![CDATA[${msg.msgType}]]></MsgType>" +
            "${content}" +
            "</xml>";

    public static final String IMG_CONTENT   = "<Image><MediaId><![CDATA[${msg.mediaId!}]]></MediaId></Image>";
    public static final String MUSIC_CONTENT = "<Music>" +
            "<Title><![CDATA[${msg.title!}]]></Title>" +
            "<Description><![CDATA[${msg.description!}]]></Description>" +
            "<MusicUrl><![CDATA[${msg.musicURL!}]]></MusicUrl>" +
            "<HQMusicUrl><![CDATA[${msg.hQMusicUrl}]]></HQMusicUrl>" +
            "<ThumbMediaId><![CDATA[${msg.thumbMediaId}]]></ThumbMediaId>" +
            "</Music>";
    public static final String NEWS_CONTENT  = "<ArticleCount>${msg.news.~size}</ArticleCount>" +
            "<Articles>" +
            "<%for(var item in msg.news!){%>" +
            "<item>" +
            "<Title><![CDATA[${item.title!}]]></Title>" +
            "<Description><![CDATA[${item.description!}]]></Description>" +
            "<PicUrl><![CDATA[${item.picUrl!}]]></PicUrl>" +
            "<Url><![CDATA[${item.url}]]></Url>" +
            "</item>" +
            "<%}%>" +
            "</Articles>";
    public static final String TEXT_CONTENT  = "<Content><![CDATA[${msg.content!}]]></Content>";
    public static final String VIDEO_CONTENT = "<Video>" +
            "<MediaId><![CDATA[${msg.mediaId!}]]></MediaId>" +
            "<Title><![CDATA[${msg.title!}]]></Title>" +
            "<Description><![CDATA[${msg.description!}]]></Description>" +
            "</Video>";
    public static final String VOICE_CONTENT = "<Voice><MediaId><![CDATA[${mediaId}]]></MediaId></Voice>";

    public static String toXml(Message message) {
        String contentTmp;
        switch (message.getMsgType()) {
            case "text":
                contentTmp = TEXT_CONTENT;
                break;
            case "image":
                contentTmp = IMG_CONTENT;
                break;
            case "voice":
                contentTmp = VOICE_CONTENT;
                break;
            case "video":
                contentTmp = VIDEO_CONTENT;
                break;
            case "music":
                contentTmp = MUSIC_CONTENT;
                break;
            case "news":
                contentTmp = NEWS_CONTENT;
                break;
            default:
                contentTmp = TEXT_CONTENT;
                break;
        }
        String content = BeetlKit.render(contentTmp, ImmutableMap.of("msg", message));
        return BeetlKit.render(MSG_TEMPLATE, ImmutableMap.of("msg", message, "content", content));
    }

    public static void main(String[] args) {
        TextMessage message = new TextMessage();
        message.setContent("hello, world!");
        System.out.println(toXml(message));
    }

}
