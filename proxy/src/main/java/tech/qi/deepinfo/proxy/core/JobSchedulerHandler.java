package tech.qi.deepinfo.proxy.core;



import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 */
@Component
public class JobSchedulerHandler extends AbstractHandler {

    private static Logger logger = LoggerFactory.getLogger(JobSchedulerHandler.class);

    @Value("${sys.proxy.platform}")
    private String platforms;

    @Value("${sys.proxy.reload.minutes}")
    private int reloadMinutes;

    private ScheduledExecutorService service;

    @Autowired
    ProxyService proxyService;

    @Autowired
    SpiderHandler spiderHandler;

    @Autowired
    RedisHandler redisHandler;

    public JobSchedulerHandler() {
        super("JobSchedulerHandler");
    }

    @Override
    public void init() {
        String[] platformList = platforms.split(",");
        Runnable runnable = new Runnable() {
            public void run() {
                String batchId = redisHandler.getUID("proxy_spider_job_id");
                for (int i = 0; i < platformList.length; i++) {
                    SpiderJob spiderJob = new SpiderJob(platformList[i], TaskType.PROXY, batchId);
                    spiderHandler.addRequest(spiderJob);
                    logger.info("新增 Proxy 任务, 平台:" + platformList[i]);
                }
            }
        };
        service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(runnable, 0, reloadMinutes, TimeUnit.MINUTES);
    }

    @Override
    public void reload() {

    }

    @Override
    public void stopMe() {
        if( null != service ) {
            service.shutdownNow();
        }
    }

}
