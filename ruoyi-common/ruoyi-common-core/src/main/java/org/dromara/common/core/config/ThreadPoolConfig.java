// 配置定时任务线程池，支持虚拟线程和传统线程模式
package org.dromara.common.core.config;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.dromara.common.core.config.properties.ThreadPoolProperties;
import org.dromara.common.core.utils.SpringUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.VirtualThreadTaskExecutor;

import java.util.concurrent.*;

/**
 * 线程池配置
 * 配置定时任务线程池，支持虚拟线程（Project Loom）和传统线程
 * 该类负责创建和管理ScheduledExecutorService，用于执行系统中的定时任务
 * 支持JDK 21+的虚拟线程技术，提升并发性能
 * 同时提供优雅的线程池关闭机制，确保应用正常退出
 *
 * @author Lion Li
 **/
// Lombok注解：自动生成SLF4J日志对象，简化日志记录代码
@Slf4j
// Spring Boot自动配置类，会在应用启动时自动加载
@AutoConfiguration
// 启用ThreadPoolProperties配置属性类，支持从application.yml读取配置
@EnableConfigurationProperties(ThreadPoolProperties.class)
public class ThreadPoolConfig {

    /**
     * 核心线程数 = cpu 核心数 + 1
     * 计算公式：Runtime获取可用处理器数量 + 1，确保CPU密集型任务最优性能
     * 这是线程池的基本大小，即使空闲也会保持的线程数量
     * 对于定时任务来说，这个数量通常足够处理大部分场景
     */
    private final int core = Runtime.getRuntime().availableProcessors() + 1;

    // 定时任务线程池实例，用于优雅关闭
    // 保存实例引用，在应用销毁时能够正确关闭线程池
    private ScheduledExecutorService scheduledExecutorService;

    /**
     * 执行周期性或定时任务
     * 创建ScheduledExecutorService Bean，用于执行@Scheduled注解的定时任务
     * 支持虚拟线程和传统线程两种模式，根据JDK版本自动选择
     * 提供统一的异常处理机制，确保任务异常能够被记录
     */
    @Bean(name = "scheduledExecutorService")
    protected ScheduledExecutorService scheduledExecutorService() {
        // daemon 必须为 true，确保线程不会阻止JVM退出
        // 非守护线程会阻止JVM正常退出，可能导致应用无法关闭
        BasicThreadFactory.Builder builder = new BasicThreadFactory.Builder().daemon(true);
        
        // 判断当前环境是否支持虚拟线程（JDK 21+）
        // 虚拟线程是Project Loom的特性，可以创建大量轻量级线程
        if (SpringUtils.isVirtual()) {
            // 虚拟线程模式：使用虚拟线程工厂，线程名格式为virtual-schedule-pool-%d
            // 虚拟线程由JVM管理，创建和销毁的开销极小，适合IO密集型任务
            builder.namingPattern("virtual-schedule-pool-%d").wrappedFactory(new VirtualThreadTaskExecutor().getVirtualThreadFactory());
            log.info("使用虚拟线程模式创建定时任务线程池");
        } else {
            // 传统线程模式：线程名格式为schedule-pool-%d
            // 使用传统的操作系统线程，适合CPU密集型任务
            builder.namingPattern("schedule-pool-%d");
            log.info("使用传统线程模式创建定时任务线程池");
        }
        
        // 创建ScheduledThreadPoolExecutor，设置核心线程数、线程工厂和拒绝策略
        // CallerRunsPolicy：当线程池满时，由调用线程执行任务，避免任务丢失
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(core,
            builder.build(),
            new ThreadPoolExecutor.CallerRunsPolicy()) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                // 调用父类方法，确保父类的清理逻辑被执行
                super.afterExecute(r, t);
                // 打印异常信息，方便排查问题
                // 这里会捕获任务执行过程中的所有异常，避免静默失败
                printException(r, t);
            }
        };
        
        // 保存线程池实例，用于销毁时关闭
        this.scheduledExecutorService = scheduledThreadPoolExecutor;
        log.info("定时任务线程池创建成功，核心线程数：{}", core);
        return scheduledThreadPoolExecutor;
    }

    /**
     * 销毁事件
     * 停止线程池
     * 先使用shutdown, 停止接收新任务并尝试完成所有已存在任务.
     * 如果超时, 则调用shutdownNow, 取消在workQueue中Pending的任务,并中断所有阻塞函数.
     * 如果仍然超時，則強制退出.
     * 另对在shutdown时线程本身被调用中断做了处理.
     * 这是标准的线程池优雅关闭流程，确保不丢失正在执行的任务
     */
    @PreDestroy
    public void destroy() {
        try {
            // 记录日志，提示开始关闭线程池
            log.info("====关闭后台任务任务线程池====");
            // 获取线程池实例
            ScheduledExecutorService pool = scheduledExecutorService;
            // 判断线程池是否已关闭
            if (pool != null && !pool.isShutdown()) {
                // 优雅关闭：不再接收新任务，等待已完成任务
                // 这是第一步，会等待正在执行的任务完成
                pool.shutdown();
                try {
                    // 等待120秒，如果超时则强制关闭
                    // 120秒应该足够完成大部分定时任务
                    if (!pool.awaitTermination(120, TimeUnit.SECONDS)) {
                        // 强制关闭：尝试停止所有正在执行的任务
                        // 会中断正在执行的任务，可能导致任务执行不完整
                        pool.shutdownNow();
                        // 再次等待120秒，给任务响应中断的时间
                        if (!pool.awaitTermination(120, TimeUnit.SECONDS)) {
                            // 记录日志，提示线程池未正常终止
                            // 这种情况通常表示有任务没有正确响应中断
                            log.info("Pool did not terminate");
                        }
                    }
                    log.info("定时任务线程池已优雅关闭");
                } catch (InterruptedException ie) {
                    // 当前线程被中断，强制关闭线程池
                    // 在关闭过程中，当前线程被其他线程中断
                    pool.shutdownNow();
                    // 恢复中断状态
                    // 这是标准的中断处理模式，确保中断状态不被吞掉
                    Thread.currentThread().interrupt();
                    log.warn("定时任务线程池关闭过程被中断");
                }
            }
        } catch (Exception e) {
            // 记录异常日志
            // 捕获所有未预期的异常，确保应用能够正常退出
            log.error("关闭定时任务线程池时发生异常", e);
        }
    }

    /**
     * 打印线程异常信息
     * 静态方法，方便其他线程池复用
     * 从Runnable和Future中提取异常信息，确保所有异常都能被记录
     * 这是线程池异常处理的标准做法，避免任务异常被静默吞掉
     */
    public static void printException(Runnable r, Throwable t) {
        // 如果异常为空且Runnable是Future类型，尝试从Future获取异常
        // Future任务可能抛出ExecutionException，需要特殊处理
        if (t == null && r instanceof Future<?>) {
            try {
                // 强制转换为Future
                Future<?> future = (Future<?>) r;
                // 判断任务是否完成
                // 只有完成的任务才可能包含异常
                if (future.isDone()) {
                    // 调用get()方法，如果任务执行异常会抛出ExecutionException
                    // 这是获取Future任务异常的标准方式
                    future.get();
                }
            } catch (CancellationException ce) {
                // 任务被取消，记录异常
                // 用户主动取消了任务
                t = ce;
            } catch (ExecutionException ee) {
                // 任务执行异常，获取根本原因
                // ExecutionException包装了任务执行过程中的实际异常
                t = ee.getCause();
            } catch (InterruptedException ie) {
                // 当前线程被中断，恢复中断状态
                // 在获取Future结果的过程中，当前线程被中断
                Thread.currentThread().interrupt();
            }
        }
        // 如果异常不为空，记录错误日志
        if (t != null) {
            log.error("定时任务执行异常: {}", t.getMessage(), t);
        }
    }

}
