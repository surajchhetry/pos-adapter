package cn.wanhui.pos.data;

/**
 * @author yinheli
 */
public class Vaucher {
    private long id;
    private Double amount;
    private int num;
    private String name;

    public Vaucher() {
    }

    public Vaucher(long id, int num) {
        this.id = id;
        this.num = num;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
