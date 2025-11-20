// 定义安全配置类的包路径
package org.dromara.common.security.config;

// Sa-Token未登录异常类
import cn.dev33.satoken.exception.NotLoginException;
// Sa-Token Servlet过滤器
import cn.dev33.satoken.filter.SaServletFilter;
// Sa-Token HTTP Basic认证工具类
import cn.dev33.satoken.httpauth.basic.SaHttpBasicUtil;
// Sa-Token拦截器
import cn.dev33.satoken.interceptor.SaInterceptor;
// Sa-Token路由匹配器
import cn.dev33.satoken.router.SaRouter;
// Sa-Token身份验证工具类
import cn.dev33.satoken.stp.StpUtil;
// Sa-Token统一返回结果
import cn.dev33.satoken.util.SaResult;
// Sa-Token常量类
import cn.dev33.satoken.util.SaTokenConsts;
// Jakarta Servlet请求接口
import jakarta.servlet.http.HttpServletRequest;
// Jakarta Servlet响应接口
import jakarta.servlet.http.HttpServletResponse;
// Lombok构造函数注解
import lombok.RequiredArgsConstructor;
// Lombok日志注解，自动生成slf4j日志对象
import lombok.extern.slf4j.Slf4j;
// HTTP状态码常量
import org.dromara.common.core.constant.HttpStatus;
// Servlet工具类
import org.dromara.common.core.utils.ServletUtils;
// Spring工具类
import org.dromara.common.core.utils.SpringUtils;
// 字符串工具类
import org.dromara.common.core.utils.StringUtils;
// 登录助手工具类
import org.dromara.common.satoken.utils.LoginHelper;
// 安全配置属性类
import org.dromara.common.security.config.properties.SecurityProperties;
// URL处理器
import org.dromara.common.security.handler.AllUrlHandler;
// Spring Value注解
import org.springframework.beans.factory.annotation.Value;
// Spring Boot自动配置注解
import org.springframework.boot.autoconfigure.AutoConfiguration;
// Spring Boot配置属性启用注解
import org.springframework.boot.context.properties.EnableConfigurationProperties;
// Spring Bean注解
import org.springframework.context.annotation.Bean;
// Spring MVC拦截器注册器
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
// Spring MVC配置接口
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 权限安全配置类
 * 配置Sa-Token拦截器、Actuator认证过滤器等安全相关组件
 * 实现WebMvcConfigurer接口，注册自定义拦截器
 *
 * @author Lion Li
 */
// Lombok日志注解，自动生成slf4j日志对象
@Slf4j
// Spring Boot自动配置注解
@AutoConfiguration
// 启用SecurityProperties配置属性
@EnableConfigurationProperties(SecurityProperties.class)
// Lombok构造函数注解，生成包含final字段的构造函数
@RequiredArgsConstructor
// 安全配置类，实现WebMvcConfigurer接口
public class SecurityConfig implements WebMvcConfigurer {

    /**
     * 安全配置属性
     * 从application.yml中读取security前缀的配置
     */
    private final SecurityProperties securityProperties;
    
    /**
     * SSE路径
     * 从配置文件中读取sse.path的值
     */
    @Value("${sse.path}")
    private String ssePath;

    /**
     * 注册Sa-Token拦截器
     * 配置全局路由拦截规则，实现登录验证和客户端ID校验
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册路由拦截器，自定义验证规则
        registry.addInterceptor(new SaInterceptor(handler -> {
                // 从Spring容器中获取AllUrlHandler，获取所有URL路径
                AllUrlHandler allUrlHandler = SpringUtils.getBean(AllUrlHandler.class);
                // 使用SaRouter配置路由匹配和检查规则
                SaRouter
                    // 匹配所有URL路径
                    .match(allUrlHandler.getUrls())
                    // 对未排除的路径进行检查
                    .check(() -> {
                        // 获取当前请求和响应对象
                        HttpServletRequest request = ServletUtils.getRequest();
                        HttpServletResponse response = ServletUtils.getResponse();
                        // 设置响应内容类型为JSON
                        response.setContentType(SaTokenConsts.CONTENT_TYPE_APPLICATION_JSON);
                        // 检查是否登录，是否有token，未登录会抛出NotLoginException
                        StpUtil.checkLogin();

                        // 检查header和param中的clientId与token中的是否一致
                        // 防止token被盗用，增加安全性
                        String headerCid = request.getHeader(LoginHelper.CLIENT_KEY);
                        String paramCid = ServletUtils.getParameter(LoginHelper.CLIENT_KEY);
                        String clientId = StpUtil.getExtra(LoginHelper.CLIENT_KEY).toString();
                        // 如果header、param中的clientId都不匹配token中的clientId，抛出异常
                        if (!StringUtils.equalsAny(clientId, headerCid, paramCid)) {
                            // token无效，客户端ID与Token不匹配
                            throw NotLoginException.newInstance(StpUtil.getLoginType(),
                                "-100", "客户端ID与Token不匹配",
                                StpUtil.getTokenValue());
                        }

                        // 以下日志会影响性能，仅用于临时测试
                        // if (log.isDebugEnabled()) {
                        //     log.info("剩余有效时间: {}", StpUtil.getTokenTimeout());
                        //     log.info("临时有效时间: {}", StpUtil.getTokenActivityTimeout());
                        // }

                    });
            })).addPathPatterns("/**") // 拦截所有路径
            // 排除不需要拦截的路径，从配置中读取
            .excludePathPatterns(securityProperties.getExcludes())
            // 排除SSE路径，避免拦截SSE长连接
            .excludePathPatterns(ssePath);
    }

    /**
     * 配置Actuator健康检查接口的账号密码鉴权
     * 使用HTTP Basic认证保护Actuator端点
     *
     * @return SaServletFilter过滤器
     */
    @Bean
    public SaServletFilter getSaServletFilter() {
        // 从配置文件中读取Spring Boot Admin的客户端用户名和密码
        String username = SpringUtils.getProperty("spring.boot.admin.client.username");
        String password = SpringUtils.getProperty("spring.boot.admin.client.password");
        // 创建并配置SaServletFilter
        return new SaServletFilter()
            // 拦截/actuator及其子路径
            .addInclude("/actuator", "/actuator/**")
            // 设置认证逻辑，使用HTTP Basic认证
            .setAuth(obj -> {
                // 验证用户名密码，格式：username:password
                SaHttpBasicUtil.check(username + ":" + password);
            })
            // 设置错误处理逻辑
            .setError(e -> {
                // 获取响应对象
                HttpServletResponse response = ServletUtils.getResponse();
                // 设置响应内容类型为JSON
                response.setContentType(SaTokenConsts.CONTENT_TYPE_APPLICATION_JSON);
                // 返回错误信息，状态码为401未授权
                return SaResult.error(e.getMessage()).setCode(HttpStatus.UNAUTHORIZED);
            });
    }

}
