package org.dromara.common.web.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * xss过滤 配置属性
 *
 * @author Lion Li
 */
// Lombok注解，自动生成getter、setter、toString等方法
@Data
// Spring Boot配置属性注解，将配置文件中xss前缀的属性绑定到此类
@ConfigurationProperties(prefix = "xss")
public class XssProperties {

    /**
     * Xss开关
     */
    // XSS过滤功能开关，true表示启用，false表示禁用
    private Boolean enabled;

    /**
     * 排除路径
     */
    // 不需要进行XSS过滤的URL路径列表，默认为空列表
    private List<String> excludeUrls = new ArrayList<>();

}
