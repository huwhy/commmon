package cn.huwhy.wx.sdk.listener;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.huwhy.wx.sdk.model.Command;

public final class EventHandler {
    private static Logger                log         = LoggerFactory.getLogger(EventHandler.class);
    private        Map<String, Listener> listenerMap = new HashMap<String, Listener>();

    public EventHandler() {}

    public String handler(Command command) {
        return getListener(command).handle(command);
    }

    private Listener getListener(Command command) {
        Listener listener = listenerMap.get(command.getCommandKey());
        return listener == null ? new DefaultListener() : listener;
    }

    public void register(String commandKey, Listener listener) {
        listenerMap.put(commandKey, listener);
    }
}
