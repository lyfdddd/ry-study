// 包声明：定义当前类所在的包路径，org.dromara.monitor.admin.config 表示监控管理配置层
package org.dromara.monitor.admin.config;

// Spring Boot Admin Server配置属性
import de.codecentric.boot.admin.server.config.AdminServerProperties;
// Spring Bean注解：将方法返回值注册为Spring Bean
import org.springframework.context.annotation.Bean;
// Spring配置注解：标记为配置类
import org.springframework.context.annotation.Configuration;
// Spring Security自定义配置
import org.springframework.security.config.Customizer;
// Spring Security HTTP安全配置
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// Spring Security启用Web安全
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// Spring Security抽象HTTP配置
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
// Spring Security请求头配置
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
// Spring Security安全过滤器链
import org.springframework.security.web.SecurityFilterChain;
// Spring Security认证成功处理器
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
// Spring Security路径模式请求匹配器
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

/**
 * 监控管理安全配置类
 * 核心业务：配置Admin Server的安全策略，包括登录认证、路径授权、CSRF防护等
 * 实现功能：
 * - 禁用iframe嵌入限制（frameOptions().disable()）
 * - 配置登录页面和认证成功处理器
 * - 配置静态资源和登录路径允许匿名访问
 * - 其他路径需要认证
 * - 禁用CSRF防护（Admin Server需要）
 *
 * @author Lion Li
 */
// Spring Security注解：启用Web安全
@EnableWebSecurity
// Spring配置注解：标记为配置类
@Configuration
public class SecurityConfig {

    // Admin Server上下文路径（如：/admin）
    private final String adminContextPath;

    /**
     * 构造函数：注入Admin Server配置属性
     *
     * @param adminServerProperties Admin Server配置属性
     */
    public SecurityConfig(AdminServerProperties adminServerProperties) {
        // 获取Admin Server上下文路径
        this.adminContextPath = adminServerProperties.getContextPath();
    }

    /**
     * 配置安全过滤器链
     * 定义Admin Server的安全策略
     *
     * @param httpSecurity HTTP安全配置
     * @return 安全过滤器链
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        // 创建认证成功处理器
        SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
        // 设置重定向参数名
        successHandler.setTargetUrlParameter("redirectTo");
        // 设置默认目标URL
        successHandler.setDefaultTargetUrl(adminContextPath + "/");
        // 创建路径模式请求匹配器构建器
        PathPatternRequestMatcher.Builder mvc = PathPatternRequestMatcher.withDefaults();
        // 配置HTTP安全
        return httpSecurity
            // 配置请求头：禁用iframe嵌入限制（允许Admin UI嵌入iframe）
            .headers((header) ->
                header.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
            // 配置请求授权
            .authorizeHttpRequests((authorize) ->
                // 配置静态资源和登录路径允许匿名访问
                authorize.requestMatchers(
                        mvc.matcher(adminContextPath + "/assets/**"),
                        mvc.matcher(adminContextPath + "/login")
                    ).permitAll()
                    // 其他所有请求需要认证
                    .anyRequest().authenticated())
            // 配置表单登录
            .formLogin((formLogin) ->
                // 设置登录页面和认证成功处理器
                formLogin.loginPage(adminContextPath + "/login").successHandler(successHandler))
            // 配置登出
            .logout((logout) ->
                // 设置登出URL
                logout.logoutUrl(adminContextPath + "/logout"))
            // 配置HTTP Basic认证
            .httpBasic(Customizer.withDefaults())
            // 禁用CSRF防护（Admin Server需要）
            .csrf(AbstractHttpConfigurer::disable)
            // 构建安全过滤器链
            .build();
    }

}
