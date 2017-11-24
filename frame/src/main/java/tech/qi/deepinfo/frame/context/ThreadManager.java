package tech.qi.deepinfo.frame.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author wangqi
 */
public class ThreadManager {

    private static Logger logger = LoggerFactory.getLogger(ThreadManager.class);

    private static List<AbstractBaseThread> threadList = new CopyOnWriteArrayList();

    public static void addThread(AbstractBaseThread... threads) {
        int count = 0;
        for (AbstractBaseThread thread : threads) {
            threadList.add(thread);
            count++;
        }
        logger.info("一共初始化加入 Thread 个数: " + count );
    }

    public static void startThread(Class... clazzs) {
        for( Class c : clazzs ) {
            boolean foundThread = false;
            for (AbstractBaseThread o : threadList) {
                if( null!=o && o.getClass().equals(c)  ) {
                    foundThread = true;
                    if(!o.isAlive()) {
                        o.start();
                        logger.info( "启动线程: " + c.getSimpleName() );
                    }else {
                        logger.warn("线程已经启动, 无需重复启动, Thread: " + o.getThreadName() + "-" + o.getThreadId());
                    }
                }
            }
            if( !foundThread ) {
                logger.error( "无法启动线程: " + c.getSimpleName() + ", 该线程无法找到" );
            }
        }
    }

    public static void stopThread(Class... clazzs) {
        for( Class c : clazzs ) {
            threadList.stream().forEach( o -> {
                if( null!=o && o.getClass().equals(c) ) {
                    o.stopMe();
                }
            });
        }
    }

    public synchronized static void removeThread(AbstractBaseThread thread) {
        try {
            if (threadList.contains(thread)) {
                threadList.remove(thread);
                logger.debug("删除一个thread,name=" + thread.getName() + ",threadId=" + thread.getThreadId());
            } else {
                logger.warn("未找到相关thread,name=" + thread.getName() + ",threadId=" + thread.getThreadId());
            }
        } catch (Exception e) {
            logger.error("removeThread error", e);
        }
    }

    public static AbstractBaseThread findById(String threadId){
        for(AbstractBaseThread thread : threadList){
            if(thread.getThreadId().equals(threadId)){
                return thread;
            }
        }
        return null;
    }

    /**
     * @param clazz
     * @return
     * 有时候一个 Thread 类型可能新建了多个实例, 这个方法只能保证找到第一个
     * 当然, 如果只有一个实例, 那么显然就返回这唯一一个.
     * 定义这个方法的好处在于, 可以通过类引用直接找到 start 的地方.
     */
    public static AbstractBaseThread findFirstByClazz(Class clazz){
        for(AbstractBaseThread thread : threadList){
            if(thread.getClass().equals(clazz)) {
                return thread;
            }
        }
        return null;
    }

    public static List<AbstractBaseThread> findByClazz(Class clazz){
        List<AbstractBaseThread> res =
                threadList.stream().filter(thread -> thread.getClass().equals(clazz)).collect(Collectors.toList());
        return res;
    }

    public static void stopAll(){
        for(AbstractBaseThread thread : threadList){
            thread.stopMe();
        }
    }

    public static List<Map<String, Object>> getAllThreadStatus() {
        List<Map<String, Object>> statusList = new ArrayList<>();
        threadList.stream().forEach( o -> statusList.add( o.getStatus() ));
        return statusList;
    }

    public static Map<String, Object> getThreadStatus(String threadId) {
        for(AbstractBaseThread thread : threadList){
            if(thread.getThreadId().equals(threadId)){
                return thread.getStatus();
            }
        }
        return null;
    }
}
