package tech.qi.deepinfo.frame.module.leader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.Lifecycle;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPubSub;
import tech.qi.deepinfo.frame.support.Constants;
import tech.qi.deepinfo.frame.support.ThreadUtil;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 *
 * @author wangqi
 * @date 2017/10/13 下午1:25
 */
@Component
public class RedisKeySubscriber implements Lifecycle {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String CHANNEL_PREFIX = "__keyspace@*__:";
    private boolean running = false;
    private List<Function<String[], Object>> commands;

    @Autowired
    @Qualifier("jediscluster")
    private JedisCluster jedisCluster;
    private ExecutorService subscriber;

    public RedisKeySubscriber(){
        commands = new CopyOnWriteArrayList<>();
        subscriber = ThreadUtil.singleThread("Redis-Key-Subscriber");
    }

    public void addCommand(Function<String[], Object> command) {
        this.commands.add(command);
    }

    @Override
    public void start() {
        if( running ){
            return;
        }
        Assert.isTrue( commands!=null && commands.size()>0, " Subscriber should have commands! " );
        subscriber.submit(() -> {
            String keyEventChannel = CHANNEL_PREFIX + Constants.LEADER_KEY;
            logger.info( "Start subscribe for Key:" + keyEventChannel );
            // 这是一个阻塞方法，会导致执行到这里的线程堵住，后续所有操作无法执行。所以需要一个单独线程来做这个事情。
            RedisKeyEventListener listener = new RedisKeyEventListener(commands);
            jedisCluster.psubscribe(listener, keyEventChannel);
            // TODO, 阻塞的线程会被正常关闭么？
        });
    }

    @Override
    public void stop() {
        if( !running ){
            return;
        }
        running = false;
        subscriber.shutdown();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    private class RedisKeyEventListener extends JedisPubSub {
        private List<Function<String[], Object>> commands;

        public RedisKeyEventListener(List<Function<String[], Object>> commands) {
            this.commands = commands;
        }

        @Override
        public void onPMessage(String pattern, String channel, String message) {
            logger.debug( "OnPMessage -> pattern:" + pattern + " channel:" + channel + " message:" + message );
            if(commands!=null && commands.size()>0){
                IntStream.range(0, commands.size()).forEach( i ->{
                    String[] params = new String[]{ pattern, channel, message };
                    synchronized (commands.get(i)) {
                        commands.get(i).apply( params );
                    }
                });
            }
        }
    }
}
