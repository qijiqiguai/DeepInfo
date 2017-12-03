package tech.qi.deepinfo.proxy.core;


import java.util.ArrayList;
import java.util.List;


/**
 */
@Component
public class SpiderHandler extends AbstractHandler {

    private static Logger logger = LoggerFactory.getLogger(SpiderHandler.class);

    private static Spider proxySpider;

    private MonitorableScheduler spiderScheduler;

    @Autowired
    ProxyPipeline proxyPipeline;
    @Autowired
    ProxyProcessor proxyProcessor;
    @Autowired
    ProxyDownloader proxyDownloader;

    @Value("${spider.threads}")
    private int threads;

    @Autowired
    RedisHandler redisHandler;

    public SpiderHandler() {
        super("ProxyHandler");
    }

    @Override
    public void init() {
        if (proxySpider != null) {
            logger.error("proxySpider 已经初始化，不能多次初始化");
            return;
        }

        logger.info("初始化ProxySpiderHandler......");
        redisHandler.cleanKeys(Constants.PROXY_INIT_USELESS_KEYS);

        List<Pipeline> pipelineList = new ArrayList<>();
        pipelineList.add(proxyPipeline);

        JedisPool jedisPool = redisHandler.getPool();
        spiderScheduler = new RedisPriorityScheduler("proxy", jedisPool);
        proxySpider = Spider.create(proxyProcessor)
                .setUUID(redisHandler.getUID("proxy_spider_id"))
                .setDownloader(proxyDownloader)
                .setPipelines(pipelineList)
                .setScheduler(spiderScheduler)
                .setExitWhenComplete(false)
                .thread(threads);
        proxySpider.setEmptySleepTime(3000);

        logger.info("启动proxySpider...");
        proxySpider.start();
        logger.info("启动proxySpider 结束");
    }

    @Override
    public void reload() {

    }

    @Override
    public void stopMe() {
        if( null != proxySpider ) {
            proxySpider.stop();
        }
    }

    public void addRequest(Request... requests) {
        proxySpider.addRequest(requests);
    }
}
