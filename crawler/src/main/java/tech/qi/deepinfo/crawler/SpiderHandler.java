package tech.qi.deepinfo.crawler;

import javax.management.JMException;

/**
 */
@Component
public class SpiderHandler extends AbstractHandler{

    private static Logger logger = LoggerFactory.getLogger(SpiderHandler.class);

    @Value("${spider.enable.jmx.monitor}")
    private boolean enableJMX;

    @Value("${spider.threads}")
    private int threads;

    @Value("${spider.status.check.time}")
    private long statusCheckTime;

    @Value("${spider.liveInfo.delay.notify.time}")
    private long liveInfoDelayNotifyTime;

    @Value("${spider.env.reload.time}")
    private long envReloadTime;

    @Value("${sys.cache.liveInfo.days}")
    private int liveInfoCacheDays;

    private static Spider spider;

    private MonitorableScheduler spiderScheduler;

    @Autowired
    LivePipeline livePipeline;
    @Autowired
    LiveProcessor liveProcessor;
    @Autowired
    LiveDownloader liveDownloader;
    @Autowired
    PluginHandler pluginHandler;

    @Autowired
    AnchorService anchorService;
    @Autowired
    LiveInfoService liveInfoService;
    @Autowired
    RedisHandler redisHandler;

    public SpiderHandler() {
        super("SpiderHandler");
    }

    @Override
    public void init() {
        if (spider != null) {
            logger.error("spider 已经初始化，不能多次初始化");
            return;
        }

        redisHandler.cleanKeys(Constants.SCRAPPER_INIT_USELESS_KEYS);
        loadAndCacheAnchor();
        loadAndCacheLiveInfo();

        ThreadManager.startThread(
                SpiderJobLogStoreThread.class,
                SpiderStatusCheckThread.class,
                SpiderJobFailCheckThread.class,
                SpiderEnvReloadThread.class
        );

        logger.info("初始化SpiderManager......");
        List<Pipeline> pipelineList = Lists.newArrayList(livePipeline);

        JedisPool jedisPool = redisHandler.newJedisPool();
        spiderScheduler = new RedisPriorityScheduler("scrapper", jedisPool);
        spider = Spider.create(liveProcessor)
                .setUUID(redisHandler.getUID("scrapper_spider_id"))
                .setDownloader(liveDownloader)
                .setPipelines(pipelineList)
                .setScheduler(spiderScheduler)
                .setExitWhenComplete(false)
                .thread(threads);
        spider.setEmptySleepTime(3000);

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
        ThreadManager.stopThread(
                SpiderJobLogStoreThread.class, SpiderStatusCheckThread.class, SpiderJobFailCheckThread.class, SpiderEnvReloadThread.class
        );
    }

    @Override
    public void reload() {

    }

    private void loadAndCacheAnchor() {
        Jedis jedis = null;
        try {
            jedis = redisHandler.getJedis();
            // todo 由于主播信息很多，后期需要分页、时间段处理
            List<Anchor> anchorList = anchorService.selectAllSummary();

            if (anchorList == null) {
                logger.warn("数据库中没有任何Anchor数据");
            } else {
                for (Anchor anchor : anchorList) {
                    jedis.hset(Constants.SPIDER_ANCHOR_DETAIL_REFRESH_TIME, anchor.getId(), anchor.getId() + "#" + anchor.getLastModifiedTime().getTime());
                }
                logger.info("一共往redis中缓存了" + anchorList.size() + "条Anchor数据");
            }

        } catch (Exception excep) {
            logger.error("loadAndCacheAnchor 异常",excep);
        } finally {
            if (null != jedis) {
                redisHandler.recycleJedis(jedis);
            }
        }
    }

    private void loadAndCacheLiveInfo(){
        Jedis jedis = null;
        try {
            jedis = redisHandler.getJedis();
            //只会缓存指定时间内的缓存，因为某个liveInfo一般都在一天内
            long minTime = System.currentTimeMillis()-liveInfoCacheDays*24*3600*1000L;
            List<LiveInfo> liveInfoList = liveInfoService.selectSummaryByLastModifiedTime(minTime);
            if (liveInfoList == null) {
                logger.warn("数据库中没有任何liveInfoCacheDays>="+liveInfoCacheDays+"的LiveInfo数据");
            } else {
                //由于liveInfo一般不会超过一天，且数据量比较大，所以设置其超时时间，比如两天
                int expireSeconds = liveInfoCacheDays * 24 * 3600;
                for (LiveInfo liveInfo : liveInfoList) {
                    String key = Constants.SPIDER_LIVE_INFO_REFRESH_TIME + liveInfo.getId();
                    jedis.set(key, liveInfo.getId() + "#" + liveInfo.getLastModifiedTime().getTime());
                    jedis.expire(key, expireSeconds);
                }
                logger.info("一共往redis中缓存了" + liveInfoList.size() + "条 LiveInfo 数据");
            }

        } catch (Exception excep) {
            logger.error("loadAndCacheAnchor 异常",excep);
        } finally {
            if (null != jedis) {
                redisHandler.recycleJedis(jedis);
            }
        }
    }

// Getters & Setters
    public long getLiveInfoDelayNotifyTime() {
        return liveInfoDelayNotifyTime;
    }

    public void setLiveInfoDelayNotifyTime(long liveInfoDelayNotifyTime) {
        this.liveInfoDelayNotifyTime = liveInfoDelayNotifyTime;
    }

    public long getEnvReloadTime() {
        return this.envReloadTime;
    }

    public long getStatusCheckTime() {
        return statusCheckTime;
    }

    public void setStatusCheckTime(long statusCheckTime) {
        this.statusCheckTime = statusCheckTime;
    }

    public String getLastSpiderDownloadTime() {
        return liveDownloader.getLastSpiderDownloadTime();
    }
}
