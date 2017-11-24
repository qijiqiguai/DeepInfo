package tech.qi.deepinfo.frame.module.leader;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.Lifecycle;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisCluster;
import tech.qi.deepinfo.frame.context.Background;
import tech.qi.deepinfo.frame.support.Constants;

import java.util.function.Function;


/**
 *
 * @author wangqi
 * @date 2017/10/26 下午1:29
 */
@Component
public class ClusterLeader implements Lifecycle, Background {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedisKeySubscriber subscriber;
    @Autowired
    @Qualifier("jediscluster")
    private JedisCluster jedisCluster;

    private boolean running = false;
    private boolean leader = false;
    private long lastRunTime = 0;

    Function<String[], Object> acquireLeader = strings -> {
        int redisMsgCmdIndex = 2;
        // 注意 expire 命令 和 expired 事件 都会收到消息
        String cmd1 = "expired";
        String cmd2 = "del";
        // 在没有任何输入（表示非Redis事件调用）或者是 Key失效 事件发生，则去抢一下权限
        if( null==strings || strings[redisMsgCmdIndex].equals(cmd1) || strings[redisMsgCmdIndex].equals(cmd2)) {
            logger.debug( "Try Acquire Leader -> params:" + JSONObject.toJSONString(strings) + " isLeader:" + leader );
            long status = jedisCluster.setnx(Constants.LEADER_KEY, "true");
            if (status != 0) {
                logger.info( "Leader Acquired." );
                // 设置不长的过期时间，方式Leader突然异常退出，Redis可以定时将Leader权限交出来。
                jedisCluster.expire(Constants.LEADER_KEY, Constants.LEADER_KEY_TIMEOUT);
                leader = true;
            }
        }else {
            logger.debug( "Acquire Leader Called, But no try -> params:" +
                    JSONObject.toJSONString(strings) + " isLeader:" + leader );
        }
        return null;
    };

    public boolean isLeader(){
        return this.leader;
    }

    @Override
    public void start() {
        if(running) {
            return;
        }
        subscriber.addCommand(acquireLeader);
        subscriber.start();
        // 启动的时候就抢一下权限试试
        acquireLeader.apply(null);
        running = true;
    }

    @Override
    public void stop() {
        if (!running) {
            return;
        }
        if( leader ){
            jedisCluster.del(Constants.LEADER_KEY);
        }
        subscriber.stop();
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }


    /**
     * 将被全局的后台线程循环调用, 定时去争抢 Leader 权限。由于Redis的失效通知机制不一定靠谱，所以需要定期去轮询。
     * 全局后台线程轮询时间较短，所以在这里控制了一个时间间隔，防止过度频繁的争抢Redis资源。事实上采用订阅机制，就是为了降低轮询次数。
     * 根据文档，一个键失效是不保证一定触发消息的，而且集群不保证消息一定送达（比如当发出通知时网络异常了，集群是发出后不管的）。
     */
    @Override
    public void backgroundProcess() {
        if( leader ){
            // Leader持续持有权限，尽量保持一致性。
            jedisCluster.expire(Constants.LEADER_KEY, Constants.LEADER_KEY_TIMEOUT);
        }else {
            synchronized (acquireLeader) {
                acquireLeader.apply(null);
            }
        }
    }

    @Override
    public boolean leaderJob() {
        // ClusterLeader 本身的 backgroundProcess 是不需要 Leader 权限就可以执行的，就是大家一起去抢Leader权限。
        return false;
    }

    @Override
    public long getLastRunTime() {
        return this.lastRunTime;
    }

    @Override
    public void setLastRunTime(long time) {
        this.lastRunTime = time;
    }

    @Override
    public long getRunIntervalMs() {
        return Constants.LEADER_KEY_TIMEOUT;
    }
}
