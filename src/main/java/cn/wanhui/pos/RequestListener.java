package cn.wanhui.pos;

import cn.wanhui.pos.util.Commons;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.ISOSource;
import org.jpos.util.Log;
import org.jpos.util.LogSource;
import org.jpos.util.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author yinheli
 */
public class RequestListener implements ISORequestListener, Configurable, LogSource {

    private Log log;

    private ExecutorService handlerThreadPool = Executors.newCachedThreadPool(new NamedThreadFactory("request-handler-"));

    private Bootstrap bootstrap = new Bootstrap();
    private SqlSessionFactory sqlSessionFactory;

    private String apiBaseUrl;

    private static class NamedThreadFactory implements ThreadFactory {

        private AtomicLong threadNo = new AtomicLong(1);

        private final String  poolName;

        private NamedThreadFactory(String poolName) {
            this.poolName = poolName;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread newThread = new Thread(r, poolName + threadNo.getAndIncrement());
            newThread.setDaemon(false);
            if (newThread.getPriority() != Thread.NORM_PRIORITY) {
                newThread.setPriority(Thread.NORM_PRIORITY);
            }
            return newThread;
        }
    }

    public RequestListener() throws Exception {
        // init
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsStream("mybatis-config.xml"));
    }

    @Override
    public void setConfiguration(Configuration configuration) throws ConfigurationException {
        apiBaseUrl = configuration.get("apiBaseUrl");
    }

    @Override
    public boolean process(final ISOSource isoSource, final ISOMsg isoMsg) {
        handlerThreadPool.submit(new Runnable() {
            @Override
            public void run() {
                final Context ctx = new Context(sqlSessionFactory, isoSource, isoMsg, apiBaseUrl);
                try {
                    bootstrap.doTrx(ctx);
                } catch (Exception e) {
                    log.error(String.format("process trans exception, the follow is request msg and exception stack: \n\n%s", Commons.dumpISOMsg(isoMsg)), e);
                    try {
                        ctx.returnMsg("96");
                    } catch (Exception ignore) {
                    }
                }
            }
        });
        return true;
    }

    @Override
    public void setLogger(Logger logger, String realm) {
        log = new Log(logger, realm);
    }

    @Override
    public String getRealm() {
        return log.getRealm();
    }

    @Override
    public Logger getLogger() {
        return log.getLogger();
    }
}
