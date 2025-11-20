package org.dromara.common.encrypt.config;

// 导入Jakarta Servlet分发类型枚举
import jakarta.servlet.DispatcherType;
// 导入加密过滤器
import org.dromara.common.encrypt.filter.CryptoFilter;
// 导入API解密配置属性
import org.dromara.common.encrypt.properties.ApiDecryptProperties;
// 导入Spring自动配置注解
import org.springframework.boot.autoconfigure.AutoConfiguration;
// 导入条件配置注解
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
// 导入启用配置属性注解
import org.springframework.boot.context.properties.EnableConfigurationProperties;
// 导入过滤器注册注解
import org.springframework.boot.web.servlet.FilterRegistration;
// 导入过滤器注册Bean
import org.springframework.boot.web.servlet.FilterRegistrationBean;
// 导入Bean定义注解
import org.springframework.context.annotation.Bean;

/**
 * API解密自动配置类
 * 配置API请求响应的加解密过滤器
 *
 * @author wdhcr
 */
// 标记为Spring自动配置类
@AutoConfiguration
// 启用ApiDecryptProperties配置属性
@EnableConfigurationProperties(ApiDecryptProperties.class)
// 条件配置，当api-decrypt.enabled为true时加载
@ConditionalOnProperty(value = "api-decrypt.enabled", havingValue = "true")
public class ApiDecryptAutoConfiguration {

    /**
     * 配置加密过滤器
     * 拦截所有请求进行加解密处理
     */
    @Bean
    @FilterRegistration(
        // 过滤器名称
        name = "cryptoFilter",
        // 拦截所有URL路径
        urlPatterns = "/*",
        // 设置最高优先级
        order = FilterRegistrationBean.HIGHEST_PRECEDENCE,
        // 只拦截REQUEST类型的请求
        dispatcherTypes = DispatcherType.REQUEST
    )
    public CryptoFilter cryptoFilter(ApiDecryptProperties properties) {
        // 创建并返回CryptoFilter实例，传入配置属性
        return new CryptoFilter(properties);
    }

}
