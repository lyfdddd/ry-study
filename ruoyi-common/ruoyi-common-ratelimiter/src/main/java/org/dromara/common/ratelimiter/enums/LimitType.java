// 定义限流类型枚举的包路径
package org.dromara.common.ratelimiter.enums;

/**
 * 限流类型枚举
 * 定义三种限流策略：默认全局限流、IP限流、集群实例限流
 * 用于@RateLimiter注解中指定限流维度
 *
 * @author ruoyi
 */
// 限流类型枚举定义
public enum LimitType {
    /**
     * 默认策略全局限流
     * 对所有请求使用统一的限流策略，不区分IP或实例
     */
    DEFAULT,

    /**
     * 根据请求者IP进行限流
     * 每个IP地址独立计算限流，不同IP之间互不影响
     */
    IP,

    /**
     * 实例限流（集群多后端实例）
     * 基于Redis的PER_CLIENT模式，每个应用实例独立限流
     */
    CLUSTER
}
