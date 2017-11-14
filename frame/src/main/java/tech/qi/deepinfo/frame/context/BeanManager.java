package tech.qi.deepinfo.frame.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;


public class BeanManager {
    private static Logger logger = LoggerFactory.getLogger(BeanManager.class);
    private static ApplicationContext applicationContext;


    public static synchronized void setApplicationContext(ApplicationContext applicationContext) {
        if (BeanManager.applicationContext != null) {
            logger.warn("ApplicationContext 已经被初始化");
        } else {
            BeanManager.applicationContext = applicationContext;
        }
    }

    public static <T> T getBean(Class<T> cls) throws BeansException {
        return applicationContext.getBean(cls);
    }

    public static <T> List<T> getBeanList(Class<T> cls) throws BeansException {
        return applicationContext.getBeansOfType(cls).values().stream().collect(Collectors.toList());
    }

    /**
     * 执行初始化, 一般放在 Main 中统一进行, 这样可以从代码中明确看到那些是系统强制依赖的, 比 Spring 的 PostConstructor 要直观.
     * 执行的过程是按照 BeanClass 的排序顺序执行的
     *
     * @param beanClass
     */
    public static void initBeans(Class... beanClass) {
        int count = 0;
        for (Class clazz : beanClass) {
            Initable obj;
            try {
                obj = (Initable) getBean(clazz);
            } catch (Exception e) {
                logger.error(clazz.getSimpleName() + " 不是 Initable 类型的", e);
                continue;
            }
            if (null != obj) {
                count++;
                obj.init();
                logger.debug(clazz.getSimpleName() + " 初始化成功");
            } else {
                logger.warn(clazz.getSimpleName() + " 无法找到, 可能是该Bean未实例化");
            }
        }
        logger.info("一共初始化bean个数: " + count + " 实际需要初始化 Bean个数: " + beanClass.length);
    }

    /**
     * 仅包含了 Spring 管理的那些 Bean 的关闭, 并不包括独立线程的关闭.
     */
    public static void stopAll() {
        List<Stoppable> stoppableList = getBeanList(Stoppable.class);
        if (stoppableList == null || stoppableList.isEmpty()) {
            logger.warn("系统中没有任何的 StoppableBean");
        } else {
            for (Stoppable bean : stoppableList) {
                logger.info("Begin to stop bean: " + bean);
                bean.stopMe();
            }
            logger.info("一共关闭bean个数:" + stoppableList.size());
        }
    }

    /**
     * @param className 包含package
     * @return
     */
    public static AbstractHandler getHandler(String className) {
        List<AbstractHandler> handlerList = getBeanList(AbstractHandler.class);
        if (handlerList == null || handlerList.isEmpty()) {
            logger.warn("系统中没有任何 HandlerBean");
            return null;
        } else {
            for (AbstractHandler ah : handlerList) {
                if (ah.getClass().getName().equals(className)) {
                    return ah;
                }
            }
            logger.warn("未找到className=" + className + "的Handler，可能是该Bean未实例化");
            return null;
        }
    }

}
