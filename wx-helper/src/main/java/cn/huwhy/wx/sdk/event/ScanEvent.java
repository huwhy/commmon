package cn.huwhy.wx.sdk.event;

public class ScanEvent extends Event {
    private String ticket;

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }
}
