package cn.huwhy.wx.sdk.api;

import java.io.IOException;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.ImmutableMap;

import cn.huwhy.wx.sdk.model.Menu;
import cn.huwhy.wx.sdk.model.MenuButton;
import cn.huwhy.wx.sdk.model.Result;

import static java.util.Arrays.asList;

public class MenuApi {

    private static final String CREATE_URI = "https://api.weixin.qq.com/cgi-bin/menu/create";

    private static final String GET_URI = "https://api.weixin.qq.com/cgi-bin/menu/get";

    public static MenuButton get(String accessToken) {
        Result result = HttpClientUtil.get(GET_URI, ImmutableMap.of("access_token", accessToken), MenuButton.class);
        return result.isOk() ? (MenuButton) result.getData() : null;
    }

    public static Result create(String accessToken, MenuButton button) throws IOException {
        return HttpClientUtil.post(CREATE_URI, accessToken, JSON.toJSONString(button));
    }

    public static void main(String[] args) throws IOException {
        String accessToken = "K1pjPMys_9xgZQFdtke8mzQKbFugoZWPe9zyxLkX3l67X6J5Ob_NAQR6t8AG6NMEBEhKsqnaJk7OOo9_dhv3UoTaGRSCIYMtPo8b6gNDzyhrIfyuWkAD0zSoJhHFgGqvSNZdAEALGS";
        MenuButton mb = get(accessToken);
        Menu menu = new Menu();
        menu.setName("买家");
        Menu subMenu = new Menu();
        subMenu.setName("卖家");
        subMenu.setType("view");
        subMenu.setUrl("http://angel.huwhy.cn/mp-article/5.html");
        menu.setSubMenus(asList(subMenu));

        Result r = create(accessToken, new MenuButton(asList(menu)));
        System.out.println(r);
    }
}
