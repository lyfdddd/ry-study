package org.dromara.common.web.config;

// 导入自定义的LocaleResolver实现类，用于解析国际化信息
// I18nLocaleResolver实现了LocaleResolver接口，从请求头content-language解析区域设置
import org.dromara.common.web.core.I18nLocaleResolver;
// 导入Spring Boot自动配置注解，标识此类为自动配置类
// @AutoConfiguration是Spring Boot 2.7+引入的注解，替代@Configuration，用于自动配置类
import org.springframework.boot.autoconfigure.AutoConfiguration;
// 导入Spring MVC自动配置类，用于指定加载顺序
// WebMvcAutoConfiguration是Spring Boot的Web MVC自动配置类，提供默认的MVC配置
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
// 导入Bean注解，用于声明Spring Bean
// @Bean注解将方法返回的对象注册为Spring容器管理的Bean，实现依赖注入
import org.springframework.context.annotation.Bean;
// 导入LocaleResolver接口，Spring MVC用于解析区域设置的接口
// LocaleResolver是Spring MVC的核心接口，用于解析每个请求的区域设置（语言和国家）
import org.springframework.web.servlet.LocaleResolver;

/**
 * 国际化配置类
 * 配置系统中使用的国际化组件，自定义LocaleResolver实现
 * 支持从请求头content-language解析用户的区域设置（语言和国家）
 * 格式如：zh_CN（中文-中国）、en_US（英文-美国）
 * 通过自定义LocaleResolver，实现灵活的国际化支持
 *
 * @author Lion Li
 */
// @AutoConfiguration注解：标识此类为Spring Boot自动配置类，Spring Boot启动时会自动加载
// 自动配置类会在满足条件时自动装配Bean，无需手动配置，遵循约定优于配置原则
// before属性：在WebMvcAutoConfiguration之前加载，确保自定义的LocaleResolver优先于默认配置
// 通过指定加载顺序，覆盖Spring Boot默认的LocaleResolver实现
@AutoConfiguration(before = WebMvcAutoConfiguration.class)
public class I18nConfig {

    /**
     * 注册自定义的LocaleResolver Bean
     * 用于解析HTTP请求中的国际化信息，支持从请求头content-language获取语言和国家
     * 格式如：zh_CN（中文-中国）、en_US（英文-美国）
     * 通过@Bean注解将I18nLocaleResolver注册为Spring Bean，Spring MVC会自动使用此解析器
     *
     * @return LocaleResolver 自定义的国际化解析器实例
     */
    // @Bean注解：声明此方法返回的对象为Spring Bean，交给Spring容器管理
    // Spring容器会管理Bean的生命周期，包括创建、初始化、销毁
    // Spring MVC会使用此Bean来解析每个请求的区域设置，实现国际化支持
    @Bean
    public LocaleResolver localeResolver() {
        // 创建并返回I18nLocaleResolver实例
        // I18nLocaleResolver实现了从请求头content-language解析Locale的逻辑
        // 通过按下划线分割字符串，提取语言代码和国家代码，创建Locale对象
        return new I18nLocaleResolver();
    }

}
