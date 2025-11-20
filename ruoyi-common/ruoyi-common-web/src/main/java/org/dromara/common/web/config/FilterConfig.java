package org.dromara.common.web.config;

// 导入Servlet分发类型枚举，用于配置过滤器拦截的请求类型
// DispatcherType定义了Servlet请求的分发类型，如REQUEST、FORWARD、INCLUDE、ERROR、ASYNC
import jakarta.servlet.DispatcherType;
// 导入XSS防护配置属性类，用于读取配置文件中的XSS相关配置
// XssProperties从application.yml中读取xss前缀的配置项，如enabled、excludeUrls
import org.dromara.common.web.config.properties.XssProperties;
// 导入可重复读取请求体的过滤器，解决请求体只能读取一次的问题
// RepeatableFilter将请求包装为RepeatedlyRequestWrapper，缓存请求体内容到字节数组
import org.dromara.common.web.filter.RepeatableFilter;
// 导入XSS攻击防护过滤器，防止跨站脚本攻击
// XssFilter对请求参数和请求体进行HTML标签清理，过滤潜在的XSS攻击代码
import org.dromara.common.web.filter.XssFilter;
// 导入Spring Boot自动配置注解，标识此类为自动配置类
// @AutoConfiguration是Spring Boot 2.7+引入的注解，替代@Configuration，用于自动配置类
import org.springframework.boot.autoconfigure.AutoConfiguration;
// 导入条件注解，根据配置属性值决定是否创建Bean
// @ConditionalOnProperty根据配置文件中的属性值决定是否创建Bean，实现按需加载
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
// 导入启用配置属性注解，使XssProperties配置类生效
// @EnableConfigurationProperties用于启用@ConfigurationProperties注解的类，实现类型安全的配置
import org.springframework.boot.context.properties.EnableConfigurationProperties;
// 导入过滤器注册注解，用于配置Servlet过滤器的注册信息
// @FilterRegistration是Spring Boot提供的注解，简化Servlet过滤器的注册配置
import org.springframework.boot.web.servlet.FilterRegistration;
// 导入过滤器注册Bean类，用于配置过滤器的执行顺序等属性
// FilterRegistrationBean是Spring Boot提供的类，用于配置过滤器的名称、URL模式、执行顺序等
import org.springframework.boot.web.servlet.FilterRegistrationBean;
// 导入Bean注解，用于声明Spring Bean
// @Bean注解将方法返回的对象注册为Spring容器管理的Bean，实现依赖注入
import org.springframework.context.annotation.Bean;

/**
 * Servlet过滤器配置类
 * 配置系统中使用的各种过滤器，包括XSS防护过滤器和请求体可重复读取过滤器
 * 使用Spring Boot的FilterRegistration机制注册过滤器，并配置拦截规则
 * 通过@ConditionalOnProperty实现按需加载，提升系统性能和灵活性
 *
 * @author Lion Li
 */
// @AutoConfiguration注解：标识此类为Spring Boot自动配置类，Spring Boot启动时会自动加载
// 自动配置类会在满足条件时自动装配Bean，无需手动配置，遵循约定优于配置原则
@AutoConfiguration
// @EnableConfigurationProperties注解：启用XssProperties配置属性类
// 将配置文件中的xss前缀属性绑定到XssProperties类，实现类型安全的配置，避免硬编码
@EnableConfigurationProperties(XssProperties.class)
public class FilterConfig {

    /**
     * 注册XSS防护过滤器Bean
     * 使用@ConditionalOnProperty条件注解，只有当配置xss.enabled=true时才创建此Bean
     * 使用@FilterRegistration注解配置过滤器的注册信息，包括名称、URL模式、执行顺序等
     * XSS防护过滤器对所有请求进行HTML标签清理，防止跨站脚本攻击
     *
     * @return XssFilter实例，用于过滤XSS攻击
     */
    // @Bean注解：声明此方法返回的对象为Spring Bean，交给Spring容器管理
    // Spring容器会管理Bean的生命周期，包括创建、初始化、销毁，实现依赖注入
    @Bean
    // @ConditionalOnProperty注解：条件注解，当配置文件中xss.enabled=true时才创建此Bean
    // havingValue = "true"表示配置值必须为true，matchIfMissing = false表示配置不存在时不创建
    // 实现按需加载，避免不必要的性能开销
    @ConditionalOnProperty(value = "xss.enabled", havingValue = "true")
    // @FilterRegistration注解：配置Servlet过滤器的注册信息，简化过滤器配置
    // name属性：过滤器名称，用于标识此过滤器，在日志和监控中使用
    // urlPatterns属性：拦截的URL模式，/*表示拦截所有请求路径，对所有请求进行XSS防护
    // order属性：执行顺序，FilterRegistrationBean.HIGHEST_PRECEDENCE + 1表示最高优先级+1
    // 设置为较高优先级，确保在业务逻辑之前执行，尽早过滤恶意请求
    // dispatcherTypes属性：分发类型，DispatcherType.REQUEST表示只拦截直接请求
    // 不拦截FORWARD、INCLUDE等内部转发请求，避免重复过滤
    @FilterRegistration(
        name = "xssFilter", // 过滤器名称，用于标识此过滤器
        urlPatterns = "/*", // 拦截所有请求路径，对所有请求进行XSS防护
        order = FilterRegistrationBean.HIGHEST_PRECEDENCE + 1, // 执行顺序，设置为最高优先级+1，确保尽早执行
        dispatcherTypes = DispatcherType.REQUEST // 只拦截REQUEST类型的请求，不拦截FORWARD、INCLUDE等
    )
    public XssFilter xssFilter() {
        // 创建并返回XssFilter实例
        // XssFilter会对请求参数和请求体进行HTML标签清理，过滤<script>、<iframe>等危险标签
        // 防止XSS（跨站脚本）攻击，保护系统安全
        return new XssFilter();
    }

    /**
     * 注册可重复读取请求体的过滤器Bean
     * 解决HTTP请求流只能读取一次的问题，使请求体可以被多次读取
     * 使用@FilterRegistration注解配置过滤器的注册信息
     * 此过滤器对所有请求进行包装，缓存请求体内容到字节数组
     *
     * @return RepeatableFilter实例，使请求体可重复读取
     */
    // @Bean注解：声明此方法返回的对象为Spring Bean，交给Spring容器管理
    // 实现依赖注入，可以在其他组件中注入使用
    @Bean
    // @FilterRegistration注解：配置Servlet过滤器的注册信息
    // name属性：过滤器名称，用于标识此过滤器
    // urlPatterns属性：拦截所有请求路径，对所有请求进行包装
    // 不指定order属性，使用默认顺序，在XssFilter之后执行
    @FilterRegistration(
        name = "repeatableFilter", // 过滤器名称，用于标识此过滤器
        urlPatterns = "/*" // 拦截所有请求路径，对所有请求进行包装
    )
    public RepeatableFilter repeatableFilter() {
        // 创建并返回RepeatableFilter实例
        // RepeatableFilter会将请求包装为RepeatedlyRequestWrapper，缓存请求体内容到字节数组
        // 使得AOP日志记录和业务处理都可以读取请求体，解决HTTP请求流只能读取一次的问题
        // 提升系统的可观测性和调试能力
        return new RepeatableFilter();
    }

}
