// 定义限流配置类的包路径
package org.dromara.common.ratelimiter.config;

// 限流切面类
import org.dromara.common.ratelimiter.aspectj.RateLimiterAspect;
// Spring Boot自动配置注解
import org.springframework.boot.autoconfigure.AutoConfiguration;
// Spring Bean注解
import org.springframework.context.annotation.Bean;
// Redis配置类
import org.springframework.data.redis.connection.RedisConfiguration;

/**
 * 限流自动配置类
 * 配置限流切面Bean，在Redis配置之后加载
 * 确保Redis连接已准备好再初始化限流组件
 *
 * @author guangxin
 * @date 2023/1/18
 */
// Spring Boot自动配置注解，在RedisConfiguration之后加载
@AutoConfiguration(after = RedisConfiguration.class)
// 限流配置类
public class RateLimiterConfig {

    /**
     * 创建限流切面Bean
     * 将RateLimiterAspect注册为Spring Bean，使其生效
     *
     * @return 限流切面实例
     */
    // Spring Bean注解，将方法返回值注册为Bean
    @Bean
    // 创建限流切面Bean
    public RateLimiterAspect rateLimiterAspect() {
        // 返回限流切面实例
        return new RateLimiterAspect();
    }

}
