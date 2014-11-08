package cn.wanhui.pos.trx;

import cn.wanhui.pos.Context;
import cn.wanhui.pos.data.BaseApiResp;
import cn.wanhui.pos.data.Vaucher;
import cn.wanhui.pos.util.Commons;
import cn.wanhui.pos.util.LoggerUtil;
import com.alibaba.fastjson.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.jpos.iso.ISOMsg;
import org.jpos.util.Log;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yinheli
 */
public class TransConsume implements Trx {

    private static final Log log = LoggerUtil.getLog(TransConsume.class.getSimpleName());

    private static final String path = "/jpos/trans/consume";

    @Override
    public void doTrx(Context ctx) throws Exception {

        final ISOMsg reqMsg = ctx.reqMsg;

        Map params = new HashMap(){{
            put("traceNo", reqMsg.getString(11));
            put("batchNo", reqMsg.getString(60));
            put("storeNo", reqMsg.getString(42));
            put("terminalNo", reqMsg.getString(41));
            put("cardNo", reqMsg.getString(2));

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

            String f63 = new String(reqMsg.getBytes(63));
            BigDecimal m100 = new BigDecimal("100");
            double cashAmount =  new BigDecimal(f63.substring(0, 12)).divide(m100).doubleValue();
            int pointAmount = Integer.parseInt(f63.substring(12, 24));

            put("pointAmount", pointAmount);
            put("cashAmount",  cashAmount);

            String registerTraceNo = f63.substring(37, 48);

            String vaucherStr = f63.substring(48);
            int vaucherLen = 22;
            int vaucherCount = vaucherStr.length() / vaucherLen;
            List<Vaucher> vaucher = new ArrayList<Vaucher>();
            for (int i = 0; i < vaucherCount; i++) {
                int start =  i * vaucherLen;
                String id =  vaucherStr.substring(start, start+20);
                String num = vaucherStr.substring(start+20, start+vaucherLen);
                vaucher.add(new Vaucher(Long.parseLong(id), Integer.parseInt(num)));
            }

            put("vaucher", JSONArray.toJSONString(vaucher));
            put("registerTraceNo", registerTraceNo);
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

        msg.set(37, result.serialNo);

        // f60
        // 当前积分余额, 当前可用积分, 本次使用积分, 本次积分增加
        StringBuilder f60 = new StringBuilder();
        f60.append(StringUtils.leftPad(result.pointBalance+"", 12));
        f60.append(StringUtils.leftPad(result.avaiablePointBalance+"", 12));
        f60.append(StringUtils.leftPad(result.usePoint+"", 12));
        f60.append(StringUtils.leftPad("", 12*4));
        f60.append(StringUtils.leftPad(result.persentPoint+"", 12));
        f60.append(StringUtils.leftPad("", 18));

        msg.set(60, f60.toString().getBytes());
        msg.set(62, result.advertisement.getBytes("gb2312"));

        ctx.returnMsg(msg, result.getStatus());
    }

    public static class Result extends BaseApiResp {
        private String serialNo;
        private int pointBalance;
        private int avaiablePointBalance;
        private int usePoint;
        private int persentPoint;
        private String advertisement;

        public String getSerialNo() {
            return serialNo;
        }

        public void setSerialNo(String serialNo) {
            this.serialNo = serialNo;
        }

        public int getPointBalance() {
            return pointBalance;
        }

        public void setPointBalance(int pointBalance) {
            this.pointBalance = pointBalance;
        }

        public int getAvaiablePointBalance() {
            return avaiablePointBalance;
        }

        public void setAvaiablePointBalance(int avaiablePointBalance) {
            this.avaiablePointBalance = avaiablePointBalance;
        }

        public int getUsePoint() {
            return usePoint;
        }

        public void setUsePoint(int usePoint) {
            this.usePoint = usePoint;
        }

        public int getPersentPoint() {
            return persentPoint;
        }

        public void setPersentPoint(int persentPoint) {
            this.persentPoint = persentPoint;
        }

        public String getAdvertisement() {
            return advertisement;
        }

        public void setAdvertisement(String advertisement) {
            this.advertisement = advertisement;
        }
    }
}
