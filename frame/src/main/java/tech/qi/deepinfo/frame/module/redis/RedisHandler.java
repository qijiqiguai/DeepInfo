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

    /**
     * 非切片连接池
     */
    private JedisPool jedisPool;

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
        JedisPoolConfig config = new JedisPoolConfig();
        //连接耗尽时是否阻塞, false报异常,ture阻塞直到超时, 默认true
        config.setBlockWhenExhausted(true);
        //设置的逐出策略类名, 默认DefaultEvictionPolicy(当连接超过最大空闲时间,或连接数超过最大空闲连接数)
        config.setEvictionPolicyClassName("org.apache.commons.pool2.impl.DefaultEvictionPolicy");
        //是否启用pool的jmx管理功能, 默认true
        config.setJmxEnabled(true);
        //MBean ObjectName = new ObjectName("org.apache.commons.pool2:type=GenericObjectPool,name=" + "pool" + i); 默认为"pool", JMX不熟,具体不知道是干啥的...默认就好.
        config.setJmxNamePrefix("pool");
        //是否启用后进先出, 默认true
        config.setLifo(true);
        //最大空闲连接数, 默认8个
        config.setMaxIdle(8);
        //最大连接数, 默认8个
        config.setMaxTotal(8);
        //获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted),如果超时就抛异常, 小于零:阻塞不确定的时间,  默认-1
        config.setMaxWaitMillis(-1);
        //逐出连接的最小空闲时间 默认1800000毫秒(30分钟)
        config.setMinEvictableIdleTimeMillis(1800000);
        //最小空闲连接数, 默认0
        config.setMinIdle(0);
        //每次逐出检查时 逐出的最大数目 如果为负数就是 : 1/abs(n), 默认3
        config.setNumTestsPerEvictionRun(3);
        //对象空闲多久后逐出, 当空闲时间>该值 且 空闲连接>最大空闲数 时直接逐出,不再根据MinEvictableIdleTimeMillis判断  (默认逐出策略)
        config.setSoftMinEvictableIdleTimeMillis(1800000);
        //在获取连接的时候检查有效性, 默认false
        config.setTestOnBorrow(false);
        //在空闲时检查有效性, 默认false
        config.setTestWhileIdle(false);
        //逐出扫描的时间间隔(毫秒) 如果为负数,则不运行逐出线程, 默认-1
        config.setTimeBetweenEvictionRunsMillis(-1);
        jedisPool = new JedisPool(config, server, port, 3000);
        return jedisPool;
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

    private String getFullkey(String prefix, String key) {
        return prefix + ":" + key;
    }
}
