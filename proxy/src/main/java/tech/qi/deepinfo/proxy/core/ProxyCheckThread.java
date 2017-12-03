package tech.qi.deepinfo.proxy.core;

import com.alibaba.fastjson.JSON;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

/**
 */
public class ProxyCheckThread extends AbstractBaseThread {
    private static Logger logger = LoggerFactory.getLogger(ProxyCheckThread.class);

    private int CHECK_TIMEOUT = 500;
    private String CHECK_WEBSITE = "http://www.baidu.com";

    private static volatile String lastBatchId; // 在线程之间是共享的, 并且修改之后马上就要同步掉

    RedisHandler redisHandler;
    ProxyService proxyService;
    Jedis jedis;

    public ProxyCheckThread(int index) {
        super("ProxyCheckThread[" + index + "]");
        redisHandler = BeanManager.getBean(RedisHandler.class);
        proxyService = BeanManager.getBean(ProxyService.class);
        // 这个线程是滚动执行的, 不是间隔定时执行, 所以不需要设置 Interval. 需要暂停的时候会在 DoTask 内部依照执行情况来 Sleep
    }

    @Override
    protected void doTask() {
        if (jedis != null) {
            try {
                String string = jedis.lpop(Constants.PROXY_CHECK_JOB);
                if (string != null) {
                    ProxyInfo proxyInfo = JSON.parseObject(string, ProxyInfo.class);
                    if (!isConnectionTimeOut(proxyInfo)) {
                        flushToDB(proxyInfo);
                    }
                } else { //如果队列中没有日志, 则暂停十秒再去拿
                    try {
                        redisHandler.recycleJedis(jedis); //暂停的时候才释放连接, 否则不停的释放然后请求
                        jedis = null;
                        logger.info("没有待存储的 待验证Proxy，" + this.getThreadName() + " 暂停, 并释放 Jedis 资源");
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        logger.warn("ProxyCheckThread 暂停失败, 遇到系统中断");
                        Thread.currentThread().interrupt();
                    }
                }
            } catch (Exception e) {
                logger.warn("无法运行 ProxyCheckThread", e);
            }
        } else {
            jedis = redisHandler.getJedis();
        }
    }

    /**
     * 每校验通过一个 Proxy 就往 DB插入一次, 因为 Proxy 不是特别多, 而且表也不大, 所以这样操作不会产生很大的性能问题
     * 同时, 在插入之前会判断一下, 如果 batchID 发生了变化, 则直接清空原来的Proxy, 即删掉上一批次的 Proxy
     * @param proxyInfo
     */
    public synchronized void flushToDB(ProxyInfo proxyInfo) {
        if ( (lastBatchId == null) ||
                (lastBatchId != null && !lastBatchId.equals(proxyInfo.getBatchId()))
        ) { //如果本次的 BatchID 和 lastBatchId 不同, 或者lastBatchId是空(第一次运行), 证明上一批次Proxy的已经没用了
            proxyService.cleanProxyInfo();
            logger.info("清除现有的 Proxy, 开始插入新的一批 Proxy");
        }

        lastBatchId = proxyInfo.getBatchId();
        proxyService.saveProxyInfo(proxyInfo);
        proxyService.saveProxyConfig(proxyInfo);
        logger.info( "写 ProxyInfo 到 DB" );
    }

    public boolean isConnectionTimeOut(ProxyInfo proxyBean) {
        int statusCode;
        try {
            HttpClient httpClient = new HttpClient();
            httpClient.getHostConfiguration().setProxy(proxyBean.getIp(), proxyBean.getPort());

            httpClient.getHttpConnectionManager().getParams().setSoTimeout(CHECK_TIMEOUT);
            httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(CHECK_TIMEOUT);

            HttpMethod method = new GetMethod(CHECK_WEBSITE);

            statusCode = httpClient.executeMethod(method);
        } catch (Exception e) {
            return true;
        }
        if (200 == statusCode) {
            return false;
        }
        return true;
    }
}
