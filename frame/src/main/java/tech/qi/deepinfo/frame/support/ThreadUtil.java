package tech.qi.deepinfo.frame.support;

import java.util.concurrent.*;

/**
 *
 * @author wangqi
 * @date 2017/10/26 下午3:42
 */
public class ThreadUtil {

    public static ExecutorService fixedThreadPool(int num, String name) {
        class JobRunnerTf implements ThreadFactory {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, name);
            }
        }
        return Executors.newFixedThreadPool(num, new JobRunnerTf());
    }


    public static ExecutorService singleThread( String name ){
        ThreadFactory namedThreadFactory = r -> {
            Thread thread = new Thread( r );
            thread.setName(name);
            return thread;
        };

        ExecutorService singleThreadPool = new ThreadPoolExecutor(
            1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingDeque<>(1024), namedThreadFactory,
            new ThreadPoolExecutor.AbortPolicy()
        );
        return singleThreadPool;
    }
}
