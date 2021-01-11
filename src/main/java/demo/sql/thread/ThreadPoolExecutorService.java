package demo.sql.thread;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

/**
 * 本地线程池
 */
@Slf4j
@Component
public class ThreadPoolExecutorService {

    private static ThreadLocal<BlockingQueue<Runnable>> blockingQueueThreadLocal = new ThreadLocal<>();
    private static ThreadLocal<ThreadPoolExecutor> threadPoolExecutorThreadLocal = new ThreadLocal<>();

    public static void clear() {
        blockingQueueThreadLocal.remove();
        threadPoolExecutorThreadLocal.remove();
    }

    public static synchronized void addWork(Runnable runnable) {
        BlockingQueue<Runnable> workQueue = blockingQueueThreadLocal.get();
        ThreadPoolExecutor threadPoolExecutor = threadPoolExecutorThreadLocal.get();
        RejectedExecutionHandler callerRunsPolicy = new ThreadPoolExecutor.CallerRunsPolicy();
        if (null == workQueue) {
            /**
             * 如果队列为null -> 新建一个
             */
            workQueue = new ArrayBlockingQueue<Runnable>(100000);
            blockingQueueThreadLocal.set(workQueue);
        }

        if (threadPoolExecutor == null) {
            int corePoolSize = 100;
            int maximumPoolSize = 200;
            long keepAliveTime = 5L;
            TimeUnit unit = TimeUnit.MINUTES;
            threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, callerRunsPolicy);
            threadPoolExecutorThreadLocal.set(threadPoolExecutor);
        }
        threadPoolExecutor.submit(runnable);
    }

    /**
     * 自旋 直至完成
     *
     * @return
     * @throws Exception
     */
    public static synchronized boolean waitComplete() throws Exception {
        ThreadPoolExecutor threadPoolExecutor = threadPoolExecutorThreadLocal.get();
        if (threadPoolExecutor == null) {
            throw new Exception("线程池为空，请初始化,可能未执行addWork方法");
        }
        for (; ; ) {
            log.info("当前剩余作业:{}", blockingQueueThreadLocal.get().size());
            log.info("当前活跃作业:{}", threadPoolExecutor.getActiveCount());
            Thread.sleep(1000);
            if (threadPoolExecutor.getActiveCount() == 0) {
                return true;
            }
        }
    }

    /**
     * 自旋 直至完成
     *
     * @return
     * @throws Exception
     */
    public static synchronized boolean isComplete() throws Exception {
        ThreadPoolExecutor threadPoolExecutor = threadPoolExecutorThreadLocal.get();
        if (threadPoolExecutor == null) {
            log.info("线程池为空，请初始化,可能未执行addWork方法");
            return false;
        }
        return threadPoolExecutor.getActiveCount() == 0;
    }

}
