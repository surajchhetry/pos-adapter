package cn.wanhui.pos.ext.iso;

import org.jpos.iso.BCDInterpreter;
import org.jpos.iso.ISOStringFieldPackager;
import org.jpos.iso.LeftPadder;
import org.jpos.iso.NullPrefixer;

/**
 * @author yinheli
 */
public class IFB_NUMERIC extends ISOStringFieldPackager {
    public IFB_NUMERIC() {
        super(LeftPadder.ZERO_PADDER, BCDInterpreter.RIGHT_PADDED, NullPrefixer.INSTANCE);
    }
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public IFB_NUMERIC(int len, String description, boolean isLeftPadded) {
        super(len, description, LeftPadder.ZERO_PADDER,
                isLeftPadded ? BCDInterpreter.LEFT_PADDED : BCDInterpreter.RIGHT_PADDED,
                NullPrefixer.INSTANCE);
    }

    /** Must override ISOFieldPackager method to set the Interpreter correctly */
    public void setPad(boolean pad)
    {
        setInterpreter(pad ? BCDInterpreter.LEFT_PADDED : BCDInterpreter.RIGHT_PADDED);
        this.pad = pad;
    }
}
