package cn.wanhui.pos.trx;

import cn.wanhui.pos.Context;

/**
 * @author yinheli
 */
public interface Trx {

    public void doTrx(Context ctx) throws Exception;

}
