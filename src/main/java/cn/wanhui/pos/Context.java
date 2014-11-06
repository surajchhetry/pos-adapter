package cn.wanhui.pos;

import cn.wanhui.pos.util.Commons;
import cn.wanhui.pos.util.LoggerUtil;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOSource;
import org.jpos.util.Log;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author yinheli
 */
public class Context {

    private static final Log log = LoggerUtil.getLog(Bootstrap.class.getSimpleName());

    private static final ArrayList<String> mtiNeedMac = new ArrayList<String>(){{
        add("0110");
        add("0210");
        add("0230");
        add("0410");
        add("0850");
        add("0870");
    }};

    public ISOSource isoSource;
    public ISOMsg reqMsg;

    public Context(ISOSource isoSource, ISOMsg reqMsg) {
        this.isoSource = isoSource;
        this.reqMsg = reqMsg;
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

        if (msg.getString(39).equals("00") && mtiNeedMac.contains(msg.getMTI())) {
            msg.set(64, Commons.getMac(msg));
        }

        isoSource.send(msg);
    }
}
