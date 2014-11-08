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

    private static final String path = "/jpos/terminal/checkin";

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
        msg.set(60, result.batchNo.getBytes());
        msg.set(61, result.storeName.getBytes("gb2312"));
        msg.set(62, ISOUtil.hex2byte(result.workingKey));
        msg.unset(63);
        ctx.returnMsg(msg, result.getStatus());
    }

    public static class Result extends Rsp {
        private String batchNo;
        private String storeName;
        private String workingKey;

        public String getBatchNo() {
            return batchNo;
        }

        public void setBatchNo(String batchNo) {
            this.batchNo = batchNo;
        }

        public String getStoreName() {
            return storeName;
        }

        public void setStoreName(String storeName) {
            this.storeName = storeName;
        }

        public String getWorkingKey() {
            return workingKey;
        }

        public void setWorkingKey(String workingKey) {
            this.workingKey = workingKey;
        }
    }

}
