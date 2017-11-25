package tech.qi.deepinfo.frame.module.spider;

import com.alibaba.fastjson.JSON;
import org.apache.commons.codec.digest.DigestUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import tech.qi.deepinfo.frame.core.Constants;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.scheduler.DuplicateRemovedScheduler;
import us.codecraft.webmagic.scheduler.MonitorableScheduler;
import us.codecraft.webmagic.scheduler.component.DuplicateRemover;


/**
 * @author wangqi
 */
public class RedisPriorityScheduler
        extends DuplicateRemovedScheduler implements MonitorableScheduler, DuplicateRemover {

    private JedisPool pool;
    /**
     * 优先级：默认0-2 数字越大优先级越高
     */
    private int PRIORITY_CAPACITY = 2;
    private String KEY_PREFIX;

    public RedisPriorityScheduler(String keyPrefix, JedisPool pool) {
        this.KEY_PREFIX = keyPrefix;
        this.pool = pool;
        setDuplicateRemover(this);
    }

    public RedisPriorityScheduler(String keyPrefix, JedisPool pool, int capacity) {
        this.pool = pool;
        this.KEY_PREFIX = keyPrefix;
        this.PRIORITY_CAPACITY = capacity;
        setDuplicateRemover(this);
    }

    @Override
    public void resetDuplicateCheck(Task task) {
        Jedis jedis = pool.getResource();
        try {
            jedis.del(getHistoryKey(task));
        } catch(Exception e) {
            logger.error("删除历史任务, 重置DuplicateCheck异常", e);
        } finally {
            pool.returnResource(jedis);
        }
    }

    @Override
    public boolean isDuplicate(Request request, Task task) {
        Jedis jedis = pool.getResource();
        try {
            boolean isDuplicate = jedis.sismember(getHistoryKey(task), request.getUrl());
            if (!isDuplicate) {
                jedis.sadd(getHistoryKey(task), request.getUrl());
            }
            return isDuplicate;
        } catch(Exception e) {
            logger.error("DuplicateCheck异常", e);
            return true;
        } finally {
            pool.returnResource(jedis);
        }

    }

    @Override
    protected void pushWhenNoDuplicate(Request request, Task task) {
        Jedis jedis = pool.getResource();
        try {
            jedis.rpush(getQueueKey(getPriority(request), task), request.getUrl());
            if (request.getExtras() != null) {
                String field = DigestUtils.shaHex(request.getUrl());
                String value = JSON.toJSONString(request);
                jedis.hset(getDetailKey(task), field, value);
            }
        } catch(Exception e) {
            logger.error("Push When No Duplicate 异常", e);
        } finally {
            pool.returnResource(jedis);
        }
    }

    @Override
    public synchronized Request poll(Task task) {
        Jedis jedis = pool.getResource();
        try {
            String url = null;
            for (int i = PRIORITY_CAPACITY; i >= 0; i--) {
                url = jedis.lpop(getQueueKey(i, task));
                if (url != null) {
                    break;
                }
            }
            if (url == null) {
                return null;
            }
            String key = getDetailKey(task);
            String field = DigestUtils.shaHex(url);
            byte[] bytes = jedis.hget(key.getBytes(), field.getBytes());
            if (bytes != null) {
                Request o = JSON.parseObject(new String(bytes), Request.class);
                return o;
            }
            Request request = new Request(url);
            return request;
        } catch(Exception e) {
            logger.error("获取爬虫任务异常", e);
            return null;
        } finally {
            pool.returnResource(jedis);
        }
    }

    protected  String getDetailKey( Task task ){
        return   Constants.SPIDER_SCHEDULER_TASK_DETAILS + KEY_PREFIX + Constants.CACHE_KEY_SEPARATOR + task.getUUID();
    }

    protected String getHistoryKey(Task task) {
        return Constants.SPIDER_SCHEDULER_HISTORY_URL + KEY_PREFIX + Constants.CACHE_KEY_SEPARATOR + task.getUUID();
    }

    protected String getQueueKey(int priority, Task task) {
        return Constants.SPIDER_SCHEDULER_TASKS + KEY_PREFIX + Constants.CACHE_KEY_SEPARATOR + priority + "_" + task.getUUID();
    }

    protected int getPriority(Request request) {
        int priority = (int) request.getPriority();
        if (priority > PRIORITY_CAPACITY) {
            priority = PRIORITY_CAPACITY;
        }
        return priority;
    }

    @Override
    public int getLeftRequestsCount(Task task) {
        Jedis jedis = pool.getResource();
        try {
            Long size = new Long(0);
            for (int i = PRIORITY_CAPACITY; i >= 0; i--) {
                Long prioritySize = jedis.llen(getQueueKey(i, task));
                size = size + prioritySize;
            }
            return size.intValue();
        } catch(Exception e) {
            logger.error("Get Left Requests Count 异常", e);
            return 0;
        } finally {
            pool.returnResource(jedis);
        }
    }

    @Override
    public int getTotalRequestsCount(Task task) {
        Jedis jedis = pool.getResource();
        try {
            Long size = jedis.scard(getHistoryKey(task));
            return size.intValue();
        } catch(Exception e) {
            logger.error("Get Total Requests Count 异常", e);
            return 0;
        } finally {
            pool.returnResource(jedis);
        }
    }
}
