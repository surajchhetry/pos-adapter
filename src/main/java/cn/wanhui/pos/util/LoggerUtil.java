package cn.wanhui.pos.util;

import org.jpos.util.Log;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;
import org.jpos.util.SimpleLogListener;

/**
 * @author yinheli
 */
public class LoggerUtil {
    public static Log getLog(String logName) {
        Logger logger = (Logger) NameRegistrar.getIfExists("logger.Q2");
        if (logger == null) {
            logger = new Logger();
            logger.addListener(new SimpleLogListener()) ;
        }
        return new Log(logger, logName);
    }
}
