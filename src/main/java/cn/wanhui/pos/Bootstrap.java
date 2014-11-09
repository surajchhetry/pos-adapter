package cn.wanhui.pos;

import cn.wanhui.pos.trx.*;
import cn.wanhui.pos.util.LoggerUtil;
import org.jpos.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yinheli
 */
public class Bootstrap {

    private static final Log log = LoggerUtil.getLog(Bootstrap.class.getSimpleName());

    private static final Map<String, Trx> trxMap = new HashMap<String, Trx>(){{
        put("0800/920000", new Checkin());                  // 签到
        put("320000", new CardsQuery());                    // 卡查询
        put("760000", new TransConsume());                  // 消费
        put("770000", new TransCancel());                   // 消费撤销
        put("210000", new TransRefund());                   // 退货
        put("7A0000", new TransRefundCancel());             // 退货撤销
        put("6B0000", new TicketQuery());                   // 券查询
        put("6C0000", new TicketConsume());                 // 券消费
        put("7B0000", new SendVerifyCode());                // 发送验证码
        put("720000", new CardActive());                    //卡激活
        put("920000", new Settle());                        // 结算
    }};

    public void doTrx(Context ctx) throws Exception {
        Trx trx = null;

        String mti = ctx.reqMsg.getMTI();
        String f3 =  ctx.reqMsg.getString(3);

        String key = f3;
        if ("0800".equals(mti)) {
            key = String.format("%s/%s", mti, f3);
        }
        trx = trxMap.containsKey(key) ? trxMap.get(key) : null;

        String trans = trx != null ? trx.getClass().getSimpleName() : "NULL";

        log.info(String.format("transCode: %s, Trx: %s", key, trans));

        if (trx == null) {
            ctx.returnMsg(ctx.reqMsg, "40");
            return;
        }

        trx.doTrx(ctx, "0400".equals(ctx.reqMsg.getMTI()));
    }

}
