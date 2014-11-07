package cn.wanhui.pos.trx;

import cn.wanhui.pos.Context;
import org.jpos.iso.ISOMsg;

/**
 * @author yinheli
 */
public class Settle implements Trx {
    @Override
    public void doTrx(Context ctx) throws Exception {
        ISOMsg msg = (ISOMsg) ctx.reqMsg.clone();
        msg.set(48, "ss");
        ctx.returnMsg(msg, "00");
    }
}
