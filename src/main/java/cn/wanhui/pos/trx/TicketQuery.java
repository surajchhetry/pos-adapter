package cn.wanhui.pos.trx;

import cn.wanhui.pos.Context;
import cn.wanhui.pos.data.BaseApiResp;
import cn.wanhui.pos.data.Ticket;
import cn.wanhui.pos.util.Commons;
import cn.wanhui.pos.util.LoggerUtil;
import com.alibaba.fastjson.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.util.Log;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yinheli
 */
public class TicketQuery implements Trx {

    private static final Log log = LoggerUtil.getLog(Checkin.class.getSimpleName());

    private static final String path = "/jpos/tickets";

    @Override
    public void doTrx(Context ctx, boolean isReversal) throws Exception {

        final ISOMsg reqMsg = ctx.reqMsg;

        Map<String, String> params = new HashMap<String, String>(){{
            put("traceNo", reqMsg.getString(11));
            put("batchNo", reqMsg.getString(60));
            put("terminalNo", reqMsg.getString(41));
            put("storeNo", reqMsg.getString(42));
            put("cardNo", reqMsg.getString(61).trim());
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

        StringBuilder f60 = new StringBuilder();
        ByteArrayOutputStream f63 = new ByteArrayOutputStream();
        result.ticket = new ArrayList<Ticket>();
        int count = 0;
        for (Ticket t : result.ticket) {
            count++;
            f60.append(StringUtils.leftPad(Integer.parseInt(t.getType()) +"", 2))
                    .append(StringUtils.leftPad(new BigDecimal(t.getAmount()).multiply(new BigDecimal("100")).toString(), 6, "0"))
                    .append(StringUtils.leftPad(t.getNum() + "", 2, "0"));

            f63.write(StringUtils.leftPad(Integer.parseInt(t.getType()) +"", 2).getBytes());
            f63.write(Commons.fillRight(t.getName(), 23, (byte) 0x20));
            f63.write(Commons.fillLeft(t.getId()+"", 20, (byte) 0x30));
        }

        if (count < 20) {
            for (int i = 0, j = 20-count; i < j; i ++) {
                f60.append(" 0");//种类
                f60.append("     0");//金额
                f60.append(" 0");//张数
            }
        }

        f63.flush();
        msg.set(60, f60.toString().getBytes());
        msg.set(63, f63.toByteArray());
        msg.set(60, ISOUtil.hex2byte("2030202020202030203020302020202020302030203020202020203020302030202020202030203020302020202020302030203020202020203020302030202020202030203020302020202020302030203020202020203020302030202020202030203020302020202020302030203020202020203020302030202020202030203020302020202020302030203020202020203020302030202020202030203020302020202020302030203020202020203020302030202020202030203020302020202020302030"));
        msg.set(61, ISOUtil.hex2byte("32D7A2B2E1BBE1D4B1BFA800000000000000000000B9FAC3B3B5EA000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"));
        log.info(String.format("response: f60: %s\nf63:%s", f60.toString(), new String(f63.toByteArray(), "GBK")));
        f63.close();

        msg.unset(64);
        ctx.returnMsg(msg, result.getStatus());
    }

    public static class Result extends BaseApiResp {
        private double pointBalance;
        private double avaiablePointBalance;
        private String serialNo;
        private List<Ticket> ticket;

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

        public String getSerialNo() {
            return serialNo;
        }

        public void setSerialNo(String serialNo) {
            this.serialNo = serialNo;
        }

        public List<Ticket> getTicket() {
            return ticket;
        }

        public void setTicket(List<Ticket> ticket) {
            this.ticket = ticket;
        }
    }
}
