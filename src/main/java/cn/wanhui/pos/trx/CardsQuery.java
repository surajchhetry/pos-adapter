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

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yinheli
 */
public class CardsQuery implements Trx {

    private static final Log log = LoggerUtil.getLog(CardsQuery.class.getSimpleName());

    private static final String path = "/jpos/cards";

    @Override
    public void doTrx(Context ctx, boolean isReversal) throws Exception {
        final ISOMsg reqMsg = ctx.reqMsg;

        Map<String, String> params = new HashMap<String, String>(){{
            put("traceNo", reqMsg.getString(11));
            put("batchNo", reqMsg.getString(60));
            put("storeNo", reqMsg.getString(42));
            put("terminalNo", reqMsg.getString(41));
            put("cardNo", reqMsg.getString(2));
            put("operatorNo", reqMsg.getString(61).trim());
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

        BigDecimal m100 = new BigDecimal("100");

        // f60
        // 当前积分余额+当前可用积分+代金券
        ByteArrayOutputStream f60 = new ByteArrayOutputStream();

        // f62
        // 代金券名称 + 代金券ID
        ByteArrayOutputStream f62 = new ByteArrayOutputStream();

        try {
            f60.write(StringUtils.leftPad(new BigDecimal(result.pointBalance).multiply(m100).toString(), 12).getBytes());
            f60.write(StringUtils.leftPad(new BigDecimal(result.avaiablePointBalance).multiply(m100).toString(), 12).getBytes());
            f60.write(StringUtils.leftPad("0", 12).getBytes());
            f60.write(StringUtils.leftPad("0", 12).getBytes());
            f60.write(StringUtils.leftPad("0", 12).getBytes());
            f60.write(StringUtils.leftPad("0", 12).getBytes());
            f60.write(StringUtils.leftPad("0", 12).getBytes());
            f60.write(StringUtils.leftPad("0", 6).getBytes());
            f60.write(StringUtils.leftPad("0", 6).getBytes());
            f60.write(StringUtils.leftPad("0", 6).getBytes());


            for (Vaucher v : result.getVaucher()) {
                // f60
                BigDecimal amount = new BigDecimal(Double.toString(v.getAmount()));
                amount = amount.multiply(m100);
                f60.write(StringUtils.leftPad(amount.longValue() + "", 6).getBytes());
                f60.write(StringUtils.leftPad(Integer.toString(v.getNum()), 2).getBytes());

                // f62
                f62.write(Commons.fillLeft(v.getName(), 23, (byte) 0x20));
                f62.write(StringUtils.leftPad(Long.toString(v.getId()), 20, "0").getBytes());

            }

            f60.flush();
            f62.flush();

            msg.set(60, f60.toByteArray());
            msg.set(62, f62.toByteArray());
        } finally {
            f60.close();
            f62.close();
        }


        // f63
        // 证件类型, 证件号码, 姓名...
        ByteArrayOutputStream f63 = new ByteArrayOutputStream();
        try {
            f63.write(result.idType.getBytes());
            f63.write(StringUtils.leftPad(result.idNo, 32).getBytes());

            f63.write(Commons.fillLeft(result.name, 32, (byte) 0x30));

            f63.write(result.gender.getBytes());
            f63.write(result.mobileNo.getBytes());
            f63.write(result.grade.getBytes());

            f63.write(Commons.fillLeft(result.address, 80, (byte) 0x20));
            f63.write(Commons.fillLeft(result.email, 40, (byte) 0x20));

            f63.flush();
            msg.set(63, f63.toByteArray());
        } finally {
            f63.close();
        }

        ctx.returnMsg(msg, result.getStatus());
    }

    public static class Result extends BaseApiResp {
        private double pointBalance;
        private double avaiablePointBalance;
        private String idType;
        private String idNo;
        private String name;
        private String gender;
        private String mobileNo;
        private String grade;
        private String address;
        private String email;
        private List<Vaucher> vaucher;

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

        public String getIdType() {
            return idType;
        }

        public void setIdType(String idType) {
            this.idType = idType;
        }

        public String getIdNo() {
            return idNo;
        }

        public void setIdNo(String idNo) {
            this.idNo = idNo;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public String getMobileNo() {
            return mobileNo;
        }

        public void setMobileNo(String mobileNo) {
            this.mobileNo = mobileNo;
        }

        public String getGrade() {
            return grade;
        }

        public void setGrade(String grade) {
            this.grade = grade;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public List<Vaucher> getVaucher() {
            return vaucher;
        }

        public void setVaucher(List<Vaucher> vaucher) {
            this.vaucher = vaucher;
        }
    }
}
