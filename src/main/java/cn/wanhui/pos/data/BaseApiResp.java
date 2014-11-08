package cn.wanhui.pos.data;

/**
 * @author yinheli
 */
public class BaseApiResp {
    private String status;
    private String message;
    private Long transTime;

    public Long getTransTime() {
        return transTime;
    }

    public void setTransTime(Long transTime) {
        this.transTime = transTime;
    }

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
}
