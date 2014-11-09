package cn.wanhui.pos.trx;

import cn.wanhui.pos.Context;
import cn.wanhui.pos.data.BaseApiResp;
import cn.wanhui.pos.util.Commons;
import cn.wanhui.pos.util.LoggerUtil;
import com.alibaba.fastjson.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.jpos.iso.ISOMsg;
import org.jpos.util.Log;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yinheli
 */
public class TransCancel implements Trx {

    private static final Log log = LoggerUtil.getLog(TransCancel.class.getSimpleName());

    private static final String path = "/jpos/trans/cancel";

    private static final String reversalPath = "/jpos/trans/cancelReversal";

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

        String url = ctx.apiBaseUrl + (isReversal ? reversalPath : path);
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

        // f60
        // 当前积分余额, 当前可用积分, 本次使用积分, 本次积分增加
        StringBuilder f60 = new StringBuilder();
        BigDecimal m100 = new BigDecimal("100");
        f60.append(StringUtils.leftPad(new BigDecimal(result.pointBalance).multiply(m100).toString(), 12));
        f60.append(StringUtils.leftPad(new BigDecimal(result.avaiablePointBalance).multiply(m100).toString(), 12));
        f60.append(StringUtils.leftPad(new BigDecimal(result.usePoint).multiply(m100).toString(), 12));
        f60.append(StringUtils.leftPad("", 12*4));
        f60.append(StringUtils.leftPad(new BigDecimal(result.persentPoint).multiply(m100).toString()+"", 12));
        f60.append(StringUtils.leftPad("", 18));

        msg.set(60, f60.toString().getBytes());

        ctx.returnMsg(msg, result.getStatus());
    }

    public static class Result extends BaseApiResp {
        private String serialNo;
        private double pointBalance;
        private double avaiablePointBalance;
        private double usePoint;
        private double persentPoint;

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

        public double getUsePoint() {
            return usePoint;
        }

        public void setUsePoint(double usePoint) {
            this.usePoint = usePoint;
        }

        public double getPersentPoint() {
            return persentPoint;
        }

        public void setPersentPoint(double persentPoint) {
            this.persentPoint = persentPoint;
        }
    }
}
