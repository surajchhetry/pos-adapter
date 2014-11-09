package cn.wanhui.pos.trx;

import cn.wanhui.pos.Context;
import cn.wanhui.pos.data.BaseApiResp;
import cn.wanhui.pos.util.Commons;
import cn.wanhui.pos.util.LoggerUtil;
import com.alibaba.fastjson.JSONArray;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yinheli
 */
public class Checkin implements Trx {

    private static final Log log = LoggerUtil.getLog(Checkin.class.getSimpleName());

    private static final String path = "/jpos/terminal/checkin";

    @Override
    public void doTrx(Context ctx, boolean isReversal) throws Exception {

        final ISOMsg reqMsg = ctx.reqMsg;

        Map<String, String> params = new HashMap<String, String>(){{
            put("traceNo", reqMsg.getString(11));
            put("storeNo", reqMsg.getString(42));
            put("terminalNo", reqMsg.getString(41));
            put("operatorNo", reqMsg.getString(61).trim());
        }};

        String url = ctx.apiBaseUrl + path;
        Result result = Commons.sendAndReceive(url, params, Result.class);

        if (result == null) {
            log.warn(String.format("time out!, reqMsg:\n%s\nreqURL:%s,params:%s",
                    Commons.dumpISOMsg(reqMsg), url, JSONArray.toJSONString(params, true)));
            return;
        }

        ISOMsg msg = (ISOMsg) ctx.reqMsg.clone();
        ctx.setTime(msg, result.getTransTime());
        msg.set(60, result.batchNo.getBytes());
        msg.set(61, result.storeName.getBytes("gb2312"));
        msg.set(62, ISOUtil.hex2byte(result.workingKey));
        msg.unset(63);
        ctx.returnMsg(msg, result.getStatus());
    }

    public static class Result extends BaseApiResp {
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
