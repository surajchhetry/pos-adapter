package cn.wanhui.pos.util;

import org.jpos.iso.ISOMsg;
import org.jpos.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * @author yinheli
 */
public class Commons {

    private static final Log log = LoggerUtil.getLog(Commons.class.getSimpleName());

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
        return new byte[0];
    }
}
