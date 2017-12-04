package tech.qi.deepinfo.frame.module.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;
import tech.qi.deepinfo.frame.core.AbstractHandler;
import tech.qi.deepinfo.frame.core.Constants;
import tech.qi.deepinfo.frame.core.LifecycleException;

import java.text.DecimalFormat;
import java.util.Set;

/**
 *
 * @author wangqi
 */
@Component
public class RedisHandler extends AbstractHandler {

    private static Logger logger = LoggerFactory.getLogger(RedisHandler.class);

    @Value("${redis.server.ip}")
    private String server;

    @Value("${redis.server.port}")
    private int port;

    @Value("${redis.default.db}")
    private int database;

    @Value("${redis.password}")
    private String password;

    @Value("${redis.namespace}")
    private String namespace;

    private JedisPool jedisPool;//非切片连接池

    public RedisHandler() {
        super("RedisHandler");
    }

    @Override
    public void init() {
        logger.info(this.getName() + " begin init...");
        jedisPool = newJedisPool();
        logger.info(this.getName() + " init finished,server: " + server);
    }

    @Override
    public void start() throws LifecycleException {

    }

    public JedisPool newJedisPool() {
        // 池基本配置
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(5);
        config.setTestOnBorrow(true);
        return new JedisPool(config, server, port, Protocol.DEFAULT_TIMEOUT, password);
    }

    @Override
    public void reload() {

    }

    /**
     * 用于清理特定的 Key, 通常在启动的时候有用, 用于清理上次运行残余的不需要的 Key
     * @param keyPatterns
     */
    public void cleanKeys( String keyPatterns ) {
        Jedis jedis = null;
        try {
            if(keyPatterns==null || keyPatterns.isEmpty()){
                logger.info("没有需要清理的 Redis Key");
            }else{
                jedis = getJedis();
                String[] patterns = keyPatterns.split(",");
                int n=0;
                for(String p : patterns){
                    Set<String> keys = jedis.keys(p);
                    if(keys!=null && !keys.isEmpty()){
                        for(String key : keys){
                            jedis.del(key);
                            logger.debug("Clean Redis Key:" + key);
                            n++;
                        }
                    }else{
                        logger.warn("没有找到 Pattern = " + p + "的 Key");
                    }
                }
                logger.info("共清理" + n + "条 Redis Key");
            }
        } catch (Exception e) {
            logger.error("Clean Redis Key 异常", e);
        } finally {
            if (null != jedis) {
                recycleJedis(jedis);
            }
        }
    }

    @Override
    public void stopMe() {
        logger.debug(this.getName() + " begin stop...");
        if (this.jedisPool != null) {
            jedisPool.destroy();
        }
    }

    @Override
    public void destroy() throws LifecycleException {

    }

    @Override
    public Status getStatus() {
        return null;
    }

    @Override
    public String getStateName() {
        return null;
    }

    public Jedis getJedis() {
        Jedis jedis = jedisPool.getResource();
        jedis.select(database);
        return jedis;
    }

    public JedisPool getPool() {
        return jedisPool;
    }

    public void recycleJedis(Jedis jedis) {
        if (null != jedis) {
            // 已经考虑到连接 Broken 的情况了
            jedis.close();
        }
    }

    public String hget(final String key, final String field) {
        Jedis jedis = getJedis();
        try {
            return jedis.hget(key, field);
        } catch (Exception e) {
            logger.error("hget 异常,key:"+key, e);
            return null;
        } finally {
            recycleJedis(jedis);
        }
    }

    public String get(final String key) {
        Jedis jedis = getJedis();
        try {
            return jedis.get(key);
        } catch (Exception e) {
            logger.error("hget 异常,key:"+key, e);
            return null;
        } finally {
            recycleJedis(jedis);
        }
    }

    public String getUID(final String key){
        Jedis jedis = getJedis();
        try {
            String prefixedKey = Constants.UID + key;
            long value = jedis.incr(prefixedKey);
            DecimalFormat fmt = new DecimalFormat();
            fmt.applyPattern("0000");
            return System.currentTimeMillis() + fmt.format(value);
        } catch (Exception e) {
            logger.error("batchDelWithPrefix 异常", e);
        } finally {
            recycleJedis(jedis);
        }
        return null;
    }

    private String getFullkey(String key) {
        return namespace + ":" + key;
    }
}
