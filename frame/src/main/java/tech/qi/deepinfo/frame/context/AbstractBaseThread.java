package tech.qi.deepinfo.frame.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 线程基类
 */
public abstract class AbstractBaseThread extends Thread implements Stoppable {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private String threadId;
    private String threadName;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    private Date firstRunTime = null;
    private Date lastExecutedTime;
    private long successCount;
    private long failCount;

    //周期，毫秒数(小于0则为一次性执行)
    private long interval = 0;
    //对于周期性thread，是 先sleep再执行任务 还是 先执行任务再sleep
    private boolean sleepBeforeTask = false;


    /**
     * 默认情况下, ThreandName == ThreadID, 只有同一个类型的线程开很多个的时候, 才需要单独设置ID
     */
    public AbstractBaseThread(String threadName) {
        super(threadName);
        this.threadName = threadName;
        this.threadId = threadName;
    }

    public AbstractBaseThread(String threadName, String threadId) {
        super(threadName);
        this.threadName = threadName;
        this.threadId = threadId;
    }

    //具体业务方法，子类实现
    protected abstract void doTask();

    @Override
    public void run() {
        try {
            while (!closed.get()) {
                if( null == firstRunTime ){
                    firstRunTime = new Date();
                }

                if (sleepBeforeTask && interval>0) {
                    sleep(interval);
                }

                execute();

                if (!sleepBeforeTask && interval>0) {
                    sleep(interval);
                }

                if (interval < 0) { //一次性任务, 直接停掉
                    stopMe();
                }
            }
        } catch (Exception e) {
            logger.error("ConsumerThread error", e);
        }
    }

    /**
     * 执行业务逻辑, tong
     */
    private void execute(){
        try {
            doTask();
            lastExecutedTime = new Date();
            successCount++;
            if( successCount%10 == 0 ) {
                logger.info(  "Thread: " + threadName + "-" + threadId + " 执行了 " + successCount + "次");
            }
            logger.debug(threadName + " executed, threadId=" + threadId + " SuccessCount: " + successCount + " LastExecTime: " + lastExecutedTime);
        } catch (WakeupException e) { //From Kafka
            if (!closed.get()) {
                logger.warn("WakeupException at closed ", e);
            } else {
                logger.error("WakeupException", e);
            }
        } catch (Exception e) {
            failCount++;
            logger.error("AThread execute error, Thread: " + threadName + "-" + threadId, e);
        }
    }

    @Override
    public void stopMe() {
        logger.info("停止thread,name=" + this.getName() + ",threadId=" + this.getThreadId() + ",successCount=" + successCount + ",failCount=" + failCount);
        closed.set(true);
        ThreadManager.removeThread(this);
    }

    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("threadId", this.threadId);
        status.put("threadName", this.threadName);
        status.put("interval", this.interval);
        status.put("successCount", this.successCount);
        status.put("failCount", this.failCount);
        if( this.lastExecutedTime != null ) {
            status.put("lastExecutedTime", Util.sdf.format(this.lastExecutedTime) );
        }
        if( this.firstRunTime != null ){
            status.put("firstRunTime", Util.sdf.format(this.firstRunTime) );
        }
        return status;
    }
//Getters & Setters
    public String getThreadName() {
        return threadName;
    }

    public boolean isSleepBeforeTask() {
        return sleepBeforeTask;
    }

    public Date getLastExecutedTime() {
        return lastExecutedTime;
    }

    public long getFailCount() {
        return failCount;
    }

    public long getSuccessCount() {
        return successCount;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public void setSleepBeforeTask(boolean sleepBeforeTask) {
        this.sleepBeforeTask = sleepBeforeTask;
    }
}
