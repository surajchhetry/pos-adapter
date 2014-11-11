package cn.wanhui.pos.util;

import com.alibaba.fastjson.JSONArray;
import org.jpos.iso.ISOMsg;
import org.jpos.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Map;

/**
 * @author yinheli
 */
public class Commons {

    private static final Log log = LoggerUtil.getLog(Commons.class.getSimpleName());

    private static final int default_connect_timeout = 5000;
    private static final int default_read_timeout = 60000;

    public static String dumpISOMsg(ISOMsg msg) {
        if (msg == null) {
            return "NULL";
        }
        try {
            ISOMsg m = (ISOMsg) msg.clone();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos, false, "GBK");
            m.dump(ps, "");
            ps.flush();
            baos.flush();
            return new String(baos.toByteArray());
        } catch (IOException e) {
            log.info(e);
        }

        return "";
    }

    public static byte[] getMac(ISOMsg msg) {
        return new byte[8];
    }

    public static byte[] fillLeft(String str, int len, byte fill) throws UnsupportedEncodingException {
        byte[] v = new byte[len];
        Arrays.fill(v, fill);
        byte[] _v = str.getBytes("gb2312");
        int start = v.length - _v.length;
        System.arraycopy(_v, 0, v, start, _v.length);
        return v;
    }

    public static byte[] fillRight(String str, int len, byte fill) throws UnsupportedEncodingException {
        byte[] v = new byte[len];
        Arrays.fill(v, fill);
        byte[] _v = str.getBytes("gb2312");
        System.arraycopy(_v, 0, v, 0, _v.length);
        return v;
    }

    public static <T> T sendAndReceive(String url, Map<String, String> params, Class<T> clazz) throws Exception {
        T result = null;
        String content = null;
        String paramJson = null; // for debug
        try {
            paramJson = JSONArray.toJSONString(params, true);
            log.info(String.format("send --> url:%s, params:\n%s", url, paramJson));
//            content = HttpUtil.doPost(url, params, default_connect_timeout, default_read_timeout);
            content = HttpUtil.doGet(url, null);
            log.info(String.format("receive:%s", content));
            if (!HttpUtil.StringUtils.isEmpty(content)) {
                result = JSONArray.parseObject(content, clazz);
            }
        } catch (Exception e) {
            log.info(String.format("exception context --> url:%s, params:\n%s\ncontent:\n%s", url,
                    paramJson, content), e);
            throw e;
        }
        return result;
    }
}
