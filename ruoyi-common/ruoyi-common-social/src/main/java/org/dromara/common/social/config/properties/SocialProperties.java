// 定义社交配置属性类的包路径
package org.dromara.common.social.config.properties;

// Lombok注解，自动生成getter、setter、toString、equals、hashCode方法
import lombok.Data;
// Spring Boot配置属性注解，指定配置文件前缀
import org.springframework.boot.context.properties.ConfigurationProperties;
// Spring组件注解
import org.springframework.stereotype.Component;

// 引入Map集合类
import java.util.Map;

/**
 * 社交登录配置属性类
 * 从application.yml中读取justauth前缀的配置
 * 管理所有第三方社交平台的登录配置
 *
 * @author thiszhc
 */
// Lombok注解，自动生成getter、setter、toString、equals、hashCode方法
@Data
// Spring组件注解，将此类注册为Spring Bean
@Component
// Spring Boot配置属性注解，指定配置文件前缀为justauth
@ConfigurationProperties(prefix = "justauth")
// 社交配置属性类
public class SocialProperties {

    /**
     * 授权类型映射
     * key为社交平台名称（如wechat_open、qq、github等）
     * value为对应平台的详细配置
     */
    private Map<String, SocialLoginConfigProperties> type;

}
