package com.uzykj.sms.module.smpp.queue;

import com.uzykj.sms.core.service.SmsDetailsService;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author ghostxbh
 * @date 2020/7/5
 * @description
 */
public class SmsSendThreadPool {
    private static Logger log = Logger.getLogger(SmsDetailsService.class.getName());
    private static final int DEFAULT_MAX_CONCURRENT = 15;

    private static final String THREAD_POOL_NAME = "XmlSendThreadPool-%d";

    private static final ThreadFactory FACTORY = new BasicThreadFactory.Builder().namingPattern(THREAD_POOL_NAME)
            .daemon(true).build();

    private static final int DEFAULT_SIZE = 1000;

    private static final long DEFAULT_KEEP_ALIVE = 60L;

    private static ThreadPoolExecutor executor;

    private static BlockingQueue<Runnable> executeQueue = new ArrayBlockingQueue<>(DEFAULT_SIZE);

    static {
        try {
            executor = new ThreadPoolExecutor(DEFAULT_MAX_CONCURRENT, DEFAULT_MAX_CONCURRENT, DEFAULT_KEEP_ALIVE,
                    TimeUnit.SECONDS, executeQueue, FACTORY);

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    log.info("SmsSendThreadPool shutting down.");
                    executor.shutdown();

                    try {
                        if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                            log.info("SmsSendThreadPool shutdown immediately due to wait timeout.");
                            executor.shutdownNow();
                        }
                    } catch (InterruptedException e) {
                        log.info("SmsSendThreadPool shutdown interrupted.");
                        executor.shutdownNow();
                    }

                    log.info("SmsSendThreadPool shutdown complete.");
                }
            }));
        } catch (Exception e) {
            log.log(Level.WARNING, "SmsSendThreadPool init error", e);
            throw new ExceptionInInitializerError(e);
        }
    }

    private SmsSendThreadPool() {
    }

    public static boolean execute(Runnable task) {
        try {
            executor.execute(task);
            TimeUnit.SECONDS.sleep(1);
        } catch (RejectedExecutionException | InterruptedException e) {
            log.log(Level.WARNING, "Task executing was rejected", e);
            return false;
        }
        return true;
    }

    public static int getActiveCount() {
        return executor.getActiveCount();
    }

    public static long getTaskCount() {
        return executor.getTaskCount();
    }

    public static long getCompletedTaskCount() {
        return executor.getCompletedTaskCount();
    }

    public static <T> Future<T> submitTask(Callable<T> task) {

        try {
            return executor.submit(task);
        } catch (RejectedExecutionException e) {
            log.log(Level.WARNING, "Task executing was rejected: {}", e);
            throw new UnsupportedOperationException("Unable to submit the task, rejected.", e);
        }
    }
}
