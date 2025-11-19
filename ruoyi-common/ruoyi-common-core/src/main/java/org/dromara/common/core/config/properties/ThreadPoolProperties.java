// 定义线程池配置属性类，支持从application.yml读取配置
package org.dromara.common.core.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 线程池 配置属性
 * 通过application.yml配置文件中的thread-pool前缀配置线程池参数
 * 该类使用Spring Boot的配置属性机制，将配置文件中的属性绑定到Java对象
 * 支持线程池的启用状态、队列容量、线程存活时间等关键参数配置
 * 使用Lombok的@Data注解自动生成getter、setter、toString等方法
 * 配置示例：
 * thread-pool:
 *   enabled: true
 *   queue-capacity: 1000
 *   keep-alive-seconds: 60
 *
 * @author Lion Li
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode方法
// 简化POJO开发，避免手写样板代码
@Data
// Spring Boot配置属性注解，指定配置文件中的前缀为thread-pool
// 会自动将thread-pool.*的配置项绑定到对应的字段上
@ConfigurationProperties(prefix = "thread-pool")
public class ThreadPoolProperties {

    /**
     * 是否开启线程池
     * 配置示例：thread-pool.enabled=true
     * 该配置项控制线程池的启用状态，为true时启用自定义线程池配置
     * 为false时使用Spring Boot默认的线程池配置
     * 默认值为false，需要手动在配置文件中开启
     */
    private boolean enabled;

    /**
     * 队列最大长度
     * 配置示例：thread-pool.queue-capacity=1000
     * 当核心线程数已满且队列已满时，才会创建新线程（不超过最大线程数）
     * 这是线程池的工作队列容量，用于存储等待执行的任务
     * 合理的队列大小可以防止内存溢出，同时保证任务不会丢失
     * 默认值为Integer.MAX_VALUE，但建议设置合理的值
     */
    private int queueCapacity;

    /**
     * 线程池维护线程所允许的空闲时间（秒）
     * 配置示例：thread-pool.keep-alive-seconds=60
     * 当线程数超过核心线程数时，空闲线程的存活时间
     * 超过这个时间没有被使用的线程会被回收，释放系统资源
     * 合理的存活时间可以平衡资源利用和性能
     * 默认值为60秒，适合大多数应用场景
     */
    private int keepAliveSeconds;

    /**
     * 构造方法
     * 设置默认值，确保即使没有配置也能正常使用
     */
    public ThreadPoolProperties() {
        // 默认不启用自定义线程池配置
        this.enabled = false;
        // 默认队列容量，2的幂次方数，适合大多数场景
        this.queueCapacity = 1024;
        // 默认线程存活时间，60秒
        this.keepAliveSeconds = 60;
    }

}
