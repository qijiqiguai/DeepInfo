package tech.qi.deepinfo.frame.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 程序关闭监听的执行逻辑，需要定义BeanManager
 * 需要关闭时出来的bean实现Stoppable接口
 */
public class ShutdownHook extends Thread {
    private static Logger logger = LoggerFactory.getLogger(ShutdownHook.class);

    public ShutdownHook() {
        super("ShutdownHook");
    }

    @Override
    public void run() {
        try {
            logger.warn("收到ShutdownHook,开始关闭系统资源......");
            BeanManager.stopAll();
            ThreadManager.stopAll();
            logger.warn("关闭系统资源结束");
        } catch (Exception e) {
            logger.error("ShutdownHookThread run error",e);
        }
    }
}
