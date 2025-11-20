package org.dromara.common.tenant.config;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import org.dromara.common.core.utils.reflect.ReflectUtils;
import org.dromara.common.redis.config.RedisConfig;
import org.dromara.common.redis.config.properties.RedissonProperties;
import org.dromara.common.tenant.core.TenantSaTokenDao;
import org.dromara.common.tenant.handle.PlusTenantLineHandler;
import org.dromara.common.tenant.handle.TenantKeyPrefixHandler;
import org.dromara.common.tenant.manager.TenantSpringCacheManager;
import org.dromara.common.tenant.properties.TenantProperties;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.SingleServerConfig;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * 租户配置类
 *
 * @author Lion Li
 */
// 启用配置属性绑定，将tenant开头的配置项绑定到TenantProperties类
@EnableConfigurationProperties(TenantProperties.class)
// Spring Boot自动配置类，在RedisConfig之后加载
@AutoConfiguration(after = {RedisConfig.class})
// 条件注解，当配置项tenant.enable的值为true时才加载此配置类
@ConditionalOnProperty(value = "tenant.enable", havingValue = "true")
public class TenantConfig {

    // 条件注解，当类路径中存在TenantLineInnerInterceptor类时才加载此配置
    @ConditionalOnClass(TenantLineInnerInterceptor.class)
    // Spring Boot自动配置类
    @AutoConfiguration
    static class MybatisPlusConfiguration {

        /**
         * 多租户插件
         */
        // 创建TenantLineInnerInterceptor Bean，用于MyBatis-Plus多租户SQL拦截
        @Bean
        public TenantLineInnerInterceptor tenantLineInnerInterceptor(TenantProperties tenantProperties) {
            // 使用PlusTenantLineHandler处理租户逻辑
            return new TenantLineInnerInterceptor(new PlusTenantLineHandler(tenantProperties));
        }

    }

    // 创建Redisson自定义配置器，用于设置多租户Redis key前缀
    @Bean
    public RedissonAutoConfigurationCustomizer tenantRedissonCustomizer(RedissonProperties redissonProperties) {
        // 返回Lambda表达式，在Redisson配置完成后执行
        return config -> {
            // 创建租户key前缀处理器
            TenantKeyPrefixHandler nameMapper = new TenantKeyPrefixHandler(redissonProperties.getKeyPrefix());
            // 通过反射获取单机配置
            SingleServerConfig singleServerConfig = ReflectUtils.invokeGetter(config, "singleServerConfig");
            if (ObjectUtil.isNotNull(singleServerConfig)) {
                // 使用单机模式
                // 设置多租户 redis key前缀
                singleServerConfig.setNameMapper(nameMapper);
            }
            // 通过反射获取集群配置
            ClusterServersConfig clusterServersConfig = ReflectUtils.invokeGetter(config, "clusterServersConfig");
            // 集群配置方式 参考下方注释
            if (ObjectUtil.isNotNull(clusterServersConfig)) {
                // 设置多租户 redis key前缀
                clusterServersConfig.setNameMapper(nameMapper);
            }
        };
    }

    /**
     * 多租户缓存管理器
     */
    // 标记为Primary Bean，优先使用此缓存管理器
    @Primary
    @Bean
    public CacheManager tenantCacheManager() {
        // 返回TenantSpringCacheManager实例，支持多租户缓存隔离
        return new TenantSpringCacheManager();
    }

    /**
     * 多租户鉴权dao实现
     */
    // 标记为Primary Bean，优先使用此SaTokenDao实现
    @Primary
    @Bean
    public SaTokenDao tenantSaTokenDao() {
        // 返回TenantSaTokenDao实例，支持多租户Sa-Token数据隔离
        return new TenantSaTokenDao();
    }

}
