package tech.qi.deepinfo.frame.context;

/**
 * @author wangqi
 * @date 2017/10/26 下午3:02
 */
public interface Background {

    /**
     * 在后台线程中轮转执行的实际任务
     */
    void backgroundProcess();

    /**
     * 是否是 集群Leader 专有的任务，比如更新某些全局信息的状态。
     */
    boolean leaderJob();

    /**
     * 上次执行时间
     * @return
     */
    long getLastRunTime();

    /**
     * 设置上次执行的时间
     * @param time
     */
    void setLastRunTime(long time);

    /**
     * 执行间隔
     * @return
     */
    long getRunIntervalMs();


    /**
     * 接口的默认方法，Since Java 1.8
     * 由于 BackgroundRunner 轮转时间比较短, 所以不是每次都需要执行当前对象的 process 方法。需要通过这个方法来校验一下本次轮转是否需要执行。
     * 可以被重写，支持自定义逻辑。
     * @return
     */
    default boolean runAtThisRound(){
        long currentTime = System.currentTimeMillis();
        if( (currentTime-getLastRunTime()) > getRunIntervalMs() ) {
            setLastRunTime(currentTime);
            return true;
        } else {
            return false;
        }
    }

}
