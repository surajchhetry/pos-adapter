package cn.wanhui.pos.trx;


import cn.wanhui.pos.Context;
import cn.wanhui.pos.data.Rsp;
import cn.wanhui.pos.util.Commons;
import cn.wanhui.pos.util.DESUtil;
import cn.wanhui.pos.util.HttpUtil;
import org.jpos.iso.ISODate;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yinheli
 */
public class Checkin implements Trx {

    private static final byte[] keyCheck = new byte[8];

    private static final String path = "/jpos/terminal/checkin";

    private class Result extends Rsp {
        private String batchNo;

        public String getBatchNo() {
            return batchNo;
        }

        public void setBatchNo(String batchNo) {
            this.batchNo = batchNo;
        }
    }

    @Override
    public void doTrx(Context ctx) throws Exception {

        final ISOMsg reqMsg = ctx.reqMsg;

        Map<String, String> params = new HashMap<String, String>(){{
            put("traceNo", reqMsg.getString(11));
            put("storeNo", reqMsg.getString(42));
            put("terminalNo", reqMsg.getString(41));
            put("operatorNo", reqMsg.getString(61).trim());
        }};

        Result result = Commons.sendAndReceive(ctx.apiBaseUrl+path, params, Result.class);


        ISOMsg msg = (ISOMsg) ctx.reqMsg.clone();

        byte[] pinkey = DESUtil.generateRandomNumber(8).getBytes();
        byte[] mackey = DESUtil.generateRandomNumber(8).getBytes();

        byte[] pinkeyWitchCheckVal = ISOUtil.concat(pinkey, ISOUtil.trim(DESUtil.encrypt(keyCheck, pinkey), 4));
        byte[] mackeyWitchCheckVal = ISOUtil.concat(mackey, ISOUtil.trim(DESUtil.encrypt(keyCheck, mackey), 4));

        msg.set(60, "000001".getBytes());
        msg.set(61, "测试商户".getBytes("gb2312"));

        msg.set(62, ISOUtil.concat(pinkeyWitchCheckVal, mackeyWitchCheckVal));

        msg.unset(63);

        ctx.returnMsg(msg, "00");
    }

}
