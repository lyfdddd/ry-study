// 定义限流注解的包路径
package org.dromara.common.ratelimiter.annotation;

// 限流类型枚举
import org.dromara.common.ratelimiter.enums.LimitType;

// Java元注解相关类
import java.lang.annotation.*;

/**
 * 限流注解
 * 用于标记需要进行限流控制的方法
 * 支持基于IP、集群或默认策略的限流
 * 使用Redis实现分布式限流
 *
 * @author Lion Li
 */
// 指定注解作用于方法级别
@Target(ElementType.METHOD)
// 指定注解在运行时保留，可通过反射读取
@Retention(RetentionPolicy.RUNTIME)
// 指定注解包含在JavaDoc中
@Documented
// 限流注解定义
public @interface RateLimiter {
    /**
     * 限流key，支持使用Spring EL表达式动态获取方法参数值
     * 格式类似于 #code.id 或 #{#code}
     * 用于区分不同的限流维度
     */
    String key() default "";

    /**
     * 限流时间窗口，单位秒
     * 默认60秒，表示在60秒内限制访问次数
     */
    int time() default 60;

    /**
     * 限流次数
     * 在time指定的时间窗口内允许的最大访问次数
     * 默认100次
     */
    int count() default 100;

    /**
     * 限流类型
     * 支持DEFAULT（默认）、IP（基于IP）、CLUSTER（集群限流）
     */
    LimitType limitType() default LimitType.DEFAULT;

    /**
     * 提示消息，支持国际化
     * 格式为 {code}，如 {rate.limiter.message}
     * 当触发限流时返回的提示信息
     */
    String message() default "{rate.limiter.message}";

    /**
     * 限流策略超时时间，单位秒
     * 默认一天（86400秒），策略存活时间，到期后会清除已存在的策略数据
     */
    int timeout() default 86400;

}
