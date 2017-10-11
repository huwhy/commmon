package cn.huwhy.wx.sdk.listener;

import cn.huwhy.wx.sdk.model.Command;

public class DefaultListener extends Listener {

    @Override
    public String handle(Command command) {
        return "success";
    }
}
