package cn.wanhui.pos.util;

import cn.wanhui.pos.data.Rsp;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jpos.iso.ISOMsg;
import org.jpos.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.ParameterizedType;
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

    public static <T> T sendAndReceive(String url, Map<String, String> params, Class<T> clazz) throws Exception {
        T result = null;
        String content = null;
        try {
            //content = HttpUtil.doPost(url, params, default_connect_timeout, default_read_timeout);
            content = HttpUtil.doGet(url, null);
            log.info(String.format("receive:%s", content));
            if (!HttpUtil.StringUtils.isEmpty(content)) {
                result = JSONArray.parseObject(content, clazz);
            }
        } catch (Exception e) {
            log.info(String.format("exception context --> url:%s, params:\n%s\ncontent:\n%s", url,
                    JSONArray.toJSONString(params, true), content), e);
            throw e;
        }
        return result;
    }
}
