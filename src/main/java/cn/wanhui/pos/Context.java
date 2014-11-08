package cn.wanhui.pos;

import cn.wanhui.pos.util.LoggerUtil;
import org.jpos.iso.ISODate;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOSource;
import org.jpos.util.Log;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

/**
 * @author yinheli
 */
public class Context {

    private static final Log log = LoggerUtil.getLog(Bootstrap.class.getSimpleName());

    private static final String[] mtiNeedMac = new String[]{
            "0110", "0210", "0230", "0410", "0850", "0870"
    };

    static {
        Arrays.sort(mtiNeedMac);
    }

    public final ISOSource isoSource;
    public final ISOMsg reqMsg;
    public final String apiBaseUrl;

    public Context(ISOSource isoSource,
                   ISOMsg reqMsg,
                   String apiBaseUrl) {
        this.isoSource = isoSource;
        this.reqMsg = reqMsg;
        this.apiBaseUrl = apiBaseUrl;
    }

    public void returnMsg(String code) throws IOException, ISOException {
        returnMsg(reqMsg, code);
    }

    public void returnMsg(ISOMsg msg, String code) throws ISOException, IOException {
        if (msg == null) {
            return;
        }
        msg.set(39, code);

        returnMsg(msg);
    }

    public void returnMsg(ISOMsg msg) throws ISOException, IOException {
        if (msg == null) {
            return;
        }

        if (msg.isRequest()) {
            msg.setResponseMTI();
        }

        msg.unset(new int[]{35,36,52});

        if (!msg.hasField(12)) {
            msg.set(12, ISODate.getTime(new Date()));
        }

        if (!msg.hasField(13)) {
            msg.set(13, ISODate.getDate(new Date()));
        }

        if (msg.getString(39).equals("00") && Arrays.binarySearch(mtiNeedMac, msg.getMTI()) >= 0) {
            // TODO  need mac?
            // msg.set(64, Commons.getMac(msg));
        }

        isoSource.send(msg);
    }
}
