package cn.wanhui.pos.trx;

import cn.wanhui.pos.Context;
import cn.wanhui.pos.data.BaseApiResp;
import cn.wanhui.pos.util.Commons;
import cn.wanhui.pos.util.LoggerUtil;
import com.alibaba.fastjson.JSONArray;
import org.jpos.iso.ISOMsg;
import org.jpos.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yinheli
 */
public class TransRefund implements Trx {

    private static final Log log = LoggerUtil.getLog(TransRefund.class.getSimpleName());

    private static final String path = "/jpos/refunds";

    @Override
    public void doTrx(Context ctx, boolean isReversal) throws Exception {
        final ISOMsg reqMsg = ctx.reqMsg;

        Map params = new HashMap(){{
            put("traceNo", reqMsg.getString(11));
            put("storeNo", reqMsg.getString(42));
            put("terminalNo", reqMsg.getString(41));

            if (reqMsg.hasField(2)) {
                put("cardNo", reqMsg.getString(2));
            }

            if (reqMsg.hasField(35)) {
                put("track2", reqMsg.getString(35));
            }

            if (reqMsg.hasField(36)) {
                put("track3", reqMsg.getString(36));
            }

            if (reqMsg.hasField(62)) {
                put("password", reqMsg.getString(62));
            }

            put("consumeType", reqMsg.getString(22));
            put("oriSerialNo", reqMsg.getString(37));

            String f60 = new String(reqMsg.getBytes(60));
            put("oriBatchNo", f60.substring(0, 6));
            put("oriTraceNo", f60.substring(6, 12));
            String time = Calendar.getInstance().get(Calendar.YEAR)+f60.substring(12, 22);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhss");
            put("oriTransTime", sdf.parse(time).getTime()/1000);
        }};

        String url = ctx.apiBaseUrl + path;
        Result result = Commons.sendAndReceive(url, params, Result.class);

        if (result == null) {
            log.warn(String.format("time out!, reqMsg:\n%s\nreqURL:%s,params:%s",
                    Commons.dumpISOMsg(reqMsg), url, JSONArray.toJSONString(params, true)));
            return;
        }

        if (!"00".equals(result.getStatus())) {
            ctx.returnMsg(result.getStatus());
            return;
        }

        ISOMsg msg = (ISOMsg) ctx.reqMsg.clone();
        ctx.setTime(msg, result.getTransTime());
        msg.set(37, result.serialNo);

        ctx.returnMsg(msg, result.getStatus());
    }

    public static class Result extends BaseApiResp {
        private String serialNo;
        private double pointBalance;
        private double avaiablePointBalance;
        private double returnPoint;
        private double returnPersentPoint;
        private double returnCash;
        private double supplyCash;

        public String getSerialNo() {
            return serialNo;
        }

        public void setSerialNo(String serialNo) {
            this.serialNo = serialNo;
        }

        public double getPointBalance() {
            return pointBalance;
        }

        public void setPointBalance(double pointBalance) {
            this.pointBalance = pointBalance;
        }

        public double getAvaiablePointBalance() {
            return avaiablePointBalance;
        }

        public void setAvaiablePointBalance(double avaiablePointBalance) {
            this.avaiablePointBalance = avaiablePointBalance;
        }

        public double getReturnPoint() {
            return returnPoint;
        }

        public void setReturnPoint(double returnPoint) {
            this.returnPoint = returnPoint;
        }

        public double getReturnPersentPoint() {
            return returnPersentPoint;
        }

        public void setReturnPersentPoint(double returnPersentPoint) {
            this.returnPersentPoint = returnPersentPoint;
        }

        public double getReturnCash() {
            return returnCash;
        }

        public void setReturnCash(double returnCash) {
            this.returnCash = returnCash;
        }

        public double getSupplyCash() {
            return supplyCash;
        }

        public void setSupplyCash(double supplyCash) {
            this.supplyCash = supplyCash;
        }
    }
}
