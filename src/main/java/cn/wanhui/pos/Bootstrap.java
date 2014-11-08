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
        put("0800/920000", new Checkin());                     // 签到
        put("0100/320000", new CardsQuery());                 // 卡查询
        put("0200/760000", new TransConsume());               // 消费
//        put("0100/320000", "trans_consume_reversal");      // 消费冲正
//        put("0100/320000", "trans_cancel");                // 消费撤销
//        put("0100/320000", "trans_cancel_reversal");       // 消费撤销冲正
//        put("0100/320000", "refunds");                     // 消费退货
//        put("0100/320000", "cancel_refunds");              // 消费退货撤销
//        put("0100/320000", "tickets_query");               // 券查询
//        put("0100/320000", "tickets_consume");             // 券消费
//        put("0100/320000", "tickets_consume_reversal");    // 券消费冲正
//        put("0100/320000", "send_verify_code");            // 发送验证码
//        put("0100/320000", "card_active");                 // 卡激活
        put("0500/920000", new Settle());                   // 结算
    }};

    public void doTrx(Context ctx) throws Exception {
        Trx trx = null;

        StringBuilder sb = new StringBuilder();
        sb.append(ctx.reqMsg.getMTI()).append("/").append(ctx.reqMsg.getString(3));
        String key = sb.toString();
        trx = trxMap.containsKey(key) ? trxMap.get(key) : null;

        String trans = trx != null ? trx.getClass().getSimpleName() : "NULL";

        log.info(String.format("transCode: %s, Trx: %s", key, trans));

        if (trx == null) {
            ctx.returnMsg(ctx.reqMsg, "40");
            return;
        }

        trx.doTrx(ctx);
    }

}
