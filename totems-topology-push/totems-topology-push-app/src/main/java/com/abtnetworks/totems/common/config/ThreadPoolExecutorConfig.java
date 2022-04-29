package com.abtnetworks.totems.common.config;

import com.abtnetworks.totems.common.executor.ExecutorDto;
import com.abtnetworks.totems.common.executor.ExtendedRunnable;
import com.abtnetworks.totems.common.utils.DateUtils;
import com.abtnetworks.totems.common.utils.IdGen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class ThreadPoolExecutorConfig {

    private static Logger logger = LoggerFactory.getLogger(ThreadPoolExecutorConfig.class);

    public static ConcurrentHashMap<String, Thread> RUNNING_MAP = new ConcurrentHashMap<>();
    public static HashMap<String, ExecutorDto> INFO_MAP = new HashMap<>();

    /**ThreadPoolExecutor的策略
    1 线程数量未达到corePoolSize，则新建一个线程(核心线程)执行任务
    2 线程数量达到了corePools，则将任务移入队列等待
    3 队列已满，新建线程(非核心线程)执行任务
    4 队列已满，总线程数又达到了maximumPoolSize，就会由(RejectedExecutionHandler)抛出异常
    */
    @Value("${task.executor.pool.size}")
    private Integer taskExecutorPoolSize = 5;

    @Value("${task.executor.queue.size}")
    private Integer taskExecutorQueueSize = 200;

    @Value("${subnet.executor.pool.size}")
    private Integer subnetExecutorPoolSize = 5;

    @Value("${subnet.executor.queue.size}")
    private Integer subnetExecutorQueueSize = 200;

    @Value("${analyze.executor.pool.size}")
    private Integer analyzeExecutorPoolSize = 5;

    @Value("${analyze.executor.queue.size}")
    private Integer analyzeExecutorQueueSize = 200;

    @Value("${merge.executor.pool.size}")
    private Integer mergeExecutorPoolSize = 5;

    @Value("${merge.executor.queue.size}")
    private Integer mergeExecutorQueueSize = 200;

    @Value("${translation.executor.pool.size}")
    private Integer translationExecutorPoolSize = 5;

    @Value("${translation.executor.queue.size}")
    private Integer translationExecutorQueueSize = 200;

    @Value("${auto.task.executor.pool.size}")
    private Integer autoTaskExecutorPoolSize = 10;

    @Bean
    public Executor pushExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //配置核心线程数
        executor.setCorePoolSize(taskExecutorPoolSize);
        //配置最大线程数
        executor.setMaxPoolSize(taskExecutorPoolSize);
        // 设置线程活跃时间（秒）
        executor.setKeepAliveSeconds(30);
        //配置队列大小
        executor.setQueueCapacity(taskExecutorQueueSize);
        //配置线程池中的线程的名称前缀
        executor.setThreadNamePrefix("push-");

        // setRejectedExecutionHandler：当pool已经达到max size的时候，如何处理新任务
        /*
        AbortPolicy：用于被拒绝任务的处理程序，它将抛出RejectedExecutionException。
     CallerRunsPolicy：用于被拒绝任务的处理程序，不在新线程中执行任务，它直接在execute方法的调用线程中运行被拒绝的任务。
     DiscardOldestPolicy：用于被拒绝任务的处理程序，它放弃最旧的未处理请求，然后重试execute。
     DiscardPolicy：用于被拒绝任务的处理程序，默认情况下它将丢弃被拒绝的任务。
        */
        System.out.println("taskExecutorPoolSize:" + taskExecutorPoolSize + ";taskExecutorQueueSize" + taskExecutorQueueSize);
        executor.setRejectedExecutionHandler((r, t) -> {
            try {
                String id = DateUtils.formatDate(new Date(), "yyyyMMddHHmmss")+"-"+ IdGen.randomBase62(6);
                logger.warn("任务id："+id+"，线程池 pushExecutor 已经超出最大线程数，队列阻塞，等待可用空间再继续！目前队列数量："
                        + t.getQueue().size()+"个，时间："+new Date());
                t.getQueue().put(r);
                logger.warn("任务id："+id+"，线程池 pushExecutor 阻塞已经解除！");
            } catch (InterruptedException e) {
                logger.error("线程池 pushExecutor 阻塞异常：", e);
            }
        });
        //执行初始化
        executor.initialize();
        return executor;
    }

    @Bean
    public Executor pathExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //配置核心线程数
        executor.setCorePoolSize(subnetExecutorPoolSize);
        //配置最大线程数
        executor.setMaxPoolSize(subnetExecutorPoolSize);
        // 设置线程活跃时间（秒）
        executor.setKeepAliveSeconds(30);
        //配置队列大小
        executor.setQueueCapacity(subnetExecutorQueueSize);
        //配置线程池中的线程的名称前缀
        executor.setThreadNamePrefix("path-");

        // setRejectedExecutionHandler：当pool已经达到max size的时候，如何处理新任务
        /*
        AbortPolicy：用于被拒绝任务的处理程序，它将抛出RejectedExecutionException。
     CallerRunsPolicy：用于被拒绝任务的处理程序，不在新线程中执行任务，它直接在execute方法的调用线程中运行被拒绝的任务。
     DiscardOldestPolicy：用于被拒绝任务的处理程序，它放弃最旧的未处理请求，然后重试execute。
     DiscardPolicy：用于被拒绝任务的处理程序，默认情况下它将丢弃被拒绝的任务。
        */
        System.out.println("subnetExecutorPoolSize:" + subnetExecutorPoolSize + ";subnetExecutorQueueSize" + subnetExecutorQueueSize);
        executor.setRejectedExecutionHandler((r, t) -> {
            try {
                String id = DateUtils.formatDate(new Date(), "yyyyMMddHHmmss")+"-"+ IdGen.randomBase62(6);
                logger.warn("任务id："+id+"，线程池 pathExecutor 已经超出最大线程数，队列阻塞，等待可用空间再继续！目前队列数量："
                        + t.getQueue().size()+"个，时间："+new Date());
                t.getQueue().put(r);
                logger.warn("任务id："+id+"，线程池 pathExecutor 阻塞已经解除！");
            } catch (InterruptedException e) {
                logger.error("线程池 pathExecutor 阻塞异常：", e);
            }
        });
        //执行初始化
        executor.initialize();
        return executor;
    }

    @Bean
    public Executor analyzeExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //配置核心线程数
        executor.setCorePoolSize(analyzeExecutorPoolSize);
        //配置最大线程数
        executor.setMaxPoolSize(analyzeExecutorPoolSize);
        // 设置线程活跃时间（秒）
        executor.setKeepAliveSeconds(30);
        //配置队列大小
        executor.setQueueCapacity(analyzeExecutorQueueSize);
        //配置线程池中的线程的名称前缀
        executor.setThreadNamePrefix("analyze-");

        // setRejectedExecutionHandler：当pool已经达到max size的时候，如何处理新任务
        /*
        AbortPolicy：用于被拒绝任务的处理程序，它将抛出RejectedExecutionException。
     CallerRunsPolicy：用于被拒绝任务的处理程序，不在新线程中执行任务，它直接在execute方法的调用线程中运行被拒绝的任务。
     DiscardOldestPolicy：用于被拒绝任务的处理程序，它放弃最旧的未处理请求，然后重试execute。
     DiscardPolicy：用于被拒绝任务的处理程序，默认情况下它将丢弃被拒绝的任务。
        */
        System.out.println("analyzeExecutorPoolSize:" + analyzeExecutorPoolSize + ";analyzeExecutorQueueSize" + analyzeExecutorQueueSize);
        executor.setRejectedExecutionHandler((r, t) -> {
            try {
                String id = DateUtils.formatDate(new Date(), "yyyyMMddHHmmss")+"-"+ IdGen.randomBase62(6);
                logger.warn("任务id："+id+"，线程池 analyzeExecutor 已经超出最大线程数，队列阻塞，等待可用空间再继续！目前队列数量："
                        + t.getQueue().size()+"个，时间："+new Date());
                t.getQueue().put(r);
                logger.warn("任务id："+id+"，线程池 mergeExecutor 阻塞已经解除！");
            } catch (InterruptedException e) {
                logger.error("线程池 analyzeExecutor 阻塞异常：", e);
            }
        });
        //执行初始化
        executor.initialize();
        return executor;
    }

    @Bean
    public Executor mergeExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //配置核心线程数
        executor.setCorePoolSize(mergeExecutorPoolSize);
        //配置最大线程数
        executor.setMaxPoolSize(mergeExecutorPoolSize);
        // 设置线程活跃时间（秒）
        executor.setKeepAliveSeconds(30);
        //配置队列大小
        executor.setQueueCapacity(mergeExecutorQueueSize);
        //配置线程池中的线程的名称前缀
        executor.setThreadNamePrefix("merge-");

        // setRejectedExecutionHandler：当pool已经达到max size的时候，如何处理新任务
        /*
        AbortPolicy：用于被拒绝任务的处理程序，它将抛出RejectedExecutionException。
     CallerRunsPolicy：用于被拒绝任务的处理程序，不在新线程中执行任务，它直接在execute方法的调用线程中运行被拒绝的任务。
     DiscardOldestPolicy：用于被拒绝任务的处理程序，它放弃最旧的未处理请求，然后重试execute。
     DiscardPolicy：用于被拒绝任务的处理程序，默认情况下它将丢弃被拒绝的任务。
        */
        System.out.println("mergeExecutorPoolSize:" + mergeExecutorPoolSize + ";mergeExecutorQueueSize" + mergeExecutorQueueSize);
        executor.setRejectedExecutionHandler((r, t) -> {
            try {
                String id = DateUtils.formatDate(new Date(), "yyyyMMddHHmmss")+"-"+ IdGen.randomBase62(6);
                logger.warn("任务id："+id+"，线程池 mergeExecutor 已经超出最大线程数，队列阻塞，等待可用空间再继续！目前队列数量："
                        + t.getQueue().size()+"个，时间："+new Date());
                t.getQueue().put(r);
                logger.warn("任务id："+id+"，线程池 mergeExecutor 阻塞已经解除！");
            } catch (InterruptedException e) {
                logger.error("线程池 mergeExecutor 阻塞异常：", e);
            }
        });
        //执行初始化
        executor.initialize();
        return executor;
    }

    @Bean
    public Executor commandExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //配置核心线程数
        executor.setCorePoolSize(taskExecutorPoolSize);
        //配置最大线程数
        executor.setMaxPoolSize(taskExecutorPoolSize);
        // 设置线程活跃时间（秒）
        executor.setKeepAliveSeconds(30);
        //配置队列大小
        executor.setQueueCapacity(200);
        //配置线程池中的线程的名称前缀
        executor.setThreadNamePrefix("command-");

        // setRejectedExecutionHandler：当pool已经达到max size的时候，如何处理新任务
        /*
        AbortPolicy：用于被拒绝任务的处理程序，它将抛出RejectedExecutionException。
     CallerRunsPolicy：用于被拒绝任务的处理程序，不在新线程中执行任务，它直接在execute方法的调用线程中运行被拒绝的任务。
     DiscardOldestPolicy：用于被拒绝任务的处理程序，它放弃最旧的未处理请求，然后重试execute。
     DiscardPolicy：用于被拒绝任务的处理程序，默认情况下它将丢弃被拒绝的任务。
        */
        executor.setRejectedExecutionHandler((r, t) -> {
            try {
                String id = DateUtils.formatDate(new Date(), "yyyyMMddHHmmss")+"-"+ IdGen.randomBase62(6);
                logger.warn("任务id："+id+"，线程池 commandExecutor 已经超出最大线程数，队列阻塞，等待可用空间再继续！目前队列数量："
                        + t.getQueue().size()+"个，时间："+new Date());
                t.getQueue().put(r);
                logger.warn("任务id："+id+"，线程池 commandExecutor 阻塞已经解除！");
            } catch (InterruptedException e) {
                logger.error("线程池 commandlineExecutor 阻塞异常：", e);
            }
        });
        //执行初始化
        executor.initialize();
        return executor;
    }

    @Bean
    public Executor commandlineExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //配置核心线程数
        executor.setCorePoolSize(5);
        //配置最大线程数
        executor.setMaxPoolSize(5);
        // 设置线程活跃时间（秒）
        executor.setKeepAliveSeconds(30);
        //配置队列大小
        executor.setQueueCapacity(200);
        //配置线程池中的线程的名称前缀
        executor.setThreadNamePrefix("commandline-");

        // setRejectedExecutionHandler：当pool已经达到max size的时候，如何处理新任务
        /*
        AbortPolicy：用于被拒绝任务的处理程序，它将抛出RejectedExecutionException。
     CallerRunsPolicy：用于被拒绝任务的处理程序，不在新线程中执行任务，它直接在execute方法的调用线程中运行被拒绝的任务。
     DiscardOldestPolicy：用于被拒绝任务的处理程序，它放弃最旧的未处理请求，然后重试execute。
     DiscardPolicy：用于被拒绝任务的处理程序，默认情况下它将丢弃被拒绝的任务。
        */
        executor.setRejectedExecutionHandler((r, t) -> {
            try {
                String id = DateUtils.formatDate(new Date(), "yyyyMMddHHmmss")+"-"+ IdGen.randomBase62(6);
                logger.warn("任务id："+id+"，线程池 commandlineExecutor 已经超出最大线程数，队列阻塞，等待可用空间再继续！目前队列数量："
                        + t.getQueue().size()+"个，时间："+new Date());
                t.getQueue().put(r);
                logger.warn("任务id："+id+"，线程池 commandlineExecutor 阻塞已经解除！");
            } catch (InterruptedException e) {
                logger.error("线程池 commandlineExecutor 阻塞异常：", e);
            }
        });
        //执行初始化
        executor.initialize();
        return executor;
    }


    /**
     * 应急处置，封堵工单下发命令行。线程池配置
     * @return
     */
    @Bean
    public Executor orderDisposalExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //配置核心线程数
        executor.setCorePoolSize(1);
        //配置最大线程数
        executor.setMaxPoolSize(1);
        // 设置线程活跃时间（秒）
        executor.setKeepAliveSeconds(30);
        //配置队列大小
        executor.setQueueCapacity(200);
        //配置线程池中的线程的名称前缀
        executor.setThreadNamePrefix("disposal-order-");

        // setRejectedExecutionHandler：当pool已经达到max size的时候，如何处理新任务
        /*
        AbortPolicy：用于被拒绝任务的处理程序，它将抛出RejectedExecutionException。
        CallerRunsPolicy：用于被拒绝任务的处理程序，不在新线程中执行任务，它直接在execute方法的调用线程中运行被拒绝的任务。
        DiscardOldestPolicy：用于被拒绝任务的处理程序，它放弃最旧的未处理请求，然后重试execute。
        DiscardPolicy：用于被拒绝任务的处理程序，默认情况下它将丢弃被拒绝的任务。
        */
        executor.setRejectedExecutionHandler((r, e) -> {
            try {
                ExtendedRunnable runnable = (ExtendedRunnable) r;
                //任务id
                String id = DateUtils.formatDate(new Date(), "yyyyMMddHHmmss")+"-"+ IdGen.randomBase62(6);
                logger.warn("任务id："+id+"，线程池 orderDisposalExecutor 已经超出最大线程数，队列阻塞，等待可用空间再继续！目前队列数量："
                        + e.getQueue().size()+"个，线程信息："+runnable.getExecutorDto().toString());
                e.getQueue().put(r);
                logger.warn("任务id："+id+"，线程池 orderDisposalExecutor 阻塞已经解除！");
            } catch (InterruptedException ex) {
                logger.error("线程池 orderDisposalExecutor 超出最大线程数：", ex);
            }
        });
        //执行初始化
        executor.initialize();
        return executor;
    }

    @Bean
    public Executor prePushTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //配置核心线程数
        executor.setCorePoolSize(taskExecutorPoolSize);
        //配置最大线程数
        executor.setMaxPoolSize(taskExecutorPoolSize);
        // 设置线程活跃时间（秒）
        executor.setKeepAliveSeconds(30);
        //配置队列大小
        executor.setQueueCapacity(200);
        //配置线程池中的线程的名称前缀
        executor.setThreadNamePrefix("pre-push-task-");

        executor.setRejectedExecutionHandler((r, e) -> {
            try {
                ExtendedRunnable runnable = (ExtendedRunnable) r;
                //任务id
                String id = DateUtils.formatDate(new Date(), "yyyyMMddHHmmss")+"-"+ IdGen.randomBase62(6);
                logger.warn("任务id："+id+"，线程池 prePushTaskExecutor 已经超出最大线程数，队列阻塞，等待可用空间再继续！目前队列数量："
                        + e.getQueue().size()+"个，线程信息："+runnable.getExecutorDto().toString());
                e.getQueue().put(r);
                logger.warn("任务id："+id+"，线程池 prePushTaskExecutor 阻塞已经解除！");
            } catch (InterruptedException ex) {
                logger.error("线程池 prePushTaskExecutor 超出最大线程数：", ex);
            }
        });
        //执行初始化
        executor.initialize();
        return executor;
    }

    @Bean
    public Executor pushScheduleExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //配置核心线程数
        executor.setCorePoolSize(1);
        //配置最大线程数
        executor.setMaxPoolSize(1);
        // 设置线程活跃时间（秒）
        executor.setKeepAliveSeconds(30);
        //配置队列大小
        executor.setQueueCapacity(200);
        //配置线程池中的线程的名称前缀
        executor.setThreadNamePrefix("push-schedule-");

        executor.setRejectedExecutionHandler((r, e) -> {
            try {
                ExtendedRunnable runnable = (ExtendedRunnable) r;
                //任务id
                String id = DateUtils.formatDate(new Date(), "yyyyMMddHHmmss")+"-"+ IdGen.randomBase62(6);
                logger.warn("任务id："+id+"，线程池 orderDisposalExecutor 已经超出最大线程数，队列阻塞，等待可用空间再继续！目前队列数量："
                        + e.getQueue().size()+"个，线程信息："+runnable.getExecutorDto().toString());
                e.getQueue().put(r);
                logger.warn("任务id："+id+"，线程池 orderDisposalExecutor 阻塞已经解除！");
            } catch (InterruptedException ex) {
                logger.error("线程池 orderDisposalExecutor 超出最大线程数：", ex);
            }
        });
        //执行初始化
        executor.initialize();
        return executor;
    }

    @Bean
    public Executor generateExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //配置核心线程数
        executor.setCorePoolSize(5);
        //配置最大线程数
        executor.setMaxPoolSize(5);
        // 设置线程活跃时间（秒）
        executor.setKeepAliveSeconds(30);
        //配置队列大小
        executor.setQueueCapacity(200);
        //配置线程池中的线程的名称前缀
        executor.setThreadNamePrefix("generate-schedule-");

        executor.setRejectedExecutionHandler((r, e) -> {
            try {
                ExtendedRunnable runnable = (ExtendedRunnable) r;
                //任务id
                String id = DateUtils.formatDate(new Date(), "yyyyMMddHHmmss")+"-"+ IdGen.randomBase62(6);
                logger.warn("任务id："+id+"，线程池 generateScheduleExecutor 已经超出最大线程数，队列阻塞，等待可用空间再继续！目前队列数量："
                        + e.getQueue().size()+"个，线程信息："+runnable.getExecutorDto().toString());
                e.getQueue().put(r);
                logger.warn("任务id："+id+"，线程池 generateScheduleExecutor 阻塞已经解除！");
            } catch (InterruptedException ex) {
                logger.error("线程池 generateScheduleExecutor 超出最大线程数：", ex);
            }
        });
        //执行初始化
        executor.initialize();
        return executor;
    }

    @Bean
    public Executor batchImportExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //配置核心线程数
        executor.setCorePoolSize(1);
        //配置最大线程数
        executor.setMaxPoolSize(1);
        // 设置线程活跃时间（秒）
        executor.setKeepAliveSeconds(30);
        //配置队列大小
        executor.setQueueCapacity(200);
        //配置线程池中的线程的名称前缀
        executor.setThreadNamePrefix("batch-import-schedule-");

        executor.setRejectedExecutionHandler((r, e) -> {
            try {
                ExtendedRunnable runnable = (ExtendedRunnable) r;
                //任务id
                String id = DateUtils.formatDate(new Date(), "yyyyMMddHHmmss")+"-"+ IdGen.randomBase62(6);
                logger.warn("任务id："+id+"，线程池 batchImportExecutor 已经超出最大线程数，队列阻塞，等待可用空间再继续！目前队列数量："
                        + e.getQueue().size()+"个，线程信息："+runnable.getExecutorDto().toString());
                e.getQueue().put(r);
                logger.warn("任务id："+id+"，线程池 batchImportExecutor 阻塞已经解除！");
            } catch (InterruptedException ex) {
                logger.error("线程池 batchImportExecutor 超出最大线程数：", ex);
            }
        });
        //执行初始化
        executor.initialize();
        return executor;
    }

    /**
     * 封禁线程配置
     */
    @Bean
    public Executor forbidExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //配置核心线程数
        executor.setCorePoolSize(1);
        //配置最大线程数
        executor.setMaxPoolSize(1);
        // 设置线程活跃时间（秒）
        executor.setKeepAliveSeconds(30);
        //配置队列大小
        executor.setQueueCapacity(200);
        //配置线程池中的线程的名称前缀
        executor.setThreadNamePrefix("forbid-");

        // setRejectedExecutionHandler：当pool已经达到max size的时候，如何处理新任务
        /*
        AbortPolicy：用于被拒绝任务的处理程序，它将抛出RejectedExecutionException。
        CallerRunsPolicy：用于被拒绝任务的处理程序，不在新线程中执行任务，它直接在execute方法的调用线程中运行被拒绝的任务。
        DiscardOldestPolicy：用于被拒绝任务的处理程序，它放弃最旧的未处理请求，然后重试execute。
        DiscardPolicy：用于被拒绝任务的处理程序，默认情况下它将丢弃被拒绝的任务。
        */
        executor.setRejectedExecutionHandler((r, e) -> {
            try {
                ExtendedRunnable runnable = (ExtendedRunnable) r;
                //任务id
                String id = DateUtils.formatDate(new Date(), "yyyyMMddHHmmss")+"-"+ IdGen.randomBase62(6);
                logger.warn("任务id："+id+"，线程池 forbidExecutor 已经超出最大线程数，队列阻塞，等待可用空间再继续！目前队列数量："
                        + e.getQueue().size()+"个，线程信息："+runnable.getExecutorDto().toString());
                e.getQueue().put(r);
                logger.warn("任务id："+id+"，线程池 forbidExecutor 阻塞已经解除！");
            } catch (InterruptedException ex) {
                logger.error("线程池 forbidExecutor 超出最大线程数：", ex);
            }
        });
        //执行初始化
        executor.initialize();
        return executor;
    }

    /**
     * 策略迁移线程配置
     */
    @Bean
    public ThreadPoolTaskExecutor translationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //配置核心线程数
        executor.setCorePoolSize(translationExecutorPoolSize);
        //配置最大线程数
        executor.setMaxPoolSize(translationExecutorPoolSize);
        // 设置线程活跃时间（秒）
        executor.setKeepAliveSeconds(30);
        //配置队列大小
        executor.setQueueCapacity(translationExecutorPoolSize);
        //配置线程池中的线程的名称前缀
        executor.setThreadNamePrefix("translation-");

        // setRejectedExecutionHandler：当pool已经达到max size的时候，如何处理新任务
        /*
        AbortPolicy：用于被拒绝任务的处理程序，它将抛出RejectedExecutionException。
        CallerRunsPolicy：用于被拒绝任务的处理程序，不在新线程中执行任务，它直接在execute方法的调用线程中运行被拒绝的任务。
        DiscardOldestPolicy：用于被拒绝任务的处理程序，它放弃最旧的未处理请求，然后重试execute。
        DiscardPolicy：用于被拒绝任务的处理程序，默认情况下它将丢弃被拒绝的任务。
        */
        executor.setRejectedExecutionHandler((r, e) -> {
            try {
                ExtendedRunnable runnable = (ExtendedRunnable) r;
                //任务id
                String id = DateUtils.formatDate(new Date(), "yyyyMMddHHmmss")+"-"+ IdGen.randomBase62(6);
                logger.warn("任务id："+id+"，线程池 TranslationTaskExecutor 已经超出最大线程数，队列阻塞，等待可用空间再继续！目前队列数量："
                        + e.getQueue().size()+"个，线程信息："+runnable.getExecutorDto().toString());
                e.getQueue().put(r);
                logger.warn("任务id："+id+"，线程池 TranslationTaskExecutor 阻塞已经解除！");
            } catch (InterruptedException ex) {
                logger.error("线程池 TranslationTaskExecutor 超出最大线程数：", ex);
            }
        });
        //执行初始化
        executor.initialize();
        return executor;
    }

    @Bean
    public Executor autoTaskPushExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //配置核心线程数
        executor.setCorePoolSize(1);
        //配置最大线程数
        executor.setMaxPoolSize(1);
        // 设置线程活跃时间（秒）
        executor.setKeepAliveSeconds(30);
        //配置队列大小
        executor.setQueueCapacity(translationExecutorQueueSize);
        //配置线程池中的线程的名称前缀
        executor.setThreadNamePrefix("autoTaskPush-");

        // setRejectedExecutionHandler：当pool已经达到max size的时候，如何处理新任务
        /*
        AbortPolicy：用于被拒绝任务的处理程序，它将抛出RejectedExecutionException。
     CallerRunsPolicy：用于被拒绝任务的处理程序，不在新线程中执行任务，它直接在execute方法的调用线程中运行被拒绝的任务。
     DiscardOldestPolicy：用于被拒绝任务的处理程序，它放弃最旧的未处理请求，然后重试execute。
     DiscardPolicy：用于被拒绝任务的处理程序，默认情况下它将丢弃被拒绝的任务。
        */
        executor.setRejectedExecutionHandler((r, t) -> {
            try {
                String id = DateUtils.formatDate(new Date(), "yyyyMMddHHmmss")+"-"+ IdGen.randomBase62(6);
                logger.warn("任务id："+id+"，线程池 autoTaskPushExecutor 已经超出最大线程数，队列阻塞，等待可用空间再继续！目前队列数量："
                        + t.getQueue().size()+"个，时间："+new Date());
                t.getQueue().put(r);
                logger.warn("任务id："+id+"，线程池 autoTaskPushExecutor 阻塞已经解除！");
            } catch (InterruptedException e) {
                logger.error("线程池 autoTaskPushExecutor 阻塞异常：", e);
            }
        });
        //执行初始化
        executor.initialize();
        return executor;
    }

    @Bean
    public Executor autoTaskGenerateCommandlineExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //配置核心线程数
        executor.setCorePoolSize(1);
        //配置最大线程数
        executor.setMaxPoolSize(1);
        // 设置线程活跃时间（秒）
        executor.setKeepAliveSeconds(30);
        //配置队列大小
        executor.setQueueCapacity(translationExecutorQueueSize);
        //配置线程池中的线程的名称前缀
        executor.setThreadNamePrefix("autoTaskGenerateCommandline-");

        // setRejectedExecutionHandler：当pool已经达到max size的时候，如何处理新任务
        /*
        AbortPolicy：用于被拒绝任务的处理程序，它将抛出RejectedExecutionException。
     CallerRunsPolicy：用于被拒绝任务的处理程序，不在新线程中执行任务，它直接在execute方法的调用线程中运行被拒绝的任务。
     DiscardOldestPolicy：用于被拒绝任务的处理程序，它放弃最旧的未处理请求，然后重试execute。
     DiscardPolicy：用于被拒绝任务的处理程序，默认情况下它将丢弃被拒绝的任务。
        */
        executor.setRejectedExecutionHandler((r, t) -> {
            try {
                String id = DateUtils.formatDate(new Date(), "yyyyMMddHHmmss")+"-"+ IdGen.randomBase62(6);
                logger.warn("任务id："+id+"，线程池 autoTaskGenerateCommandlineExecutor 已经超出最大线程数，队列阻塞，等待可用空间再继续！目前队列数量："
                        + t.getQueue().size()+"个，时间："+new Date());
                t.getQueue().put(r);
                logger.warn("任务id："+id+"，线程池 autoTaskGenerateCommandlineExecutor 阻塞已经解除！");
            } catch (InterruptedException e) {
                logger.error("线程池 autoTaskGenerateCommandlineExecutor 阻塞异常：", e);
            }
        });
        //执行初始化
        executor.initialize();
        return executor;
    }

    @Bean
    public Executor autoGenerateExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //配置核心线程数
        executor.setCorePoolSize(autoTaskExecutorPoolSize);
        //配置最大线程数
        executor.setMaxPoolSize(autoTaskExecutorPoolSize);
        // 设置线程活跃时间（秒）
        executor.setKeepAliveSeconds(30);
        //配置队列大小
        executor.setQueueCapacity(200);
        //配置线程池中的线程的名称前缀
        executor.setThreadNamePrefix("auto-generate-");

        executor.setRejectedExecutionHandler((r, e) -> {
            try {
                ExtendedRunnable runnable = (ExtendedRunnable) r;
                //任务id
                String id = DateUtils.formatDate(new Date(), "yyyyMMddHHmmss")+"-"+ IdGen.randomBase62(6);
                logger.warn("任务id："+id+"，线程池 generateScheduleExecutor 已经超出最大线程数，队列阻塞，等待可用空间再继续！目前队列数量："
                        + e.getQueue().size()+"个，线程信息："+runnable.getExecutorDto().toString());
                e.getQueue().put(r);
                logger.warn("任务id："+id+"，线程池 autoGenerateExecutor 阻塞已经解除！");
            } catch (InterruptedException ex) {
                logger.error("线程池 autoGenerateExecutor 超出最大线程数：", ex);
                Thread.currentThread().interrupt();
            }
        });
        //执行初始化
        executor.initialize();
        return executor;
    }

    @Bean
    public Executor addressPushExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //配置核心线程数
        executor.setCorePoolSize(1);
        //配置最大线程数
        executor.setMaxPoolSize(1);
        // 设置线程活跃时间（秒）
        executor.setKeepAliveSeconds(30);
        //配置队列大小
        executor.setQueueCapacity(translationExecutorQueueSize);
        //配置线程池中的线程的名称前缀
        executor.setThreadNamePrefix("addressTaskPush-");

        // setRejectedExecutionHandler：当pool已经达到max size的时候，如何处理新任务
        /*
        AbortPolicy：用于被拒绝任务的处理程序，它将抛出RejectedExecutionException。
     CallerRunsPolicy：用于被拒绝任务的处理程序，不在新线程中执行任务，它直接在execute方法的调用线程中运行被拒绝的任务。
     DiscardOldestPolicy：用于被拒绝任务的处理程序，它放弃最旧的未处理请求，然后重试execute。
     DiscardPolicy：用于被拒绝任务的处理程序，默认情况下它将丢弃被拒绝的任务。
        */
        executor.setRejectedExecutionHandler((r, t) -> {
            try {
                String id = DateUtils.formatDate(new Date(), "yyyyMMddHHmmss")+"-"+ IdGen.randomBase62(6);
                logger.warn("任务id："+id+"，线程池 addressPushExecutor 已经超出最大线程数，队列阻塞，等待可用空间再继续！目前队列数量："
                        + t.getQueue().size()+"个，时间："+new Date());
                t.getQueue().put(r);
                logger.warn("任务id："+id+"，线程池 addressPushExecutor 阻塞已经解除！");
            } catch (InterruptedException e) {
                logger.error("线程池 addressPushExecutor 阻塞异常：", e);
                Thread.currentThread().interrupt();
            }
        });
        //执行初始化
        executor.initialize();
        return executor;
    }

}
