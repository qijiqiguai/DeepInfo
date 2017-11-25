package tech.qi.deepinfo.frame.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.qi.deepinfo.frame.core.Background;
import tech.qi.deepinfo.frame.core.Lifecycle;
import tech.qi.deepinfo.frame.core.LifecycleException;
import tech.qi.deepinfo.frame.module.leader.ClusterLeader;
import tech.qi.deepinfo.frame.core.Constants;
import tech.qi.deepinfo.frame.support.ThreadUtil;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;

/**
 *
 * @author wangqi
 * @date 2017/10/26 下午3:03
 *
 */
@Component
public class BackgroundRunner implements Lifecycle {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String name = "BackgroundRunner";
    private Lifecycle.State state;

    ClusterLeader clusterLeader;
    private ExecutorService timer;
    private ExecutorService runner;
    private Set<Background> backgrounds;

    @Autowired
    public BackgroundRunner( ClusterLeader clusterLeader ){
        // 确保写入和使用是相互不影响的
        backgrounds = new CopyOnWriteArraySet<>();
        timer = ThreadUtil.singleThread("Background-Runner-Timer");
        runner = ThreadUtil.fixedThreadPool(5, "Background-Runner");
        this.clusterLeader = clusterLeader;
        addBackground(clusterLeader);
        clusterLeader.start();
        state = State.NEW;
    }

    public void addBackground(Background background) {
        if( null != background ){
            backgrounds.add(background);
        }
    }

    @Override
    public void init() {
        synchronized (this.state) {
            this.state = State.INITIALIZING;
            // Empty
            this.state = State.INITIALIZED;
        }
    }

    @Override
    public void start() {
        synchronized (this.state) {
            this.state = State.STARTING;

            // 提交一个轮转任务，持续调用后台任务列表。
            timer.execute(() -> {
                boolean run = true;
                while (!Thread.currentThread().isInterrupted() && run) {
                    logger.debug("BackgroundRunner is running");
                    try {
                        // 调用每一个后台任务的 backgroundProcess 方法。执行之前需要校验本次是否执行。
                        if( null!=backgrounds && backgrounds.size()>0 ){
                            backgrounds.forEach( o -> {
                                try {
                                    // 非Leader任务，或者是Leader任务且当前就是Leader的情况下为 OK
                                    boolean conditionOk = !o.leaderJob() || ( o.leaderJob() && clusterLeader.isLeader() );
                                    if( o.runAtThisRound() && conditionOk){
                                        logger.debug("BackgroundRunner ConditionOK, Execute:" + o.getClass().getSimpleName());
                                        // 提交到Runner去异步执行，而不是阻塞定时线程
                                        runner.submit(() -> o.backgroundProcess());
                                    }
                                } catch (Exception e) {
                                    logger.warn( o.getClass().getSimpleName() + " Background Job Run Fail.", e );
                                }
                            } );
                        }else {
                            logger.debug( "BackgroundRunner have no job todo" );
                        }
                        Thread.sleep(Constants.BACKGROUND_RUN_INTERVAL);
                    } catch (InterruptedException e) {
                        logger.warn( "BackgroundRunner Sleep Interrupted", e );
                        run = false;
                    } catch (Exception e) {
                        logger.warn( "BackgroundRunner Unexpected Exception", e );
                    }
                }
            });

            this.state = State.STARTED;
        }
    }

    @Override
    public void stopMe() throws LifecycleException {
        if( this.state==State.STOPPED || this.state==State.STOPPING ){
            return;
        }
        synchronized (this.state) {
            this.state = State.STOPPING;
            timer.shutdown();
            this.state = State.STOPPED;
        }
    }

    @Override
    public void destroy() throws LifecycleException {
        synchronized (this.state) {
            this.state = State.DESTROYING;
            // Empty
            this.state = State.DESTROYED;
        }
    }

    @Override
    public State getState() {
        return this.state;
    }

    @Override
    public String getStateName() {
        return this.name;
    }
}
