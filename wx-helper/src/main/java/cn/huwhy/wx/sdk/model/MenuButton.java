package cn.huwhy.wx.sdk.model;

import java.util.List;

public class MenuButton {
    private List<Menu> button;

    private MenuButton menu;

    public MenuButton() {
    }

    public MenuButton(List<Menu> button) {
        this.button = button;
    }

    public List<Menu> getButton() {
        return button;
    }

    public void setButton(List<Menu> button) {
        this.button = button;
    }

    public MenuButton getMenu() {
        return menu;
    }

    public void setMenu(MenuButton menu) {
        this.menu = menu;
    }
}
