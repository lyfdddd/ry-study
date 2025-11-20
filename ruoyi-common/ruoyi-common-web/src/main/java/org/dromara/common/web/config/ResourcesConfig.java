package org.dromara.common.web.config;

// 导入Hutool的日期时间类，提供日期时间操作功能
import cn.hutool.core.date.DateTime;
// 导入Hutool的日期工具类，提供日期解析和格式化功能
import cn.hutool.core.date.DateUtil;
// 导入对象工具类，用于对象判空操作
import org.dromara.common.core.utils.ObjectUtils;
// 导入全局异常处理器，统一处理系统中的异常
import org.dromara.common.web.handler.GlobalExceptionHandler;
// 导入Web请求调用时间统计拦截器，记录请求处理时间
import org.dromara.common.web.interceptor.PlusWebInvokeTimeInterceptor;
// 导入Spring Boot自动配置注解，标识此类为自动配置类
import org.springframework.boot.autoconfigure.AutoConfiguration;
// 导入Bean注解，用于声明Spring Bean
import org.springframework.context.annotation.Bean;
// 导入格式化器注册接口，用于注册类型转换器
import org.springframework.format.FormatterRegistry;
// 导入CORS配置类，用于配置跨域请求规则
import org.springframework.web.cors.CorsConfiguration;
// 导入基于URL的CORS配置源，用于注册CORS配置
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
// 导入CORS过滤器，处理跨域请求
import org.springframework.web.filter.CorsFilter;
// 导入拦截器注册接口，用于注册拦截器
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
// 导入资源处理器注册接口，用于配置静态资源处理
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
// 导入WebMvc配置接口，提供MVC配置方法
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// 导入Java日期类
import java.util.Date;

/**
 * Web通用配置类
 * 实现WebMvcConfigurer接口，提供Spring MVC的通用配置
 * 包括拦截器注册、类型转换器注册、静态资源处理、CORS跨域配置和全局异常处理
 *
 * @author Lion Li
 */
// @AutoConfiguration注解：标识此类为Spring Boot自动配置类，Spring Boot启动时会自动加载
@AutoConfiguration
public class ResourcesConfig implements WebMvcConfigurer {

    /**
     * 添加拦截器配置
     * 注册PlusWebInvokeTimeInterceptor拦截器，记录所有请求的处理时间
     *
     * @param registry 拦截器注册对象
     */
    // 重写WebMvcConfigurer接口的addInterceptors方法，注册自定义拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册PlusWebInvokeTimeInterceptor拦截器
        // 该拦截器会在请求处理前后记录日志和统计执行时间，用于监控接口性能
        registry.addInterceptor(new PlusWebInvokeTimeInterceptor());
    }

    /**
     * 添加类型转换器配置
     * 注册字符串到Date类型的转换器，支持自动将请求参数中的日期字符串转换为Date对象
     * 使用Hutool的DateUtil解析日期，支持多种日期格式
     *
     * @param registry 格式化器注册对象
     */
    // 重写WebMvcConfigurer接口的addFormatters方法，注册自定义类型转换器
    @Override
    public void addFormatters(FormatterRegistry registry) {
        // 注册String到Date的转换器
        registry.addConverter(String.class, Date.class, source -> {
            // 使用Hutool的DateUtil解析日期字符串
            // DateUtil支持多种日期格式，如yyyy-MM-dd、yyyy-MM-dd HH:mm:ss等
            DateTime parse = DateUtil.parse(source);
            // 判断解析结果是否为null
            if (ObjectUtils.isNull(parse)) {
                // 解析失败，返回null
                return null;
            }
            // 将Hutool的DateTime对象转换为JDK标准的Date对象
            // 因为Spring MVC需要JDK标准的Date类型
            return parse.toJdkDate();
        });
    }

    /**
     * 添加静态资源处理器配置
     * 配置静态资源（如图片、CSS、JS）的访问路径和映射
     * 当前实现为空，由具体项目根据需要实现
     *
     * @param registry 资源处理器注册对象
     */
    // 重写WebMvcConfigurer接口的addResourceHandlers方法，配置静态资源处理
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 当前未配置静态资源路径，由具体项目实现
        // 例如可以配置：registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
    }

    /**
     * 配置CORS跨域请求
     * 注册CorsFilter Bean，处理跨域请求
     * 允许所有来源、所有请求头、所有HTTP方法，并支持携带凭证（如Cookie）
     *
     * @return CorsFilter CORS过滤器实例
     */
    // @Bean注解：声明此方法返回的对象为Spring Bean，交给Spring容器管理
    @Bean
    public CorsFilter corsFilter() {
        // 创建CORS配置对象，用于配置跨域规则
        CorsConfiguration config = new CorsConfiguration();
        // 允许携带凭证（如Cookie、Session等）
        // 设置为true时，allowedOrigins不能为*，必须使用allowedOriginPatterns
        config.setAllowCredentials(true);
        // 设置允许的访问源地址模式，*表示允许所有来源
        // 使用addAllowedOriginPattern而不是addAllowedOrigin，支持*通配符
        config.addAllowedOriginPattern("*");
        // 设置允许的请求头，*表示允许所有请求头
        config.addAllowedHeader("*");
        // 设置允许的HTTP请求方法，*表示允许所有方法（GET、POST、PUT、DELETE等）
        config.addAllowedMethod("*");
        // 设置预检请求（OPTIONS）的有效期，1800秒（30分钟）
        // 在有效期内，浏览器不会重复发送预检请求，提升性能
        config.setMaxAge(1800L);
        
        // 创建基于URL的CORS配置源，用于注册CORS配置
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 注册CORS配置，/**表示匹配所有路径，对所有请求应用此CORS配置
        source.registerCorsConfiguration("/**", config);
        // 创建并返回CorsFilter实例，Spring会自动将此过滤器添加到过滤器链中
        return new CorsFilter(source);
    }

    /**
     * 注册全局异常处理器Bean
     * GlobalExceptionHandler统一处理系统中的所有异常，返回统一的响应格式
     *
     * @return GlobalExceptionHandler 全局异常处理器实例
     */
    // @Bean注解：声明此方法返回的对象为Spring Bean
    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        // 创建并返回GlobalExceptionHandler实例
        // GlobalExceptionHandler使用@RestControllerAdvice注解，会拦截所有Controller的异常
        // 并将异常转换为统一的R响应格式返回给客户端
        return new GlobalExceptionHandler();
    }
}
