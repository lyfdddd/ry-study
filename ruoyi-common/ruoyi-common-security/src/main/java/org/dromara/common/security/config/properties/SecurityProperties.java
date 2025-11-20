// 定义安全配置属性类的包路径
package org.dromara.common.security.config.properties;

// Lombok注解，自动生成getter、setter、toString、equals、hashCode方法
import lombok.Data;
// Spring Boot配置属性注解
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Security安全配置属性类
 * 用于配置安全相关的属性，如排除路径等
 * 通过application.yml中的security前缀配置
 *
 * @author Lion Li
 */
// Lombok注解，自动生成getter、setter、toString、equals、hashCode方法
@Data
// Spring Boot配置属性注解，指定前缀为security
@ConfigurationProperties(prefix = "security")
// 安全配置属性类
public class SecurityProperties {

    /**
     * 排除路径数组
     * 配置不需要进行安全拦截的路径，如：/login、/captcha等
     */
    private String[] excludes;

}
