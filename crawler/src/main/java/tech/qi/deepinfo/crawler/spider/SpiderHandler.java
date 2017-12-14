package tech.qi.deepinfo.crawler.spider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPool;
import tech.qi.deepinfo.frame.core.AbstractHandler;
import tech.qi.deepinfo.frame.core.LifecycleException;
import tech.qi.deepinfo.frame.module.redis.RedisHandler;
import tech.qi.deepinfo.frame.module.spider.BatchTaskEntry;
import tech.qi.deepinfo.frame.module.spider.RedisPriorityScheduler;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.monitor.SpiderMonitor;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.scheduler.MonitorableScheduler;

import javax.management.JMException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author wangqi
 */
@Component
public class SpiderHandler extends AbstractHandler {

    private static Logger logger = LoggerFactory.getLogger(SpiderHandler.class);

    @Value("${spider.enable.jmx.monitor}")
    private boolean enableJMX;

    @Value("${spider.threads}")
    private int threads;

    @Value("${spider.status.check.time.ms}")
    private long statusCheckTime;

    @Value("${spider.env.reload.time.ms}")
    private long envReloadTime;

    @Autowired
    RedisHandler redisHandler;

    @Autowired
    WebProcessor webProcessor;

    @Autowired
    PluginBasedDownloader webDownloader;

    @Autowired
    Pipeline pipeline;

    private static Spider spider;
    private MonitorableScheduler spiderScheduler;

    public SpiderHandler() {
        super("SpiderHandler");
    }

    @Override
    public void init() {
        if (spider != null) {
            logger.error("spider 已经初始化，不能多次初始化");
            return;
        }

//        ThreadManager.startThread(
//                SpiderJobLogStoreThread.class,
//                SpiderStatusCheckThread.class,
//                SpiderJobFailCheckThread.class,
//                SpiderEnvReloadThread.class
//        );

        logger.info("初始化SpiderManager......");
        List<Pipeline> pipelineList = new ArrayList<>(1);
        pipelineList.add(pipeline);

        JedisPool jedisPool = redisHandler.newJedisPool();
        spiderScheduler = new RedisPriorityScheduler("scrapper", jedisPool);
        spider = Spider.create(webProcessor)
                .setUUID(redisHandler.getUID("spider"))
                .setDownloader(webDownloader)
                .setPipelines(pipelineList)
                .setScheduler(spiderScheduler)
                .setExitWhenComplete(false)
                .thread(threads);
        spider.setEmptySleepTime(3000);

        BatchTaskEntry test = new BatchTaskEntry("xueqiu", "people", UUID.randomUUID().toString());
        test.setUrl("https://xueqiu.com/people");
        this.addRequest(test);
    }

    @Override
    public void start() throws LifecycleException {
        logger.info("启动Spider...");
        spider.start();
        logger.info("启动Spider 结束");

        logger.info("开启SpiderManager监控...");
        startJMXMonitor();
        logger.info("开启SpiderManager监控结束");

        logger.info("初始化SpiderManager 结束");
    }

    private void startJMXMonitor(){
        if(enableJMX){
            try {
                SpiderMonitor.instance().register(spider);
            } catch (JMException e) {
                logger.warn("Add JMX Monitor Error", e);
            }
        } else {
            logger.warn("No JMX Monitor By Config");
        }
    }

    public void addRequest(Request... requests) {
        spider.addRequest(requests);
    }

    public Spider getSpider() {
        return spider;
    }

    /**
     * 是否在运行
     * @return
     */
    public boolean isRunning(){
        if(spider==null){
            return false;
        }else{
            if(spider.getStatus() == Spider.Status.Running){
                return true;
            }else{
                return false;
            }
        }
    }

    @Override
    public void stopMe() {
        if( null != spider ){
            spider.stop();
        }
//        ThreadManager.stopThread(
//                SpiderJobLogStoreThread.class,
//                SpiderStatusCheckThread.class,
//                SpiderJobFailCheckThread.class,
//                SpiderEnvReloadThread.class
//        );
    }

    @Override
    public void destroy() throws LifecycleException {

    }

    @Override
    public Status getStatus() {
        return null;
    }

    @Override
    public void reload() {

    }
}
