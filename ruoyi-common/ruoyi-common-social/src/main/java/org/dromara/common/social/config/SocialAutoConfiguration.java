// 定义社交自动配置类的包路径
package org.dromara.common.social.config;

// 引入JustAuth的授权状态缓存接口
import me.zhyd.oauth.cache.AuthStateCache;
// 引入社交配置属性类
import org.dromara.common.social.config.properties.SocialProperties;
// 引入基于Redis的授权状态缓存实现
import org.dromara.common.social.utils.AuthRedisStateCache;
// Spring Boot自动配置注解
import org.springframework.boot.autoconfigure.AutoConfiguration;
// Spring Boot启用配置属性注解
import org.springframework.boot.context.properties.EnableConfigurationProperties;
// Spring Bean注解
import org.springframework.context.annotation.Bean;

/**
 * 社交登录自动配置类
 * 配置社交登录相关的Bean，包括授权状态缓存
 *
 * @author thiszhc
 */
// Spring Boot自动配置注解
@AutoConfiguration
// 启用SocialProperties配置属性
@EnableConfigurationProperties(SocialProperties.class)
// 社交自动配置类
public class SocialAutoConfiguration {

    /**
     * 配置授权状态缓存Bean
     * 使用Redis实现授权状态的存储，支持分布式环境
     *
     * @return AuthStateCache实例
     */
    @Bean
    public AuthStateCache authStateCache() {
        // 创建并返回AuthRedisStateCache实例
        return new AuthRedisStateCache();
    }

}
