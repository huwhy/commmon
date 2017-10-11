package cn.huwhy.wx.sdk.model;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

public class UserInfoList {
    @JSONField(name = "user_list")
    private List<UserInfo> list;

    public List<UserInfo> getList() {
        return list;
    }

    public void setList(List<UserInfo> list) {
        this.list = list;
    }
}
