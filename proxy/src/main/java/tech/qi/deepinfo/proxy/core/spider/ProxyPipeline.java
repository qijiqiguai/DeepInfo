package tech.qi.deepinfo.proxy.core.spider;


/**
 */
@Component
public class ProxyPipeline implements Pipeline {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    ProxyService proxyService;

    @Autowired
    RedisHandler redisHandler;

    @Override
    public void process(ResultItems resultItems, Task task) {
        SpiderJob spiderJob = new SpiderJob(resultItems.getRequest());
        logger.debug("Start ProxyPipeline Result: " + spiderJob.getBatchId());

        TaskType taskType = TaskType.valueOf(resultItems.getRequest().getExtra("TaskType").toString());
        switch (taskType) {
            case PROXY:
                saveToRedis(resultItems);
                break;
            default:
                logger.error("TaskType Unmatched Error!", JSON.toJSONString(resultItems.getRequest()) );
                break;
        }
    }

    private void saveToRedis(ResultItems resultItems){
        List<ProxyInfo> proxyInfoList = resultItems.get(Constants.SPIDER_PROXY);
        Jedis jedis = redisHandler.getJedis();
        try {
            for(ProxyInfo proxyInfo : proxyInfoList){
                jedis.rpush(Constants.PROXY_CHECK_JOB, JSON.toJSONString(proxyInfo));
            }
        } catch (Exception excep){
            logger.error( "Redis Store Pipeline Error!", excep );
        }finally {
            if( null != jedis ){
                redisHandler.recycleJedis(jedis);
            }
        }
    }
}
