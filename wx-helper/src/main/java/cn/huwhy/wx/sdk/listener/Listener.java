package cn.huwhy.wx.sdk.listener;

import cn.huwhy.wx.sdk.model.Command;

public abstract class Listener {
    public abstract String handle(Command command);
}
