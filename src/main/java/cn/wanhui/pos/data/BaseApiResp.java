package cn.wanhui.pos.data;

/**
 * @author yinheli
 */
public class BaseApiResp {
    private String status;
    private String message;
    private long transTime;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTransTime() {
        return transTime;
    }

    public void setTransTime(long transTime) {
        this.transTime = transTime;
    }
}
