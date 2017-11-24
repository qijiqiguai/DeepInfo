package tech.qi.deepinfo.frame.context;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.qi.deepinfo.frame.module.leader.ClusterLeader;
import tech.qi.deepinfo.frame.support.Constants;
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

    ClusterLeader clusterLeader;
    private ExecutorService timer;
    private ExecutorService runner;
    private Set<Background> backgrounds;
    private boolean running;

    @Autowired
    public BackgroundRunner( ClusterLeader clusterLeader ){
        // 确保写入和使用是相互不影响的
        backgrounds = new CopyOnWriteArraySet<>();
        timer = ThreadUtil.singleThread("Background-Runner-Timer");
        runner = ThreadUtil.fixedThreadPool(5, "Background-Runner");
        this.clusterLeader = clusterLeader;
        addBackground(clusterLeader);
        clusterLeader.start();
    }

    public void addBackground(Background background) {
        if( null != background ){
            backgrounds.add(background);
        }
    }

    @Override
    public void init() throws LifecycleException {

    }

    @Override
    public void start() {
        if( running ){
            return;
        }
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
        running =  true;
    }

    @Override
    public void stop() {
        if( !running ){
            return;
        }
        running =  false;
        timer.shutdown();
    }


    @Override
    public void addLifecycleListener(LifecycleListener lifecycleListener) {

    }

    @Override
    public LifecycleListener[] findLifecycleListeners() {
        return new LifecycleListener[0];
    }

    @Override
    public void removeLifecycleListener(LifecycleListener lifecycleListener) {

    }

    @Override
    public void destroy() throws LifecycleException {

    }

    @Override
    public LifecycleState getState() {
        return null;
    }

    @Override
    public String getStateName() {
        return null;
    }
}
